package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandedEgrCommandTests {
    @Test
    fun `test valid commanded egr responses handler`() {
        listOf(
            "414545" to 27.1f,
            "414500" to 0f,
            "4145FF" to 100f,
            "4145FFFF" to 100f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                CommandedEgrCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 1)}%", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class EgrErrorCommandTests {
    @Test
    fun `test valid egr error responses handler`() {
        listOf(
            "410610" to -87.5f,
            "410643" to -47.7f,
            "410680" to 0f,
            "4106C8" to 56.25f,
            "410600" to -100f,
            "4106FF" to 99.2f,
            "4106FFFF" to 99.2f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                EgrErrorCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 1)}%", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
