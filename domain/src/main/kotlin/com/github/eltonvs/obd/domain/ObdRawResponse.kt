package com.github.eltonvs.obd.domain

data class ObdRawResponse(
    val rawValue: String,
    val elapsedTime: Long
)