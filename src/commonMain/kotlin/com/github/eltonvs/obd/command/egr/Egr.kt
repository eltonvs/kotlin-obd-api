package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.calculatePercentage
import com.github.eltonvs.obd.command.formatFloat

private const val SINGLE_BYTE = 1
private const val HUNDRED_PERCENT = 100f
private const val HALF_SCALE = 128f

class CommandedEgrCommand : ObdCommand() {
    override val tag = "COMMANDED_EGR"
    override val name = "Commanded EGR"
    override val mode = "01"
    override val pid = "2C"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        formatFloat(calculatePercentage(response.bufferedValue, bytesToProcess = SINGLE_BYTE), 1)
    }
}

class EgrErrorCommand : ObdCommand() {
    override val tag = "EGR_ERROR"
    override val name = "EGR Error"
    override val mode = "01"
    override val pid = "2D"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        val value = bytesToInt(response.bufferedValue, bytesToProcess = SINGLE_BYTE)
        val normalized = value * (HUNDRED_PERCENT / HALF_SCALE) - HUNDRED_PERCENT
        formatFloat(normalized, 1)
    }
}
