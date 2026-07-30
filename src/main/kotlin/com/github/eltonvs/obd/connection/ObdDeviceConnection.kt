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
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

private const val LEGACY_READ_RETRY_DELAY_MS = 500L
private const val READ_POLL_INTERVAL_MS = 10L

private data class ReadProgress(
    val interByteDeadline: Long,
    val shouldStop: Boolean,
)

class ObdDeviceConnection
    @JvmOverloads
    constructor(
        private val inputStream: InputStream,
        private val outputStream: OutputStream,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
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
                val cacheKey = cacheKey(command)
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
            var rawData = ""
            val elapsedTime =
                measureTimeMillis {
                    sendCommand(command, delayTime)
                    rawData = readRawData(readPolicy)
                }
            return ObdRawResponse(rawData, elapsedTime)
        }

        private suspend fun sendCommand(
            command: ObdCommand,
            delayTime: Long,
        ) {
            withContext(ioDispatcher) {
                outputStream.write("${command.rawCommand}\r".toByteArray())
                outputStream.flush()
            }
            if (delayTime > 0) {
                delay(delayTime)
            }
        }

        private suspend fun readRawData(readPolicy: ObdReadPolicy): String =
            withContext(ioDispatcher) {
                val responseDeadline = deadlineFromNow(readPolicy.responseTimeoutMs)
                var interByteDeadline = responseDeadline
                val response = StringBuilder()
                var shouldStop = false

                while (!shouldStop) {
                    val readProgress = drainAvailableBytes(response, interByteDeadline, readPolicy)
                    interByteDeadline = readProgress.interByteDeadline
                    shouldStop = readProgress.shouldStop
                    if (!shouldStop) {
                        val now = System.nanoTime()
                        val nextDeadline =
                            if (response.isNotEmpty()) {
                                minOf(responseDeadline, interByteDeadline)
                            } else {
                                responseDeadline
                            }
                        shouldStop = now >= nextDeadline
                        if (!shouldStop) {
                            delay(nextPollDelayMs(nextDeadline - now))
                        }
                    }
                }
                cleanResponse(response)
            }

        private fun cacheKey(command: ObdCommand): String {
            val classKey = command::class.qualifiedName ?: command.javaClass.name
            return "$classKey:${command.rawCommand}"
        }

        private fun cleanResponse(response: StringBuilder): String {
            val cleanedResponse =
                removeAll(
                    SEARCHING_PATTERN,
                    response.toString(),
                )
            return cleanedResponse.trim()
        }

        private fun legacyReadPolicy(maxRetries: Int): ObdReadPolicy {
            require(maxRetries >= 0) { "maxRetries must be >= 0" }
            val timeoutMs = maxRetries.toLong() * LEGACY_READ_RETRY_DELAY_MS
            return ObdReadPolicy(
                responseTimeoutMs = timeoutMs,
                interByteTimeoutMs = timeoutMs,
            )
        }

        private fun drainAvailableBytes(
            response: StringBuilder,
            interByteDeadline: Long,
            readPolicy: ObdReadPolicy,
        ): ReadProgress {
            var nextInterByteDeadline = interByteDeadline
            var shouldStop = false
            while (inputStream.available() > 0 && !shouldStop) {
                val byteValue = inputStream.read()
                if (byteValue == -1) {
                    shouldStop = true
                } else {
                    val charValue = byteValue.toChar()
                    if (charValue == '>') {
                        shouldStop = true
                    } else {
                        response.append(charValue)
                        nextInterByteDeadline = deadlineFromNow(readPolicy.interByteTimeoutMs)
                    }
                }
            }
            return ReadProgress(nextInterByteDeadline, shouldStop)
        }

        private fun deadlineFromNow(durationMs: Long): Long {
            val durationNanos = TimeUnit.MILLISECONDS.toNanos(durationMs)
            return System.nanoTime() + durationNanos
        }

        private fun nextPollDelayMs(remainingNanos: Long): Long {
            val remainingMs = TimeUnit.NANOSECONDS.toMillis(remainingNanos)
            return minOf(READ_POLL_INTERVAL_MS, maxOf(1L, remainingMs))
        }
    }
