package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class AvailablePIDsCommand01to20ParameterizedTests(private val rawValue: String, private val expected: IntArray) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Renault Sandero 2014
            arrayOf(
                "BE3EB811",
                intArrayOf(0x1, 0x3, 0x4, 0x5, 0x6, 0x7, 0xb, 0xc, 0xd, 0xe, 0xf, 0x11, 0x13, 0x14, 0x15, 0x1c, 0x20)
            ),
            // Chevrolet Onix 2015
            arrayOf(
                "BE3FB813",
                intArrayOf(
                    0x1, 0x3, 0x4, 0x5, 0x6, 0x7, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x13, 0x14, 0x15, 0x1c, 0x1f,
                    0x20
                )
            ),
            // Toyota Corolla 2015
            arrayOf(
                "BE1FA813",
                intArrayOf(0x1, 0x3, 0x4, 0x5, 0x6, 0x7, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11, 0x13, 0x15, 0x1c, 0x1f, 0x20)
            ),
            // Fiat Siena 2011
            arrayOf(
                "BE3EB811",
                intArrayOf(0x1, 0x3, 0x4, 0x5, 0x6, 0x7, 0xb, 0xc, 0xd, 0xe, 0xf, 0x11, 0x13, 0x14, 0x15, 0x1c, 0x20)
            ),
            // VW Gol 2014
            arrayOf(
                "BE3EB813",
                intArrayOf(
                    0x1, 0x3, 0x4, 0x5, 0x6, 0x7, 0xb, 0xc, 0xd, 0xe, 0xf, 0x11, 0x13, 0x14, 0x15, 0x1c, 0x1f, 0x20
                )
            ),
            // Empty
            arrayOf("00000000", intArrayOf()),
            // Complete
            arrayOf("FFFFFFFF", (0x1..0x20).toList().toIntArray())
        )
    }

    @Test
    fun `test valid available PIDs 01 to 20 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_01_TO_20).run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(",") { "%02X".format(it) }, obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class AvailablePIDsCommand21to40ParameterizedTests(private val rawValue: String, private val expected: IntArray) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Renault Sandero 2014
            arrayOf("80018001", intArrayOf(0x21, 0x30, 0x31, 0x40)),
            // Chevrolet Onix 2015
            arrayOf("8007A011", intArrayOf(0x21, 0x2e, 0x2f, 0x30, 0x31, 0x33, 0x3c, 0x40)),
            // Toyota Corolla 2015
            arrayOf("9005B015", intArrayOf(0x21, 0x24, 0x2e, 0x30, 0x31, 0x33, 0x34, 0x3c, 0x3e, 0x40)),
            // Fiat Siena 2011
            arrayOf("80000000", intArrayOf(0x21)),
            // VW Gol 2014
            arrayOf("8007A011", intArrayOf(0x21, 0x2e, 0x2f, 0x30, 0x31, 0x33, 0x3c, 0x40)),
            // Empty
            arrayOf("00000000", intArrayOf()),
            // Complete
            arrayOf("FFFFFFFF", (0x21..0x40).toList().toIntArray())
        )
    }

    @Test
    fun `test valid available PIDs 21 to 40 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_21_TO_40).run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(",") { "%02X".format(it) }, obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class AvailablePIDsCommand41to60ParameterizedTests(private val rawValue: String, private val expected: IntArray) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Renault Sandero 2014
            arrayOf("80000000", intArrayOf(0x41)),
            // Chevrolet Onix 2015
            arrayOf("FED0C000", intArrayOf(0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x49, 0x4a, 0x4c, 0x51, 0x52)),
            // Toyota Corolla 2015
            arrayOf("7ADC8001", intArrayOf(0x42, 0x43, 0x44, 0x45, 0x47, 0x49, 0x4a, 0x4c, 0x4d, 0x4e, 0x51, 0x60)),
            // VW Gol 2014
            arrayOf(
                "FED14400",
                intArrayOf(0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x49, 0x4a, 0x4c, 0x50, 0x52, 0x56)
            ),
            // Empty
            arrayOf("00000000", intArrayOf()),
            // Complete
            arrayOf("FFFFFFFF", (0x41..0x60).toList().toIntArray())
        )
    }

    @Test
    fun `test valid available PIDs 41 to 60 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_41_TO_60).run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(",") { "%02X".format(it) }, obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class AvailablePIDsCommand61to80ParameterizedTests(private val rawValue: String, private val expected: IntArray) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Toyota Corolla 2015
            arrayOf("08000000", intArrayOf(0x65)),
            // Empty
            arrayOf("00000000", intArrayOf()),
            // Complete
            arrayOf("FFFFFFFF", (0x61..0x80).toList().toIntArray())
        )
    }

    @Test
    fun `test valid available PIDs 61 to 80 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_61_TO_80).run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(",") { "%02X".format(it) }, obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class AvailablePIDsCommand81toA0ParameterizedTests(private val rawValue: String, private val expected: IntArray) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Empty
            arrayOf("00000000", intArrayOf()),
            // Complete
            arrayOf("FFFFFFFF", (0x81..0xA0).toList().toIntArray())
        )
    }

    @Test
    fun `test valid available PIDs 81 to A0 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_81_TO_A0).run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(",") { "%02X".format(it) }, obdResponse.formattedValue)
    }
}