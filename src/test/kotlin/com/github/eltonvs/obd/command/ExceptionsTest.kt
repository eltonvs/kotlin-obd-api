package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@RunWith(Parameterized::class)
class UnSupportedCommandExceptionParameterizedTests(
    private val rawValue: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                // OBD-II negative response: 7F <service> <11 or 12>
                arrayOf<Any>("7F0111"),
                arrayOf<Any>("7F 01 11"),
                arrayOf<Any>("7F0A12"),
                arrayOf<Any>("7F 0A 12"),
                arrayOf<Any>("7F 01 12\r"),
            )
    }

    @Test
    fun `test unsupported command response throws`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        assertFailsWith<UnSupportedCommandException> {
            SpeedCommand().handleResponse(rawResponse)
        }
    }
}

class UnSupportedCommandExceptionTests {
    @Test
    fun `test negative response with other error code is not unsupported`() {
        val rawResponse = ObdRawResponse(value = "7F0B13", elapsedTime = 0)
        val exception = runCatching { SpeedCommand().handleResponse(rawResponse) }.exceptionOrNull()
        assertFalse(exception is UnSupportedCommandException, "Unexpected $exception")
    }

    @Test
    fun `test data containing negative response bytes is parsed`() {
        val rawResponse = ObdRawResponse(value = "410D7F0111", elapsedTime = 0)
        val obdResponse = SpeedCommand().handleResponse(rawResponse)
        assertEquals("127", obdResponse.value)
    }

    @Test
    fun `test multi-frame trouble codes containing negative response bytes are parsed`() {
        val rawResponse = ObdRawResponse(value = "00A\r0:43047F011101\r1:011901200000", elapsedTime = 0)
        val obdResponse = TroubleCodesCommand().handleResponse(rawResponse)
        assertEquals(listOf("C3F01", "P1101", "P0119", "P0120").joinToString(","), obdResponse.formattedValue)
    }

    @Test
    fun `test negative response mixed with another ECU response is not unsupported`() {
        val rawResponse = ObdRawResponse(value = "7F0112\r410D15", elapsedTime = 0)
        val exception = runCatching { SpeedCommand().handleResponse(rawResponse) }.exceptionOrNull()
        assertFalse(exception is UnSupportedCommandException, "Unexpected $exception")
    }
}
