package com.github.eltonvs.obd.command.engine

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class SpeedCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410D15", 21),
            arrayOf("410D40", 64),
            arrayOf("410D00", 0),
            arrayOf("410DFF", 255),
            arrayOf("410DFFFF", 255)
        )
    }

    @Test
    fun `test valid speed responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = SpeedCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}Km/h", obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class RPMCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410C200D", 2051),
            arrayOf("410C283C", 2575),
            arrayOf("410C0A00", 640),
            arrayOf("410C0000", 0),
            arrayOf("410CFFFF", 16_383)
        )
    }

    @Test
    fun `test valid rpm responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = RPMCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}RPM", obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class MassAirFlowCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("41109511", 381.61f),
            arrayOf("41101234", 46.6f),
            arrayOf("41100000", 0f),
            arrayOf("4110FFFF", 655.35f)
        )
    }

    @Test
    fun `test valid maf responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = MassAirFlowCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.2fg/s".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class RuntimeCommandParameterizedTests(private val rawValue: String, private val expected: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("411F4543", "04:55:31"),
            arrayOf("411F1234", "01:17:40"),
            arrayOf("411F0000", "00:00:00"),
            arrayOf("411FFFFF", "18:12:15")
        )
    }

    @Test
    fun `test valid runtime responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = RuntimeCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals(expected, obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class LoadCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410410", 6.3f),
            arrayOf("410400", 0f),
            arrayOf("4104FF", 100f),
            arrayOf("4104FFFF", 100f)
        )
    }

    @Test
    fun `test valid engine load responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = LoadCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class AbsoluteLoadCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("41434143", 6551.8f),
            arrayOf("41431234", 1827.5f),
            arrayOf("41430000", 0f),
            arrayOf("4143FFFF", 25_700f)
        )
    }

    @Test
    fun `test valid engine absolute load responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AbsoluteLoadCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class ThrottlePositionCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("411111", 6.7f),
            arrayOf("411100", 0f),
            arrayOf("4111FF", 100f),
            arrayOf("4111FFFF", 100f)
        )
    }

    @Test
    fun `test valid throttle position responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = ThrottlePositionCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class RelativeThrottlePositionCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("414545", 27.1f),
            arrayOf("414500", 0f),
            arrayOf("4145FF", 100f),
            arrayOf("4145FFFF", 100f)
        )
    }

    @Test
    fun `test valid relative throttle position responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = RelativeThrottlePositionCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}