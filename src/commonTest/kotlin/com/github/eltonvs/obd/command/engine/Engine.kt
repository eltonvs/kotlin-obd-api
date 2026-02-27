package com.github.eltonvs.obd.command.engine

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class SpeedCommandTests {
    @Test
    fun `test valid speed responses handler`() {
        listOf(
            "410D15" to 21,
            "410D40" to 64,
            "410D00" to 0,
            "410DFF" to 255,
            "410DFFFF" to 255,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = SpeedCommand().run { handleResponse(rawResponse) }
            assertEquals("${expected}Km/h", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class RPMCommandTests {
    @Test
    fun `test valid rpm responses handler`() {
        listOf(
            "410C200D" to 2051,
            "410C283C" to 2575,
            "410C0A00" to 640,
            "410C0000" to 0,
            "410CFFFF" to 16_383,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = RPMCommand().run { handleResponse(rawResponse) }
            assertEquals("${expected}RPM", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class MassAirFlowCommandTests {
    @Test
    fun `test valid maf responses handler`() {
        listOf(
            "41109511" to 381.61f,
            "41101234" to 46.6f,
            "41100000" to 0f,
            "4110FFFF" to 655.35f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = MassAirFlowCommand().run { handleResponse(rawResponse) }
            assertEquals("${formatFloat(expected, 2)}g/s", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class RuntimeCommandTests {
    @Test
    fun `test valid runtime responses handler`() {
        listOf(
            "411F4543" to "04:55:31",
            "411F1234" to "01:17:40",
            "411F0000" to "00:00:00",
            "411FFFFF" to "18:12:15",
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = RuntimeCommand().run { handleResponse(rawResponse) }
            assertEquals(expected, obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class LoadCommandTests {
    @Test
    fun `test valid engine load responses handler`() {
        listOf(
            "410410" to 6.3f,
            "410400" to 0f,
            "4104FF" to 100f,
            "4104FFFF" to 100f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = LoadCommand().run { handleResponse(rawResponse) }
            assertEquals("${formatFloat(expected, 1)}%", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class AbsoluteLoadCommandTests {
    @Test
    fun `test valid engine absolute load responses handler`() {
        listOf(
            "41434143" to 6551.8f,
            "41431234" to 1827.5f,
            "41430000" to 0f,
            "4143FFFF" to 25_700f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = AbsoluteLoadCommand().run { handleResponse(rawResponse) }
            assertEquals("${formatFloat(expected, 1)}%", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class ThrottlePositionCommandTests {
    @Test
    fun `test valid throttle position responses handler`() {
        listOf(
            "411111" to 6.7f,
            "411100" to 0f,
            "4111FF" to 100f,
            "4111FFFF" to 100f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = ThrottlePositionCommand().run { handleResponse(rawResponse) }
            assertEquals("${formatFloat(expected, 1)}%", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class RelativeThrottlePositionCommandTests {
    @Test
    fun `test valid relative throttle position responses handler`() {
        listOf(
            "414545" to 27.1f,
            "414500" to 0f,
            "4145FF" to 100f,
            "4145FFFF" to 100f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = RelativeThrottlePositionCommand().run { handleResponse(rawResponse) }
            assertEquals("${formatFloat(expected, 1)}%", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
