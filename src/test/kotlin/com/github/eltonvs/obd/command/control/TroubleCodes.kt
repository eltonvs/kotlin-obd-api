package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.NoDataException
import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class DTCNumberCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410100452100", 0),
            arrayOf("410100000000", 0),
            arrayOf("41017F000000", 127),
            arrayOf("410123456789", 35),
            arrayOf("41017FFFFFFF", 127),
            arrayOf("410180000000", 0),
            arrayOf("410180FFFFFF", 0),
            arrayOf("410189ABCDEF", 9),
            arrayOf("4101FFFFFFFF", 127)
        )
    }

    @Test
    fun `test valid DTC number responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = DTCNumberCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("$expected codes", obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class DistanceSinceCodesClearedCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("4131F967", 63_847),
            arrayOf("41310000", 0),
            arrayOf("4131FFFF", 65_535)
        )
    }

    @Test
    fun `test valid distance since codes cleared responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = DistanceSinceCodesClearedCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}Km", obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class TimeSinceCodesClearedCommandParameterizedTests(private val rawValue: String, private val expected: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("414E4543", 17_731),
            arrayOf("414E0000", 0),
            arrayOf("414EFFFF", 65_535)
        )
    }

    @Test
    fun `test valid time since codes cleared responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = TimeSinceCodesClearedCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("${expected}min", obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class TroubleCodesCommandsParameterizedTests(private val rawValue: String, private val expected: List<String>) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Two frames with four dtc
            arrayOf("4300035104A1AB\r43F10600000000", listOf("P0003", "C1104", "B21AB", "U3106")),
            // One frame with three dtc
            arrayOf("43010301040105", listOf("P0103", "P0104", "P0105")),
            // One frame with two dtc
            arrayOf("43010301040000", listOf("P0103", "P0104")),
            // Two frames with four dtc CAN (ISO-15765) format
            arrayOf("00A\r0:430401080118\r1:011901200000", listOf("P0108", "P0118", "P0119", "P0120")),
            // One frame with two dtc CAN (ISO-15765) format
            arrayOf("430201200121", listOf("P0120", "P0121")),
            // Empty data
            arrayOf("4300", listOf<String>())
        )
    }

    @Test
    fun `test valid trouble codes responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = TroubleCodesCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(separator = ","), obdResponse.formattedValue)
    }
}

class TroubleCodesCommandsTests() {
    @Test(expected = NoDataException::class)
    fun `test trouble codes no data response`() {
        val rawResponse = ObdRawResponse(value = "43NODATA", elapsedTime = 0)
        TroubleCodesCommand().run {
            handleResponse(rawResponse)
        }
    }
}

@RunWith(Parameterized::class)
class PendingTroubleCodesCommandsParameterizedTests(private val rawValue: String, private val expected: List<String>) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Two frames with four dtc
            arrayOf("4700035104A1AB\r47F10600000000", listOf("P0003", "C1104", "B21AB", "U3106")),
            // One frame with three dtc
            arrayOf("47010301040105", listOf("P0103", "P0104", "P0105")),
            // One frame with two dtc
            arrayOf("47010301040000", listOf("P0103", "P0104")),
            // Two frames with four dtc CAN (ISO-15765) format
            arrayOf("00A\r0:470401080118\r1:011901200000", listOf("P0108", "P0118", "P0119", "P0120")),
            // One frame with two dtc CAN (ISO-15765) format
            arrayOf("470201200121", listOf("P0120", "P0121")),
            // Empty data
            arrayOf("4700", listOf<String>())
        )
    }

    @Test
    fun `test valid pending trouble codes responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = PendingTroubleCodesCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(separator = ","), obdResponse.formattedValue)
    }
}

class PendingTroubleCodesCommandsTests() {
    @Test(expected = NoDataException::class)
    fun `test pending trouble codes no data response`() {
        val rawResponse = ObdRawResponse(value = "47NODATA", elapsedTime = 0)
        PendingTroubleCodesCommand().run {
            handleResponse(rawResponse)
        }
    }
}

@RunWith(Parameterized::class)
class PermanentTroubleCodesCommandsParameterizedTests(
    private val rawValue: String,
    private val expected: List<String>
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            // Two frames with four dtc
            arrayOf("4A00035104A1AB\r4AF10600000000", listOf("P0003", "C1104", "B21AB", "U3106")),
            // One frame with three dtc
            arrayOf("4A010301040105", listOf("P0103", "P0104", "P0105")),
            // One frame with two dtc
            arrayOf("4A010301040000", listOf("P0103", "P0104")),
            // Two frames with four dtc CAN (ISO-15765) format
            arrayOf("00A\r0:4A0401080118\r1:011901200000", listOf("P0108", "P0118", "P0119", "P0120")),
            // One frame with two dtc CAN (ISO-15765) format
            arrayOf("4A0201200121", listOf("P0120", "P0121")),
            // Empty data
            arrayOf("4A00", listOf<String>())
        )
    }

    @Test
    fun `test valid permanent trouble codes responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = PermanentTroubleCodesCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals(expected.joinToString(separator = ","), obdResponse.formattedValue)
    }
}

class PermanentTroubleCodesCommandsTests() {
    @Test(expected = NoDataException::class)
    fun `test permanent trouble codes no data response`() {
        val rawResponse = ObdRawResponse(value = "4ANODATA", elapsedTime = 0)
        PermanentTroubleCodesCommand().run {
            handleResponse(rawResponse)
        }
    }
}