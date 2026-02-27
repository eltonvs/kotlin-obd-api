package com.github.eltonvs.obd.command.engine

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.calculatePercentage
import com.github.eltonvs.obd.command.formatFloat

private const val SINGLE_BYTE = 1
private const val RPM_DIVISOR = 4
private const val MAF_DIVISOR = 100f
private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60
private const val TIME_PADDING = 2
private const val PAD_CHAR = '0'

class SpeedCommand : ObdCommand() {
    override val tag = "SPEED"
    override val name = "Vehicle Speed"
    override val mode = "01"
    override val pid = "0D"

    override val defaultUnit = "Km/h"
    override val handler = { response: ObdRawResponse ->
        bytesToInt(response.bufferedValue, bytesToProcess = SINGLE_BYTE).toString()
    }
}

class RPMCommand : ObdCommand() {
    override val tag = "ENGINE_RPM"
    override val name = "Engine RPM"
    override val mode = "01"
    override val pid = "0C"

    override val defaultUnit = "RPM"
    override val handler = { response: ObdRawResponse -> (bytesToInt(response.bufferedValue) / RPM_DIVISOR).toString() }
}

class MassAirFlowCommand : ObdCommand() {
    override val tag = "MAF"
    override val name = "Mass Air Flow"
    override val mode = "01"
    override val pid = "10"

    override val defaultUnit = "g/s"
    override val handler = { response: ObdRawResponse ->
        val value = bytesToInt(response.bufferedValue) / MAF_DIVISOR
        formatFloat(value, 2)
    }
}

class RuntimeCommand : ObdCommand() {
    override val tag = "ENGINE_RUNTIME"
    override val name = "Engine Runtime"
    override val mode = "01"
    override val pid = "0F"

    override val handler = { response: ObdRawResponse -> parseRuntime(response.bufferedValue) }

    private fun parseRuntime(rawValue: IntArray): String {
        val seconds = bytesToInt(rawValue)
        val hh = seconds / SECONDS_PER_HOUR
        val mm = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val ss = seconds % SECONDS_PER_MINUTE
        return listOf(hh, mm, ss).joinToString(":") { it.toString().padStart(TIME_PADDING, PAD_CHAR) }
    }
}

class LoadCommand : ObdCommand() {
    override val tag = "ENGINE_LOAD"
    override val name = "Engine Load"
    override val mode = "01"
    override val pid = "04"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        formatFloat(calculatePercentage(response.bufferedValue, bytesToProcess = SINGLE_BYTE), 1)
    }
}

class AbsoluteLoadCommand : ObdCommand() {
    override val tag = "ENGINE_ABSOLUTE_LOAD"
    override val name = "Engine Absolute Load"
    override val mode = "01"
    override val pid = "43"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse -> formatFloat(calculatePercentage(response.bufferedValue), 1) }
}

class ThrottlePositionCommand : ObdCommand() {
    override val tag = "THROTTLE_POSITION"
    override val name = "Throttle Position"
    override val mode = "01"
    override val pid = "11"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        formatFloat(calculatePercentage(response.bufferedValue, bytesToProcess = SINGLE_BYTE), 1)
    }
}

class RelativeThrottlePositionCommand : ObdCommand() {
    override val tag = "RELATIVE_THROTTLE_POSITION"
    override val name = "Relative Throttle Position"
    override val mode = "01"
    override val pid = "45"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        formatFloat(calculatePercentage(response.bufferedValue, bytesToProcess = SINGLE_BYTE), 1)
    }
}
