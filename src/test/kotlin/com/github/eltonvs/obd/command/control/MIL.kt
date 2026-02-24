package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class MILOnCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Boolean,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("410100452100", false),
                arrayOf<Any>("410100000000", false),
                arrayOf<Any>("41017F000000", false),
                arrayOf<Any>("41017FFFFFFF", false),
                arrayOf<Any>("410180000000", true),
                arrayOf<Any>("410180FFFFFF", true),
                arrayOf<Any>("4101FFFFFFFF", true),
            )
    }

    @Test
    fun `test valid MIL on responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            MILOnCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("MIL is ${if (expected) "ON" else "OFF"}", obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class DistanceMILOnCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Int,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("41210000", 0),
                arrayOf<Any>("41215C8D", 23_693),
                arrayOf<Any>("4121FFFF", 65_535),
            )
    }

    @Test
    fun `test valid distance MIL on responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            DistanceMILOnCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("${expected}Km", obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class TimeSinceMILOnCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Int,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("414D0000", 0),
                arrayOf<Any>("414D5C8D", 23_693),
                arrayOf<Any>("414DFFFF", 65_535),
            )
    }

    @Test
    fun `test valid time since MIL on responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            TimeSinceMILOnCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("${expected}min", obdResponse.formattedValue)
    }
}
