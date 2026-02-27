package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatHex
import kotlin.test.Test
import kotlin.test.assertEquals

private val AVAILABLE_PIDS_01_TO_20_VALUES =
    listOf(
        // Renault Sandero 2014
        Pair(
            "BE3EB811",
            intArrayOf(
                0x1,
                0x3,
                0x4,
                0x5,
                0x6,
                0x7,
                0xb,
                0xc,
                0xd,
                0xe,
                0xf,
                0x11,
                0x13,
                0x14,
                0x15,
                0x1c,
                0x20,
            ),
        ),
        // Chevrolet Onix 2015
        Pair(
            "BE3FB813",
            intArrayOf(
                0x1,
                0x3,
                0x4,
                0x5,
                0x6,
                0x7,
                0xb,
                0xc,
                0xd,
                0xe,
                0xf,
                0x10,
                0x11,
                0x13,
                0x14,
                0x15,
                0x1c,
                0x1f,
                0x20,
            ),
        ),
        // Toyota Corolla 2015
        Pair(
            "BE1FA813",
            intArrayOf(
                0x1,
                0x3,
                0x4,
                0x5,
                0x6,
                0x7,
                0xc,
                0xd,
                0xe,
                0xf,
                0x10,
                0x11,
                0x13,
                0x15,
                0x1c,
                0x1f,
                0x20,
            ),
        ),
        // Fiat Siena 2011
        Pair(
            "BE3EB811",
            intArrayOf(
                0x1,
                0x3,
                0x4,
                0x5,
                0x6,
                0x7,
                0xb,
                0xc,
                0xd,
                0xe,
                0xf,
                0x11,
                0x13,
                0x14,
                0x15,
                0x1c,
                0x20,
            ),
        ),
        // VW Gol 2014
        Pair(
            "BE3EB813",
            intArrayOf(
                0x1,
                0x3,
                0x4,
                0x5,
                0x6,
                0x7,
                0xb,
                0xc,
                0xd,
                0xe,
                0xf,
                0x11,
                0x13,
                0x14,
                0x15,
                0x1c,
                0x1f,
                0x20,
            ),
        ),
        // Empty
        Pair("00000000", intArrayOf()),
        // Complete
        Pair("FFFFFFFF", (0x1..0x20).toList().toIntArray()),
    )

class AvailablePIDsCommand01to20Tests {
    @Test
    fun `test valid available PIDs 01 to 20 responses handler`() {
        AVAILABLE_PIDS_01_TO_20_VALUES.forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_01_TO_20).run {
                    handleResponse(rawResponse)
                }
            assertEquals(
                expected.joinToString(",") { formatHex(it) },
                obdResponse.formattedValue,
                "Failed for: $rawValue",
            )
        }
    }
}

class AvailablePIDsCommand21to40Tests {
    @Test
    fun `test valid available PIDs 21 to 40 responses handler`() {
        listOf(
            // Renault Sandero 2014
            Pair("80018001", intArrayOf(0x21, 0x30, 0x31, 0x40)),
            // Chevrolet Onix 2015
            Pair(
                "8007A011",
                intArrayOf(0x21, 0x2e, 0x2f, 0x30, 0x31, 0x33, 0x3c, 0x40),
            ),
            // Toyota Corolla 2015
            Pair(
                "9005B015",
                intArrayOf(0x21, 0x24, 0x2e, 0x30, 0x31, 0x33, 0x34, 0x3c, 0x3e, 0x40),
            ),
            // Fiat Siena 2011
            Pair("80000000", intArrayOf(0x21)),
            // VW Gol 2014
            Pair(
                "8007A011",
                intArrayOf(0x21, 0x2e, 0x2f, 0x30, 0x31, 0x33, 0x3c, 0x40),
            ),
            // Empty
            Pair("00000000", intArrayOf()),
            // Complete
            Pair("FFFFFFFF", (0x21..0x40).toList().toIntArray()),
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_21_TO_40).run {
                    handleResponse(rawResponse)
                }
            assertEquals(
                expected.joinToString(",") { formatHex(it) },
                obdResponse.formattedValue,
                "Failed for: $rawValue",
            )
        }
    }
}

class AvailablePIDsCommand41to60Tests {
    @Test
    fun `test valid available PIDs 41 to 60 responses handler`() {
        listOf(
            // Renault Sandero 2014
            Pair("80000000", intArrayOf(0x41)),
            // Chevrolet Onix 2015
            Pair(
                "FED0C000",
                intArrayOf(0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x49, 0x4a, 0x4c, 0x51, 0x52),
            ),
            // Toyota Corolla 2015
            Pair(
                "7ADC8001",
                intArrayOf(0x42, 0x43, 0x44, 0x45, 0x47, 0x49, 0x4a, 0x4c, 0x4d, 0x4e, 0x51, 0x60),
            ),
            // VW Gol 2014
            Pair(
                "FED14400",
                intArrayOf(0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x49, 0x4a, 0x4c, 0x50, 0x52, 0x56),
            ),
            // Empty
            Pair("00000000", intArrayOf()),
            // Complete
            Pair("FFFFFFFF", (0x41..0x60).toList().toIntArray()),
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_41_TO_60).run {
                    handleResponse(rawResponse)
                }
            assertEquals(
                expected.joinToString(",") { formatHex(it) },
                obdResponse.formattedValue,
                "Failed for: $rawValue",
            )
        }
    }
}

class AvailablePIDsCommand61to80Tests {
    @Test
    fun `test valid available PIDs 61 to 80 responses handler`() {
        listOf(
            // Toyota Corolla 2015
            Pair("08000000", intArrayOf(0x65)),
            // Empty
            Pair("00000000", intArrayOf()),
            // Complete
            Pair("FFFFFFFF", (0x61..0x80).toList().toIntArray()),
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_61_TO_80).run {
                    handleResponse(rawResponse)
                }
            assertEquals(
                expected.joinToString(",") { formatHex(it) },
                obdResponse.formattedValue,
                "Failed for: $rawValue",
            )
        }
    }
}

class AvailablePIDsCommand81toA0Tests {
    @Test
    fun `test valid available PIDs 81 to A0 responses handler`() {
        listOf(
            // Empty
            Pair("00000000", intArrayOf()),
            // Complete
            Pair("FFFFFFFF", (0x81..0xA0).toList().toIntArray()),
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_81_TO_A0).run {
                    handleResponse(rawResponse)
                }
            assertEquals(
                expected.joinToString(",") { formatHex(it) },
                obdResponse.formattedValue,
                "Failed for: $rawValue",
            )
        }
    }
}
