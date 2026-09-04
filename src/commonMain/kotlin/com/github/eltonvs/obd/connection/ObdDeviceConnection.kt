package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.removeAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlin.time.TimeSource

private const val LEGACY_READ_RETRY_DELAY_MS = 500L
private const val READ_POLL_INTERVAL_MS = 10L

class ObdDeviceConnection(
    private val inputStream: Source,
    private val outputStream: Sink,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val runMutex = Mutex()
    private val responseCache = mutableMapOf<String, ObdRawResponse>()

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

    // Dispatchers.Default is used instead of Dispatchers.IO for Kotlin Multiplatform
    // compatibility. kotlinx-io buffers are non-blocking and work efficiently on Default.
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

    private suspend fun readRawData(readPolicy: ObdReadPolicy): String =
        withContext(dispatcher) {
            val response = StringBuilder()

            // Always drain whatever is already buffered before consulting any timeout,
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
                                while (!inputStream.request(1)) {
                                    delay(READ_POLL_INTERVAL_MS)
                                }
                            }
                        shouldStop =
                            gotMoreData == null ||
                            drainAvailableBytes(response)
                    }
                }
            }
            cleanResponse(response)
        }

    /**
     * Consumes every byte currently buffered, returning `true` when the response
     * terminator (`>`) was seen and reading should stop.
     */
    private fun drainAvailableBytes(response: StringBuilder): Boolean {
        var shouldStop = false
        while (!shouldStop && inputStream.request(1)) {
            val charValue = inputStream.readByte().toInt().toChar()
            if (charValue == '>') {
                shouldStop = true
            } else {
                response.append(charValue)
            }
        }
        return shouldStop
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
