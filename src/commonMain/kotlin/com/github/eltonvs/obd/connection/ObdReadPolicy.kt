package com.github.eltonvs.obd.connection

internal data class ObdReadPolicy(
    val responseTimeoutMs: Long,
    val interByteTimeoutMs: Long,
) {
    init {
        require(responseTimeoutMs >= 0) { "responseTimeoutMs must be >= 0" }
        require(interByteTimeoutMs >= 0) { "interByteTimeoutMs must be >= 0" }
    }
}
