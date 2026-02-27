package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleVoltageCommandTests {
    @Test
    fun `test valid module voltage responses handler`() {
        listOf(
            "414204E2" to 1.25f,
            "41420000" to 0f,
            "4142FFFF" to 65.535f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                ModuleVoltageCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 2)}V", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class TimingAdvanceCommandTests {
    @Test
    fun `test valid timing advance responses handler`() {
        listOf(
            "410E70" to -8f,
            "410E00" to -64f,
            "410EFF" to 63.5f,
            "410EFFFF" to 63.5f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                TimingAdvanceCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${formatFloat(expected, 2)}°", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class VINCommandTests {
    @Test
    fun `test valid vin responses handler`() {
        listOf(
            // CAN (ISO-15765) format
            "0140:4902013933591:425352375248452:4A323938313136" to "93YBSR7RHEJ298116",
            "0140:4902015750301:5A5A5A39395A542:53333932313234" to "WP0ZZZ99ZTS392124",
            // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) format
            "490201000000394902023359425349020352375248490204454A323949020538313136" to "93YBSR7RHEJ298116",
            "4902010000005749020250305A5A4902035A39395A4902045453333949020532313234" to "WP0ZZZ99ZTS392124",
            "014 0: 49 02 01 39 42 47 1: 4B 54 34 38 56 30 4A 2: 47 31 34 31 38 30 39" to "9BGKT48V0JG141809",
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                VINCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(expected, obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
