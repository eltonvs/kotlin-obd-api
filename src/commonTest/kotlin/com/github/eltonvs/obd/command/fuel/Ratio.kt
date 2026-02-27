package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandedEquivalenceRatioCommandTests {
    @Test
    fun `test valid commanded equivalence ratio responses handler`() {
        listOf(
            "41441234" to 0.14f,
            "41444040" to 0.5f,
            "41448080" to 1f,
            "41440000" to 0f,
            "4144FFFF" to 2f,
            "4144FFFFFFFF" to 2f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                CommandedEquivalenceRatioCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 2) + "F/A", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class FuelAirEquivalenceRatioCommandTests {
    @Test
    fun `test valid fuel air equivalence ratio responses handler`() {
        listOf(
            "41341234" to 0.14f,
            "41344040" to 0.5f,
            "41348080" to 1f,
            "41340000" to 0f,
            "4134FFFF" to 2f,
            "4134FFFFFFFF" to 2f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            FuelAirEquivalenceRatioCommand.OxygenSensor.entries.forEach { sensor ->
                val obdResponse =
                    FuelAirEquivalenceRatioCommand(sensor).run {
                        handleResponse(rawResponse)
                    }
                assertEquals(formatFloat(expected, 2) + "F/A", obdResponse.formattedValue, "Failed for: $rawValue")
            }
        }
    }
}
