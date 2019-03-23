package com.github.eltonvs.obd.command.engine

import com.github.eltonvs.obd.command.ObdCommand


private fun calculate(rawValue: String): Int {
    val a = rawValue[2].toInt()
    val b = rawValue[3].toInt()
    return a * 256 + b
}

class SpeedCommand : ObdCommand() {
    override val tag = "SPEED"
    override val name = "Vehicle Speed"
    override val mode = "01"
    override val pid = "0D"

    override val defaultUnit = "Km/h"
    override val handler = { x: String -> x[2].toInt().toString() }
}

class RPMCommand : ObdCommand() {
    override val tag = "ENGINE_RPM"
    override val name = "Engine RPM"
    override val mode = "01"
    override val pid = "0C"

    override val defaultUnit = "RPM"
    override val handler = { x: String -> (calculate(x) / 4).toString() }
}

class MassAirFlowCommand : ObdCommand() {
    override val tag = "MAF"
    override val name = "Mass Air Flow"
    override val mode = "01"
    override val pid = "10"

    override val defaultUnit = "g/s"
    override val handler = { x: String -> "%.2f".format(calculate(x) / 100f) }
}

class RuntimeCommand : ObdCommand() {
    override val tag = "ENGINE_RUNTIME"
    override val name = "Engine Runtime"
    override val mode = "01"
    override val pid = "0F"

    override val handler = ::parseRuntime

    private fun parseRuntime(rawValue: String): String {
        val seconds = calculate(rawValue)
        val hh = seconds / 3600
        val mm = (seconds % 3600) / 60
        val ss = seconds % 60
        return "$hh:$mm:$ss"
    }
}