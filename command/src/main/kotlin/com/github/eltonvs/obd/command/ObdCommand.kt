package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.domain.ObdRawResponse
import com.github.eltonvs.obd.domain.ObdResponse

interface ObdCommand {
    val name: String
    val mode: String
    val pid: String

    fun handle(rawResponse: ObdRawResponse): ObdResponse =
        ObdResponse(
            command = this,
            rawResponse = rawResponse,
            value = rawResponse.value
        )
    fun format(response: ObdResponse): String = "${response.value}${response.unit}"
}