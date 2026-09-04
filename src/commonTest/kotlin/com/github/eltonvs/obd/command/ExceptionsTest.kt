package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.engine.SpeedCommand
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ExceptionsTest {
    private val command = SpeedCommand()

    @Test
    fun `unsupported command response throws UnSupportedCommandException`() {
        // OBD-II negative response: 7F <service> <error code 11 or 12>
        listOf(
            "7F0111",
            "7F 01 11",
            "7F0A12",
            "7F 0A 12",
            // Adapter echo / headers around the negative response
            "01 0D\r7F 01 11",
            "7E8 03 7F 01 12",
            // CAN frame padded out to eight bytes
            "7E8 03 7F 01 11 00 00 00 00",
        ).forEach { rawValue ->
            assertFailsWith<UnSupportedCommandException>("Expected exception for: $rawValue") {
                val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
                command.handleResponse(rawResponse)
            }
        }
    }

    @Test
    fun `payload bytes that read like a negative response do not throw`() {
        // The frame is a positive response whose data happens to contain 7F 01 11.
        listOf(
            "41 0D 7F",
            "41017F0111",
            "41 01 7F 01 11 00",
            "410D40",
        ).forEach { rawValue ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            try {
                command.handleResponse(rawResponse)
            } catch (_: UnSupportedCommandException) {
                throw AssertionError("Should not throw UnSupportedCommandException for: $rawValue")
            } catch (_: Exception) {
                // Other exceptions are acceptable
            }
        }
    }

    @Test
    fun `out-of-range negative response does not throw UnSupportedCommandException`() {
        // 7F0B13 — error code 13 is outside the 11-12 range, should not match
        val rawResponse = ObdRawResponse(value = "7F0B13", elapsedTime = 0)
        try {
            command.handleResponse(rawResponse)
        } catch (e: UnSupportedCommandException) {
            throw AssertionError("Should not throw UnSupportedCommandException for 7F0B13")
        } catch (_: Exception) {
            // Other exceptions are acceptable
        }
    }
}
