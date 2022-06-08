package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.removeAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import kotlin.system.measureTimeMillis


class ObdDeviceConnection(private val inputStream: InputStream, private val outputStream: OutputStream) {
    private val responseCache = mutableMapOf<ObdCommand, ObdRawResponse>()

    @Synchronized
    suspend fun run(
        command: ObdCommand,
        useCache: Boolean = false,
        delayTime: Long = 0
    ): ObdResponse = withContext(Dispatchers.IO) {
        val obdRawResponse =
            if (useCache && responseCache[command] != null) {
                responseCache.getValue(command)
            } else {
                runCommand(command, delayTime).also {
                    // Save response to cache
                    if (useCache) {
                        responseCache[command] = it
                    }
                }
            }
        command.handleResponse(obdRawResponse)
    }

    private fun runCommand(command: ObdCommand, delayTime: Long): ObdRawResponse {
        var rawData: String
        val elapsedTime = measureTimeMillis {
            sendCommand(command, delayTime)
            rawData = readRawData()
        }
        return ObdRawResponse(rawData, elapsedTime)
    }

    private fun sendCommand(command: ObdCommand, delayTime: Long = 0) {
        outputStream.write("${command.rawCommand}\r".toByteArray())
        outputStream.flush()
        if (delayTime > 0) {
            sleep(delayTime)
        }
    }

    private fun readRawData(): String {
        var b: Byte
        var c: Char
        val res = StringBuffer()

        // read until '>' arrives OR end of stream reached (-1)
        while (inputStream.available() > 0) {
            b = inputStream.read().toByte()
            if (b < 0) {
                break
            }
            c = b.toInt().toChar()
            if (c == '>') {
                break
            }
            res.append(c)
        }

        return removeAll(SEARCHING_PATTERN, res.toString()).trim()
    }
}