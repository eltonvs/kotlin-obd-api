package com.github.eltonvs.obd.domain

data class ObdRawResponse(
    val value: String,
    val elapsedTime: Long
) {
    val bufferedValue: IntArray
        get() = value.chunked(2) { it.toString().toInt(radix = 16) }.toIntArray()
}