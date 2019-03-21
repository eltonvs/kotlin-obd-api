package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand


private fun calculate(rawValue: String): Int {
    val a = rawValue[2].toInt()
    val b = rawValue[3].toInt()
    return a * 256 + b
}

class DTCNumberCommand : ObdCommand() {
    override val tag = "DTC_NUMBER"
    override val name = "Diagnostic Trouble Codes Number"
    override val mode = "01"
    override val pid = "01"

    override val defaultUnit = " codes"
    override val handler = { rawValue: String ->
        val mil = rawValue[2].toInt()
        val codeCount = mil and 0x7F
        codeCount.toString()
    }
}

class DistanceSinceCodesClearedCommand : ObdCommand() {
    override val tag = "DISTANCE_TRAVELED_AFTER_CODES_CLEARED"
    override val name = "Distance traveled since codes cleared"
    override val mode = "01"
    override val pid = "31"

    override val defaultUnit = "Km"
    override val handler = { x: String -> calculate(x).toString() }
}

class TimeSinceCodesClearedCommand : ObdCommand() {
    override val tag = "TIME_SINCE_CODES_CLEARED"
    override val name = "Time since codes cleared"
    override val mode = "01"
    override val pid = "4E"

    override val defaultUnit = "min"
    override val handler = { x: String -> calculate(x).toString() }
}