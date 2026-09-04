package com.github.eltonvs.obd.command

import kotlin.math.abs
import kotlin.math.floor

private const val DECIMAL_RADIX = 10L
private const val HEX_RADIX = 16
private const val HEX_WIDTH = 2

/**
 * Locale-independent float formatting that works across all Kotlin platforms.
 * Replaces JVM-only `"%.Nf".format(value)` and reproduces its rounding: the
 * value is rounded half away from zero on its decimal expansion, so
 * `-93.75f` becomes `"-93.8"` and `9.995f` (really `9.99499988f`) stays `"9.99"`.
 */
fun formatFloat(
    value: Float,
    decimalPlaces: Int,
): String = formatFloat(value.toDouble(), decimalPlaces)

fun formatFloat(
    value: Double,
    decimalPlaces: Int,
): String {
    require(decimalPlaces >= 0) { "decimalPlaces must be >= 0" }
    var divisor = 1L
    repeat(decimalPlaces) { divisor *= DECIMAL_RADIX }

    val rounded = roundHalfAwayFromZero(value * divisor)

    val isNegative = rounded < 0
    val absRounded = abs(rounded)
    val intPart = absRounded / divisor
    val fracPart = absRounded % divisor

    return buildString {
        if (isNegative) append('-')
        append(intPart)
        if (decimalPlaces > 0) {
            append('.')
            append(fracPart.toString().padStart(decimalPlaces, '0'))
        }
    }
}

private fun roundHalfAwayFromZero(value: Double): Long =
    if (value < 0) {
        -floor(-value + 0.5).toLong()
    } else {
        floor(value + 0.5).toLong()
    }

/**
 * Format an integer as a two-digit uppercase hexadecimal string.
 * Replaces JVM-only `"%02X".format(value)`.
 */
fun formatHex(value: Int): String = value.toString(HEX_RADIX).uppercase().padStart(HEX_WIDTH, '0')
