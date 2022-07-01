package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class ModuleVoltageCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("414204E2", 1.25f),
            arrayOf("41420000", 0f),
            arrayOf("4142FFFF", 65.535f)
        )
    }

    @Test
    fun `test valid module voltage responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = ModuleVoltageCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.2fV".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class TimingAdvanceCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410E70", -8f),
            arrayOf("410E00", -64f),
            arrayOf("410EFF", 63.5f),
            arrayOf("410EFFFF", 63.5f)
        )
    }

    @Test
    fun `test valid timing advance responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = TimingAdvanceCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.2f°".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class VINCommandParameterizedTests(private val rawValue: String, private val expected: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // CAN (ISO-15765) format
            arrayOf("0140:4902013933591:425352375248452:4A323938313136", "93YBSR7RHEJ298116"),
            arrayOf("0140:4902015750301:5A5A5A39395A542:53333932313234", "WP0ZZZ99ZTS392124"),
            // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) format
            arrayOf("490201000000394902023359425349020352375248490204454A323949020538313136", "93YBSR7RHEJ298116"),
            arrayOf("4902010000005749020250305A5A4902035A39395A4902045453333949020532313234", "WP0ZZZ99ZTS392124"),
            arrayOf("014 0: 49 02 01 39 42 47 1: 4B 54 34 38 56 30 4A 2: 47 31 34 31 38 30 39", "9BGKT48V0JG141809")
        )
    }

    @Test
    fun `test valid vin responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = VINCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals(expected, obdResponse.formattedValue)
    }
}