package com.github.eltonvs.obd

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/** Locale-independent float formatting for test assertions. */
internal fun formatFloat(
    value: Float,
    decimalPlaces: Int,
): String {
    val multiplier = 10.0.pow(decimalPlaces).toFloat()
    val rounded = (value * multiplier).roundToInt() / multiplier

    val isNegative = rounded < 0
    val absValue = abs(rounded)

    val intPart = absValue.toInt()
    val fracPart = ((absValue - intPart) * multiplier).roundToInt()

    return buildString {
        if (isNegative) append('-')
        append(intPart)
        append('.')
        append(fracPart.toString().padStart(decimalPlaces, '0').take(decimalPlaces))
    }
}

/** Format hex value with zero padding. */
internal fun formatHex(value: Int): String {
    val hex = value.toString(16).uppercase()
    return if (hex.length < 2) "0$hex" else hex
}
