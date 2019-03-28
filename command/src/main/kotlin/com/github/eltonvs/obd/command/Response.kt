package com.github.eltonvs.obd.command


data class ObdRawResponse(
    val value: String,
    val elapsedTime: Long
) {
    val bufferedValue: IntArray
        get() = value.chunked(2) { it.toString().toInt(radix = 16) }.toIntArray()
}

data class ObdResponse(
    val command: ObdCommand,
    val rawResponse: ObdRawResponse,
    val value: String,
    val unit: String = ""
) {
    val formattedValue: String get() = command.format(this)
}