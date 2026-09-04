package com.github.eltonvs.obd.command

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

/** formatFloat replaced `"%.Nf".format(...)`; on the JVM it must agree with it. */
class UtilsJvmTest {
    @Test
    fun `formatFloat agrees with String format on single precision ties`() {
        listOf(9.995f to 2, 2.675f to 2, 1.005f to 2, 0.045f to 2, 14.65f to 1, 216.15f to 1).forEach { (value, decimals) ->
            assertEquals(String.format(Locale.ROOT, "%.${decimals}f", value), formatFloat(value, decimals), "$value")
        }
    }

    @Test
    fun `formatFloat agrees with String format for every fuel trim value`() {
        for (raw in 0..0xFF) {
            val value = (raw - 128) * 100 / 128f
            assertEquals(String.format(Locale.ROOT, "%.1f", value), formatFloat(value, 1), "raw=$raw")
        }
    }

    @Test
    fun `formatFloat agrees with String format for every module voltage value`() {
        for (raw in 0..0xFFFF) {
            val value = raw / 1000f
            assertEquals(String.format(Locale.ROOT, "%.2f", value), formatFloat(value, 2), "raw=$raw")
        }
    }

    @Test
    fun `formatFloat agrees with String format for every fuel consumption value`() {
        for (raw in 0..0xFFFF) {
            val value = raw * 0.05
            assertEquals(String.format(Locale.ROOT, "%.1f", value), formatFloat(value, 1), "raw=$raw")
        }
    }
}
