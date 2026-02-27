package com.github.eltonvs.obd.command.temperature

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class AirIntakeTemperatureCommandTests {
    @Test
    fun `test valid air intake temperature responses handler`() {
        listOf(
            "410F40" to 24f,
            "410F5D" to 53f,
            "410F00" to -40f,
            "410FFF" to 215f,
            "410FFFFF" to 215f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                AirIntakeTemperatureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 1)}°C", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class AmbientAirTemperatureCommandTests {
    @Test
    fun `test valid ambient air intake temperature responses handler`() {
        listOf(
            "414640" to 24f,
            "41465D" to 53f,
            "414600" to -40f,
            "4146FF" to 215f,
            "4146FFFF" to 215f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                AmbientAirTemperatureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 1)}°C", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class EngineCoolantTemperatureCommandTests {
    @Test
    fun `test valid engine coolant temperature responses handler`() {
        listOf(
            "410540" to 24f,
            "41055D" to 53f,
            "410500" to -40f,
            "4105FF" to 215f,
            "4105FFFF" to 215f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                EngineCoolantTemperatureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 1)}°C", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class OilTemperatureCommandTests {
    @Test
    fun `test valid oil temperature responses handler`() {
        listOf(
            "415C40" to 24f,
            "415C5D" to 53f,
            "415C00" to -40f,
            "415CFF" to 215f,
            "415CFFFF" to 215f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                OilTemperatureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 1)}°C", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
