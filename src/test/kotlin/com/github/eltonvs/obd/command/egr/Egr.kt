package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class CommandedEgrCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Float,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("414545", 27.1f),
                arrayOf<Any>("414500", 0f),
                arrayOf<Any>("4145FF", 100f),
                arrayOf<Any>("4145FFFF", 100f),
            )
    }

    @Test
    fun `test valid commanded egr responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            CommandedEgrCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class EgrErrorCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Float,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("410610", -87.5f),
                arrayOf<Any>("410643", -47.7f),
                arrayOf<Any>("410680", 0f),
                arrayOf<Any>("4106C8", 56.25f),
                arrayOf<Any>("410600", -100f),
                arrayOf<Any>("4106FF", 99.2f),
                arrayOf<Any>("4106FFFF", 99.2f),
            )
    }

    @Test
    fun `test valid egr error responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            EgrErrorCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}
