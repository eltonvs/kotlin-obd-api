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
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlin.time.TimeSource

private const val READ_RETRY_DELAY_MS = 500L

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
    ): ObdResponse =
        runMutex.withLock {
            val cacheKey = command.rawCommand
            val obdRawResponse =
                if (useCache && responseCache[cacheKey] != null) {
                    responseCache.getValue(cacheKey)
                } else {
                    runCommand(command, delayTime, maxRetries).also {
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
        maxRetries: Int,
    ): ObdRawResponse {
        var rawData = ""
        val mark = TimeSource.Monotonic.markNow()
        sendCommand(command, delayTime)
        rawData = readRawData(maxRetries)
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

    private suspend fun readRawData(maxRetries: Int): String =
        withContext(dispatcher) {
            val res = StringBuilder()
            var retriesCount = 0

            // Read until '>' arrives or retries are exhausted.
            while (retriesCount <= maxRetries) {
                if (inputStream.request(1)) {
                    val charValue = inputStream.readByte().toInt().toChar()
                    if (charValue == '>') {
                        break
                    }
                    res.append(charValue)
                } else {
                    retriesCount += 1
                    delay(READ_RETRY_DELAY_MS)
                }
            }
            removeAll(SEARCHING_PATTERN, res.toString()).trim()
        }
}
