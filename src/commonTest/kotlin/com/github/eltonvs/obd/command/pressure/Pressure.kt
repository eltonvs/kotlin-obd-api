package com.github.eltonvs.obd.command.pressure

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class BarometricPressureCommandTests {
    @Test
    fun `test valid barometric pressure responses handler`() {
        listOf(
            "413312" to 18,
            "413340" to 64,
            "413364" to 100,
            "413380" to 128,
            "413300" to 0,
            "4133FF" to 255,
            "4133FFFF" to 255,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                BarometricPressureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}kPa", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class IntakeManifoldPressureCommandTests {
    @Test
    fun `test valid intake manifold pressure responses handler`() {
        listOf(
            "410B12" to 18,
            "410B39" to 57,
            "410B40" to 64,
            "410B64" to 100,
            "410B80" to 128,
            "410B00" to 0,
            "410BFF" to 255,
            "410BFFFF" to 255,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                IntakeManifoldPressureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}kPa", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class FuelPressureCommandTests {
    @Test
    fun `test valid fuel pressure responses handler`() {
        listOf(
            "410A12" to 54,
            "410A40" to 192,
            "410A64" to 300,
            "410A80" to 384,
            "410A00" to 0,
            "410AFF" to 765,
            "410AFFFF" to 765,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelPressureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}kPa", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class FuelRailPressureCommandTests {
    @Test
    fun `test valid fuel rail pressure responses handler`() {
        listOf(
            "41230000" to 0.000f,
            "410B39" to 4.503f,
            "410B6464" to 2030.300f,
            "4123FFFF" to 5177.265f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelRailPressureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 3)}kPa", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class FuelRailGaugePressureCommandTests {
    @Test
    fun `test valid fuel rail gauge pressure responses handler`() {
        listOf(
            "41231234" to 46_600,
            "41234354" to 172_360,
            "412360ED" to 248_130,
            "41238080" to 328_960,
            "41230000" to 0,
            "4123FFFF" to 655_350,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelRailGaugePressureCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}kPa", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
