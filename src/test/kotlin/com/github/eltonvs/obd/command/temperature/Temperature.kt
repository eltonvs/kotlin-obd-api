package com.github.eltonvs.obd.command.temperature

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class AirIntakeTemperatureCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410F40", 24f),
            arrayOf("410F5D", 53f),
            arrayOf("410F00", -40f),
            arrayOf("410FFF", 215f),
            arrayOf("410FFFFF", 215f)
        )
    }

    @Test
    fun `test valid air intake temperature responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AirIntakeTemperatureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f°C".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class AmbientAirTemperatureCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("414640", 24f),
            arrayOf("41465D", 53f),
            arrayOf("414600", -40f),
            arrayOf("4146FF", 215f),
            arrayOf("4146FFFF", 215f)
        )
    }

    @Test
    fun `test valid ambient air intake temperature responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AmbientAirTemperatureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f°C".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class EngineCoolantTemperatureCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410540", 24f),
            arrayOf("41055D", 53f),
            arrayOf("410500", -40f),
            arrayOf("4105FF", 215f),
            arrayOf("4105FFFF", 215f)
        )
    }

    @Test
    fun `test valid engine coolant temperature responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = EngineCoolantTemperatureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f°C".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class OilTemperatureCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("415C40", 24f),
            arrayOf("415C5D", 53f),
            arrayOf("415C00", -40f),
            arrayOf("415CFF", 215f),
            arrayOf("415CFFFF", 215f)
        )
    }

    @Test
    fun `test valid oil temperature responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = OilTemperatureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f°C".format(expected), obdResponse.formattedValue)
    }
}