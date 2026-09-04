package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.removeAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlin.time.TimeSource

private const val LEGACY_READ_RETRY_DELAY_MS = 500L
private const val READ_POLL_INTERVAL_MS = 10L
private const val READ_CHUNK_SIZE = 256
private const val RESPONSE_TERMINATOR = '>'

/**
 * Serialises OBD commands over a [Source]/[Sink] pair.
 *
 * Bytes are pulled from [inputStream] by a single reader coroutine that is
 * started on the first [run] and owns the source from then on. `run()` only
 * consumes from the reader's channel, so its timeouts fire even when the
 * source blocks in the underlying transport read. Call [close] when the
 * connection is no longer needed to stop the reader; the streams themselves
 * belong to the caller and are not closed.
 */
class ObdDeviceConnection(
    private val inputStream: Source,
    private val outputStream: Sink,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AutoCloseable {
    private val runMutex = Mutex()
    private val responseCache = mutableMapOf<String, ObdRawResponse>()

    private val readerScope = CoroutineScope(SupervisorJob() + dispatcher)
    private val incoming = Channel<Byte>(Channel.UNLIMITED)
    private val reader: Job = readerScope.launch(start = CoroutineStart.LAZY) { pumpInput() }

    suspend fun run(
        command: ObdCommand,
        useCache: Boolean = false,
        delayTime: Long = 0,
        maxRetries: Int = 5,
    ): ObdResponse = runWithReadPolicy(command, useCache, delayTime, legacyReadPolicy(maxRetries))

    internal suspend fun runWithReadPolicy(
        command: ObdCommand,
        useCache: Boolean = false,
        delayTime: Long = 0,
        readPolicy: ObdReadPolicy,
    ): ObdResponse =
        runMutex.withLock {
            val cacheKey = "${command.tag}:${command.rawCommand}"
            val obdRawResponse =
                if (useCache && responseCache[cacheKey] != null) {
                    responseCache.getValue(cacheKey)
                } else {
                    runCommand(command, delayTime, readPolicy).also {
                        if (useCache) {
                            responseCache[cacheKey] = it
                        }
                    }
                }
            command.handleResponse(obdRawResponse)
        }

    override fun close() {
        readerScope.cancel()
        incoming.close()
    }

    private suspend fun runCommand(
        command: ObdCommand,
        delayTime: Long,
        readPolicy: ObdReadPolicy,
    ): ObdRawResponse {
        val mark = TimeSource.Monotonic.markNow()
        sendCommand(command, delayTime)
        val rawData = readRawData(readPolicy)
        val elapsedTime = mark.elapsedNow().inWholeMilliseconds
        return ObdRawResponse(rawData, elapsedTime)
    }

    private suspend fun sendCommand(
        command: ObdCommand,
        delayTime: Long,
    ) {
        withContext(dispatcher) {
            outputStream.write("${command.rawCommand}\r".encodeToByteArray())
            outputStream.flush()
        }
        if (delayTime > 0) {
            delay(delayTime)
        }
    }

    /**
     * Reader loop: copies whatever the source yields into [incoming].
     *
     * A transport-backed source (`RawSource.buffered()`) blocks in
     * `readAtMostTo` until data arrives and returns -1 only once exhausted, so
     * -1 closes the channel. A plain [Buffer] returns -1 whenever it is
     * momentarily empty, so it is polled instead. A source that throws (e.g.
     * closed underneath us) closes the channel with that cause.
     */
    private suspend fun pumpInput() {
        val chunk = ByteArray(READ_CHUNK_SIZE)
        val pollWhenEmpty = inputStream is Buffer
        try {
            while (readerScope.isActive) {
                val read = inputStream.readAtMostTo(chunk)
                when {
                    read > 0 -> {
                        for (i in 0 until read) {
                            incoming.trySend(chunk[i])
                        }
                    }

                    pollWhenEmpty -> {
                        delay(READ_POLL_INTERVAL_MS)
                    }

                    else -> {
                        incoming.close()
                        return
                    }
                }
            }
        } catch (e: IllegalStateException) {
            incoming.close(e)
        } catch (e: kotlinx.io.IOException) {
            incoming.close(e)
        }
    }

    private suspend fun readRawData(readPolicy: ObdReadPolicy): String {
        reader.start()
        val response = StringBuilder()

        // Always drain whatever has already arrived before consulting any timeout,
        // so a fully buffered response is returned even with a zero-length budget.
        var shouldStop = drainAvailableBytes(response)
        if (!shouldStop) {
            // The whole read is capped by the response budget; each gap between
            // bytes is capped by the (usually shorter) inter-byte budget.
            withTimeoutOrNull(readPolicy.responseTimeoutMs) {
                while (!shouldStop) {
                    val idleTimeoutMs =
                        if (response.isNotEmpty()) {
                            readPolicy.interByteTimeoutMs
                        } else {
                            readPolicy.responseTimeoutMs
                        }
                    val gotMoreData =
                        withTimeoutOrNull(idleTimeoutMs) {
                            while (incoming.isEmpty) {
                                delay(READ_POLL_INTERVAL_MS)
                            }
                        }
                    shouldStop = gotMoreData == null || drainAvailableBytes(response)
                }
            }
        }
        return cleanResponse(response)
    }

    /**
     * Consumes every byte the reader has delivered so far, returning `true` when
     * the response terminator was seen or the reader has stopped, i.e. reading
     * should end. A reader that stopped because the source failed rethrows.
     */
    private fun drainAvailableBytes(response: StringBuilder): Boolean {
        while (true) {
            val result: ChannelResult<Byte> = incoming.tryReceive()
            if (result.isClosed) {
                result.exceptionOrNull()?.let { throw it }
                return true
            }
            if (result.isFailure) {
                return false
            }
            val charValue = result.getOrThrow().toInt().toChar()
            if (charValue == RESPONSE_TERMINATOR) {
                return true
            }
            response.append(charValue)
        }
    }

    private fun cleanResponse(response: StringBuilder): String = removeAll(SEARCHING_PATTERN, response.toString()).trim()

    private fun legacyReadPolicy(maxRetries: Int): ObdReadPolicy {
        require(maxRetries >= 0) { "maxRetries must be >= 0" }
        val timeoutMs = maxRetries.toLong() * LEGACY_READ_RETRY_DELAY_MS
        return ObdReadPolicy(
            responseTimeoutMs = timeoutMs,
            interByteTimeoutMs = timeoutMs,
        )
    }
}
