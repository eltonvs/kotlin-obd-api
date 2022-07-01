package com.github.eltonvs.obd.command.pressure

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class BarometricPressureCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("413312", 18),
            arrayOf("413340", 64),
            arrayOf("413364", 100),
            arrayOf("413380", 128),
            arrayOf("413300", 0),
            arrayOf("4133FF", 255),
            arrayOf("4133FFFF", 255)
        )
    }

    @Test
    fun `test valid barometric pressure responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = BarometricPressureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}kPa", obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class IntakeManifoldPressureCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410B12", 18),
            arrayOf("410B39", 57),
            arrayOf("410B40", 64),
            arrayOf("410B64", 100),
            arrayOf("410B80", 128),
            arrayOf("410B00", 0),
            arrayOf("410BFF", 255),
            arrayOf("410BFFFF", 255)
        )
    }

    @Test
    fun `test valid intake manifold pressure responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = IntakeManifoldPressureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}kPa", obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class FuelPressureCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410A12", 54),
            arrayOf("410A40", 192),
            arrayOf("410A64", 300),
            arrayOf("410A80", 384),
            arrayOf("410A00", 0),
            arrayOf("410AFF", 765),
            arrayOf("410AFFFF", 765)
        )
    }

    @Test
    fun `test valid fuel pressure responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelPressureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}kPa", obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class FuelRailPressureCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("41230000", 0.000f),
            arrayOf("410B39", 4.503f),
            arrayOf("410B6464", 2030.300f),
            arrayOf("4123FFFF", 5177.265f)
        )
    }

    @Test
    fun `test valid fuel rail pressure responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelRailPressureCommand().run {
            handleResponse(rawResponse)
        }

        assertEquals("%.3f".format(expected) + "kPa", obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class FuelRailGaugePressureCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("41231234", 46_600),
            arrayOf("41234354", 172_360),
            arrayOf("412360ED", 248_130),
            arrayOf("41238080", 328_960),
            arrayOf("41230000", 0),
            arrayOf("4123FFFF", 655_350)
        )
    }

    @Test
    fun `test valid fuel rail gauge pressure responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelRailGaugePressureCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}kPa", obdResponse.formattedValue)
    }
}