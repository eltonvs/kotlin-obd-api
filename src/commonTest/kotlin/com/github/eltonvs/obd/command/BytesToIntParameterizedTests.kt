package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals

class BytesToIntTests {
    @Test
    fun `test valid results for bytesToInt`() {
        listOf(
            Triple(intArrayOf(0x0), 0, -1) to 0L,
            Triple(intArrayOf(0x1), 0, -1) to 1L,
            Triple(intArrayOf(0x1), 0, 1) to 1L,
            Triple(intArrayOf(0x10), 0, -1) to 16L,
            Triple(intArrayOf(0x11), 0, -1) to 17L,
            Triple(intArrayOf(0xFF), 0, -1) to 255L,
            Triple(intArrayOf(0xFF, 0xFF), 0, -1) to 65535L,
            Triple(intArrayOf(0xFF, 0xFF), 0, 1) to 255L,
            Triple(intArrayOf(0xFF, 0x00), 0, -1) to 65280L,
            Triple(intArrayOf(0xFF, 0x00), 0, 1) to 255L,
            Triple(intArrayOf(0x41, 0x0D, 0x40), 2, -1) to 64L,
            Triple(intArrayOf(0x41, 0x0D, 0x40, 0xFF), 2, 1) to 64L,
        ).forEach { (params, expected) ->
            val (bufferedValue, start, bytesToProcess) = params
            val result = bytesToInt(bufferedValue, start = start, bytesToProcess = bytesToProcess)
            assertEquals(expected, result, "Failed for: ${bufferedValue.contentToString()}, start=$start, bytes=$bytesToProcess")
        }
    }
}
