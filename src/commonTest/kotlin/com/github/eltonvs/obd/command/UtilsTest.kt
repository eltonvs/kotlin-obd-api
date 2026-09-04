package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test
    fun `formatFloat rounds half away from zero`() {
        // Only values that are exactly representable as Float, because Kotlin/JS
        // models Float as a Double; see UtilsJvmTest for the single-precision cases.
        val cases =
            listOf(
                Triple(-93.75f, 1, "-93.8"),
                Triple(93.75f, 1, "93.8"),
                Triple(-1.25f, 1, "-1.3"),
                Triple(0.5f, 0, "1"),
                Triple(1f, 0, "1"),
                Triple(0.125f, 2, "0.13"),
                Triple(76.5625f, 2, "76.56"),
                Triple(123.5f, 0, "124"),
                Triple(12.25f, 2, "12.25"),
                Triple(10f, 3, "10.000"),
                Triple(0f, 2, "0.00"),
            )
        for ((value, decimals, expected) in cases) {
            assertEquals(expected, formatFloat(value, decimals), "formatFloat($value, $decimals)")
        }
    }

    @Test
    fun `formatFloat keeps Double precision`() {
        // 0x10E3 * 0.05 = 216.15 as a Double; truncating to Float gave 216.1499939 -> "216.1"
        assertEquals("216.2", formatFloat(0x10E3 * 0.05, 1))
        assertEquals("3276.8", formatFloat(0xFFFF * 0.05, 1))
    }

    @Test
    fun `formatHex pads to two uppercase digits`() {
        assertEquals("00", formatHex(0))
        assertEquals("0A", formatHex(10))
        assertEquals("FF", formatHex(255))
        assertEquals("123", formatHex(0x123))
    }
}
