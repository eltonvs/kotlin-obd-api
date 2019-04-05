package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.removeAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import kotlin.system.measureTimeMillis


class ObdDeviceConnection(private val inputStream: InputStream, private val outputStream: OutputStream) {
    private val responseCache = mutableMapOf<ObdCommand, ObdRawResponse>()

    suspend fun run(command: ObdCommand, useCache: Boolean = false, delayTime: Long = 0): ObdResponse {
        val obdRawResponse =
            if (useCache && responseCache[command] != null) {
                responseCache.getValue(command)
            } else {
                runCommand(command, delayTime).also {
                    // Save response to cache
                    responseCache[command] = it
                }
            }
        return command.handleResponse(obdRawResponse)
    }

    private suspend fun runCommand(command: ObdCommand, delayTime: Long): ObdRawResponse {
        var rawData = ""
        val elapsedTime = measureTimeMillis {
            sendCommand(command, delayTime)
            rawData = readRawData()
        }
        return ObdRawResponse(rawData, elapsedTime)
    }

    private suspend fun sendCommand(command: ObdCommand, delayTime: Long = 0) = withContext(Dispatchers.IO) {
        outputStream.write("${command.rawCommand}\r".toByteArray())
        outputStream.flush()
        if (delayTime > 0) {
            delay(delayTime)
        }
    }

    private suspend fun readRawData(): String = withContext(Dispatchers.IO) {
        var b: Byte
        var c: Char
        val res = StringBuffer()

        // read until '>' arrives OR end of stream reached (-1)
        while (true) {
            b = inputStream.read().toByte()
            if (b < 0) {
                break
            }
            c = b.toChar()
            if (c == '>') {
                break
            }
            res.append(c)
        }

        removeAll(SEARCHING_PATTERN, res.toString()).trim()
    }
}