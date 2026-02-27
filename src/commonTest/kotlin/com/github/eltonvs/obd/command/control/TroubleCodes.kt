package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.NoDataException
import com.github.eltonvs.obd.command.ObdRawResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DTCNumberCommandTests {
    @Test
    fun `test valid DTC number responses handler`() {
        listOf(
            "410100452100" to 0,
            "410100000000" to 0,
            "41017F000000" to 127,
            "410123456789" to 35,
            "41017FFFFFFF" to 127,
            "410180000000" to 0,
            "410180FFFFFF" to 0,
            "410189ABCDEF" to 9,
            "4101FFFFFFFF" to 127,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                DTCNumberCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("$expected codes", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class DistanceSinceCodesClearedCommandTests {
    @Test
    fun `test valid distance since codes cleared responses handler`() {
        listOf(
            "4131F967" to 63_847,
            "41310000" to 0,
            "4131FFFF" to 65_535,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                DistanceSinceCodesClearedCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}Km", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class TimeSinceCodesClearedCommandTests {
    @Test
    fun `test valid time since codes cleared responses handler`() {
        listOf(
            "414E4543" to 17_731,
            "414E0000" to 0,
            "414EFFFF" to 65_535,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                TimeSinceCodesClearedCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals("${expected}min", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class TroubleCodesCommandTests {
    @Test
    fun `test valid trouble codes responses handler`() {
        listOf(
            // Two frames with four dtc
            "4300035104A1AB\r43F10600000000" to listOf("P0003", "C1104", "B21AB", "U3106"),
            // One frame with three dtc
            "43010301040105" to listOf("P0103", "P0104", "P0105"),
            // One frame with two dtc
            "43010301040000" to listOf("P0103", "P0104"),
            // Two frames with four dtc CAN (ISO-15765) format
            "00A\r0:430401080118\r1:011901200000" to listOf("P0108", "P0118", "P0119", "P0120"),
            // One frame with two dtc CAN (ISO-15765) format
            "430201200121" to listOf("P0120", "P0121"),
            // Empty data
            "4300" to listOf(),
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                TroubleCodesCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(expected.joinToString(separator = ","), obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test trouble codes no data response`() {
        assertFailsWith<NoDataException> {
            val rawResponse = ObdRawResponse(value = "43NODATA", elapsedTime = 0)
            TroubleCodesCommand().run {
                handleResponse(rawResponse)
            }
        }
    }
}

class PendingTroubleCodesCommandTests {
    @Test
    fun `test valid pending trouble codes responses handler`() {
        listOf(
            // Two frames with four dtc
            "4700035104A1AB\r47F10600000000" to listOf("P0003", "C1104", "B21AB", "U3106"),
            // One frame with three dtc
            "47010301040105" to listOf("P0103", "P0104", "P0105"),
            // One frame with two dtc
            "47010301040000" to listOf("P0103", "P0104"),
            // Two frames with four dtc CAN (ISO-15765) format
            "00A\r0:470401080118\r1:011901200000" to listOf("P0108", "P0118", "P0119", "P0120"),
            // One frame with two dtc CAN (ISO-15765) format
            "470201200121" to listOf("P0120", "P0121"),
            // Empty data
            "4700" to listOf(),
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                PendingTroubleCodesCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(expected.joinToString(separator = ","), obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test pending trouble codes no data response`() {
        assertFailsWith<NoDataException> {
            val rawResponse = ObdRawResponse(value = "47NODATA", elapsedTime = 0)
            PendingTroubleCodesCommand().run {
                handleResponse(rawResponse)
            }
        }
    }
}

class PermanentTroubleCodesCommandTests {
    @Test
    fun `test valid permanent trouble codes responses handler`() {
        listOf(
            // Two frames with four dtc
            "4A00035104A1AB\r4AF10600000000" to listOf("P0003", "C1104", "B21AB", "U3106"),
            // One frame with three dtc
            "4A010301040105" to listOf("P0103", "P0104", "P0105"),
            // One frame with two dtc
            "4A010301040000" to listOf("P0103", "P0104"),
            // Two frames with four dtc CAN (ISO-15765) format
            "00A\r0:4A0401080118\r1:011901200000" to listOf("P0108", "P0118", "P0119", "P0120"),
            // One frame with two dtc CAN (ISO-15765) format
            "4A0201200121" to listOf("P0120", "P0121"),
            // Empty data
            "4A00" to listOf(),
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                PermanentTroubleCodesCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(expected.joinToString(separator = ","), obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test permanent trouble codes no data response`() {
        assertFailsWith<NoDataException> {
            val rawResponse = ObdRawResponse(value = "4ANODATA", elapsedTime = 0)
            PermanentTroubleCodesCommand().run {
                handleResponse(rawResponse)
            }
        }
    }
}
