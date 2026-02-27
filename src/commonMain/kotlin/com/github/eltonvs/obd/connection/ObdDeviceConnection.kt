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
import kotlin.system.measureTimeMillis

private const val READ_RETRY_DELAY_MS = 500L

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
        ): ObdResponse =
            runMutex.withLock {
                val cacheKey = cacheKey(command)
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
            val elapsedTime =
                measureTimeMillis {
                    sendCommand(command, delayTime)
                    rawData = readRawData(maxRetries)
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

        private suspend fun readRawData(maxRetries: Int): String =
            withContext(ioDispatcher) {
                val res = StringBuilder()
                var retriesCount = 0

                // Read until '>' arrives, stream ends (-1), or retries are exhausted.
                var isReading = true
                while (isReading) {
                    if (inputStream.available() > 0) {
                        val byteValue = inputStream.read()
                        if (byteValue == -1) {
                            isReading = false
                        } else {
                            val charValue = byteValue.toChar()
                            if (charValue == '>') {
                                isReading = false
                            } else {
                                res.append(charValue)
                            }
                        }
                    } else {
                        if (retriesCount >= maxRetries) {
                            isReading = false
                        } else {
                            retriesCount += 1
                            delay(READ_RETRY_DELAY_MS)
                        }
                    }
                }
                removeAll(SEARCHING_PATTERN, res.toString()).trim()
            }

        private fun cacheKey(command: ObdCommand): String {
            val classKey = command::class.qualifiedName ?: command.javaClass.name
            return "$classKey:${command.rawCommand}"
        }
    }
