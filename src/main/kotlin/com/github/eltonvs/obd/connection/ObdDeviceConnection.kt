package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.removeAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import kotlin.system.measureTimeMillis


class ObdDeviceConnection(
    private val inputStream: InputStream,
    private val outputStream: OutputStream
) {
    private val responseCache = mutableMapOf<ObdCommand, ObdRawResponse>()

    suspend fun run(
        command: ObdCommand,
        useCache: Boolean = false,
        delayTime: Long = 0,
        maxRetries: Int = 5,
    ): ObdResponse = runBlocking {
        val obdRawResponse =
            if (useCache && responseCache[command] != null) {
                responseCache.getValue(command)
            } else {
                runCommand(command, delayTime, maxRetries).also {
                    // Save response to cache
                    if (useCache) {
                        responseCache[command] = it
                    }
                }
            }
        command.handleResponse(obdRawResponse)
    }

    private suspend fun runCommand(command: ObdCommand, delayTime: Long, maxRetries: Int): ObdRawResponse {
        var rawData = ""
        val elapsedTime = measureTimeMillis {
            sendCommand(command, delayTime)
            rawData = readRawData(maxRetries)
        }
        return ObdRawResponse(rawData, elapsedTime)
    }

    private suspend fun sendCommand(command: ObdCommand, delayTime: Long) = runBlocking {
        withContext(Dispatchers.IO) {
            outputStream.write("${command.rawCommand}\r".toByteArray())
            outputStream.flush()
            if (delayTime > 0) {
                delay(delayTime)
            }
        }
    }

    private suspend fun readRawData(maxRetries: Int): String = runBlocking {
        var b: Byte
        var c: Char
        val res = StringBuffer()
        var retriesCount = 0

        withContext(Dispatchers.IO) {
            // read until '>' arrives OR end of stream reached (-1)
            while (retriesCount <= maxRetries) {
                if (inputStream.available() > 0) {
                    b = inputStream.read().toByte()
                    if (b < 0) {
                        break
                    }
                    c = b.toInt().toChar()
                    if (c == '>') {
                        break
                    }
                    res.append(c)
                } else {
                    retriesCount += 1
                    delay(500)
                }
            }
            removeAll(SEARCHING_PATTERN, res.toString()).trim()
        }
    }
}