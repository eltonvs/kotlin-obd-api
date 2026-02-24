package com.github.eltonvs.obd.command

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class BytesToIntParameterizedTests(
    private val bufferedValue: IntArray,
    private val start: Int,
    private val bytesToProcess: Int,
    private val expected: Long,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>(intArrayOf(0x0), 0, -1, 0),
                arrayOf<Any>(intArrayOf(0x1), 0, -1, 1),
                arrayOf<Any>(intArrayOf(0x1), 0, 1, 1),
                arrayOf<Any>(intArrayOf(0x10), 0, -1, 16),
                arrayOf<Any>(intArrayOf(0x11), 0, -1, 17),
                arrayOf<Any>(intArrayOf(0xFF), 0, -1, 255),
                arrayOf<Any>(intArrayOf(0xFF, 0xFF), 0, -1, 65535),
                arrayOf<Any>(intArrayOf(0xFF, 0xFF), 0, 1, 255),
                arrayOf<Any>(intArrayOf(0xFF, 0x00), 0, -1, 65280),
                arrayOf<Any>(intArrayOf(0xFF, 0x00), 0, 1, 255),
                arrayOf<Any>(intArrayOf(0x41, 0x0D, 0x40), 2, -1, 64),
                arrayOf<Any>(intArrayOf(0x41, 0x0D, 0x40, 0xFF), 2, 1, 64),
            )
    }

    @Test
    fun `test valid results for bytesToInt`() {
        val result = bytesToInt(bufferedValue, start = start, bytesToProcess = bytesToProcess)
        assertEquals(expected, result)
    }
}
