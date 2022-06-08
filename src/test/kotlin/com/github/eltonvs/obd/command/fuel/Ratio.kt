package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class CommandedEquivalenceRatioCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("41441234", 0.14f),
            arrayOf("41444040", 0.5f),
            arrayOf("41448080", 1f),
            arrayOf("41440000", 0f),
            arrayOf("4144FFFF", 2f),
            arrayOf("4144FFFFFFFF", 2f)
        )
    }

    @Test
    fun `test valid commanded equivalence ratio responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = CommandedEquivalenceRatioCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.2fF/A".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class FuelAirEquivalenceRatioCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("41341234", 0.14f),
            arrayOf("41344040", 0.5f),
            arrayOf("41348080", 1f),
            arrayOf("41340000", 0f),
            arrayOf("4134FFFF", 2f),
            arrayOf("4134FFFFFFFF", 2f)
        )
    }

    @Test
    fun `test valid fuel air equivalence ratio responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        FuelAirEquivalenceRatioCommand.OxygenSensor.values().forEach {
            val obdResponse = FuelAirEquivalenceRatioCommand(it).run {
                handleResponse(rawResponse)
            }
            assertEquals("%.2fF/A".format(expected), obdResponse.formattedValue)
        }
    }
}