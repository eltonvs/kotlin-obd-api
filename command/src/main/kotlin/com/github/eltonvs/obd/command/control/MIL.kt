package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.domain.ObdResponse

private fun calculate(rawValue: String): Int {
    val a = rawValue[2].toInt()
    val b = rawValue[3].toInt()
    return a * 256 + b
}

class MILOnCommand : ObdCommand() {
    override val tag = "MIL_ON"
    override val name = "MIL on"
    override val mode = "01"
    override val pid = "01"

    override val handler = { rawValue: String ->
        val mil = rawValue[2].toInt()
        val milOn = mil and 0x80 == 128
        milOn.toString()
    }

    override fun format(response: ObdResponse): String {
        val milOn = response.value.toBoolean()
        return "MIL is ${if (milOn) "ON" else "OFF"}"
    }
}

class DistanceMILOnCommand : ObdCommand() {
    override val tag = "DISTANCE_TRAVELED_MIL_ON"
    override val name = "Distance traveled with MIL on"
    override val mode = "01"
    override val pid = "21"

    override val defaultUnit = "Km"
    override val handler = { x: String -> calculate(x).toString() }
}

class TimeSinceMILOnCommand : ObdCommand() {
    override val tag = "TIME_TRAVELED_MIL_ON"
    override val name = "Time run with MIL on"
    override val mode = "01"
    override val pid = "4D"

    override val defaultUnit = "min"
    override val handler = { x: String -> calculate(x).toString() }
}