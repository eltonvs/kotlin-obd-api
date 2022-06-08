package com.github.eltonvs.obd.command.engine

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.calculatePercentage


class SpeedCommand : ObdCommand() {
    override val tag = "SPEED"
    override val name = "Vehicle Speed"
    override val mode = "01"
    override val pid = "0D"

    override val defaultUnit = "Km/h"
    override val handler = { it: ObdRawResponse -> bytesToInt(it.bufferedValue, bytesToProcess = 1).toString() }
}

class RPMCommand : ObdCommand() {
    override val tag = "ENGINE_RPM"
    override val name = "Engine RPM"
    override val mode = "01"
    override val pid = "0C"

    override val defaultUnit = "RPM"
    override val handler = { it: ObdRawResponse -> (bytesToInt(it.bufferedValue) / 4).toString() }
}

class MassAirFlowCommand : ObdCommand() {
    override val tag = "MAF"
    override val name = "Mass Air Flow"
    override val mode = "01"
    override val pid = "10"

    override val defaultUnit = "g/s"
    override val handler = { it: ObdRawResponse -> "%.2f".format(bytesToInt(it.bufferedValue) / 100f) }
}

class RuntimeCommand : ObdCommand() {
    override val tag = "ENGINE_RUNTIME"
    override val name = "Engine Runtime"
    override val mode = "01"
    override val pid = "0F"

    override val handler = { it: ObdRawResponse -> parseRuntime(it.bufferedValue) }

    private fun parseRuntime(rawValue: IntArray): String {
        val seconds = bytesToInt(rawValue)
        val hh = seconds / 3600
        val mm = (seconds % 3600) / 60
        val ss = seconds % 60
        return listOf(hh, mm, ss).joinToString(":") { it.toString().padStart(2, '0') }
    }
}

class LoadCommand : ObdCommand() {
    override val tag = "ENGINE_LOAD"
    override val name = "Engine Load"
    override val mode = "01"
    override val pid = "04"

    override val defaultUnit = "%"
    override val handler = { it: ObdRawResponse -> "%.1f".format(calculatePercentage(it.bufferedValue, bytesToProcess = 1)) }
}

class AbsoluteLoadCommand : ObdCommand() {
    override val tag = "ENGINE_ABSOLUTE_LOAD"
    override val name = "Engine Absolute Load"
    override val mode = "01"
    override val pid = "43"

    override val defaultUnit = "%"
    override val handler = { it: ObdRawResponse -> "%.1f".format(calculatePercentage(it.bufferedValue)) }
}

class ThrottlePositionCommand : ObdCommand() {
    override val tag = "THROTTLE_POSITION"
    override val name = "Throttle Position"
    override val mode = "01"
    override val pid = "11"

    override val defaultUnit = "%"
    override val handler = { it: ObdRawResponse -> "%.1f".format(calculatePercentage(it.bufferedValue, bytesToProcess = 1)) }
}

class RelativeThrottlePositionCommand : ObdCommand() {
    override val tag = "RELATIVE_THROTTLE_POSITION"
    override val name = "Relative Throttle Position"
    override val mode = "01"
    override val pid = "45"

    override val defaultUnit = "%"
    override val handler = { it: ObdRawResponse -> "%.1f".format(calculatePercentage(it.bufferedValue, bytesToProcess = 1)) }
}