package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class MILOnCommandTests {
    @Test
    fun `test valid MIL on responses handler`() {
        listOf(
            "410100452100" to false,
            "410100000000" to false,
            "41017F000000" to false,
            "41017FFFFFFF" to false,
            "410180000000" to true,
            "410180FFFFFF" to true,
            "4101FFFFFFFF" to true,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                MILOnCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(
                "MIL is ${if (expected) "ON" else "OFF"}",
                obdResponse.formattedValue,
                "Failed for: $rawValue",
            )
        }
    }
}

class DistanceMILOnCommandTests {
    @Test
    fun `test valid distance MIL on responses handler`() {
        listOf(
            "41210000" to 0,
            "41215C8D" to 23_693,
            "4121FFFF" to 65_535,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                DistanceMILOnCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}Km", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class TimeSinceMILOnCommandTests {
    @Test
    fun `test valid time since MIL on responses handler`() {
        listOf(
            "414D0000" to 0,
            "414D5C8D" to 23_693,
            "414DFFFF" to 65_535,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                TimeSinceMILOnCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}min", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
