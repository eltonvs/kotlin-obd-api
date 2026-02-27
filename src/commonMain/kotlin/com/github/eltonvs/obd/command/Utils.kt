package com.github.eltonvs.obd.command

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Locale-independent float formatting that works across all Kotlin platforms.
 * Replaces JVM-only `"%.Nf".format(value)`.
 */
fun formatFloat(
    value: Float,
    decimalPlaces: Int,
): String {
    val multiplier = 10f.pow(decimalPlaces)
    val rounded = (value * multiplier).roundToLong() / multiplier.toDouble()

    val isNegative = rounded < 0
    val absValue = abs(rounded)

    val intPart = absValue.toLong()
    val fracPart = ((absValue - intPart) * multiplier).roundToInt()

    return buildString {
        if (isNegative) append('-')
        append(intPart)
        if (decimalPlaces > 0) {
            append('.')
            append(fracPart.toString().padStart(decimalPlaces, '0').take(decimalPlaces))
        }
    }
}

/**
 * Format an integer as a two-digit uppercase hexadecimal string.
 * Replaces JVM-only `"%02X".format(value)`.
 */
fun formatHex(value: Int): String {
    val hex = value.toString(16).uppercase()
    return hex.padStart(2, '0')
}
