package com.github.eltonvs.obd.command


abstract class ObdCommand {
    abstract val tag: String
    abstract val name: String
    abstract val mode: String
    abstract val pid: String

    open val defaultUnit: String = ""
    open val skipDigitCheck: Boolean = false
    open val handler: (ObdRawResponse) -> String = { it.value }

    val rawCommand: String
        get() = listOf(mode, pid).joinToString(" ")

    fun handleResponse(rawResponse: ObdRawResponse): ObdResponse {
        val checkedRawResponse = BadResponseException.checkForExceptions(this, rawResponse)
        return ObdResponse(
            command = this,
            rawResponse = checkedRawResponse,
            value = handler(checkedRawResponse),
            unit = defaultUnit
        )
    }

    open fun format(response: ObdResponse): String = "${response.value}${response.unit}"
}