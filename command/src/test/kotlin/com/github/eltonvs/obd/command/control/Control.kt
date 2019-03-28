package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class VINCommandParameterizedTests(private val rawValue: String, private val expected: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun vinValues() = listOf(
            // CAN (ISO-15765) format
            arrayOf("0140:4902013933591:425352375248452:4A323938313136", "93YBSR7RHEJ298116"),
            arrayOf("0140:4902015750301:5A5A5A39395A542:53333932313234", "WP0ZZZ99ZTS392124"),
            // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) format
            arrayOf("490201000000394902023359425349020352375248490204454A323949020538313136", "93YBSR7RHEJ298116"),
            arrayOf("4902010000005749020250305A5A4902035A39395A4902045453333949020532313234", "WP0ZZZ99ZTS392124")
        )
    }

    @Test
    fun `test valid vin responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val vinCommand = VINCommand()
        val obdResponse = vinCommand.handleResponse(rawResponse)
        assertEquals(expected, obdResponse.formattedValue)
    }
}