package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.bytesToInt

private const val MIL_BYTE_INDEX = 2
private const val MIL_ON_MASK = 0x80

class MILOnCommand : ObdCommand() {
    override val tag = "MIL_ON"
    override val name = "MIL on"
    override val mode = "01"
    override val pid = "01"

    override val handler = { response: ObdRawResponse ->
        val mil = response.bufferedValue[MIL_BYTE_INDEX]
        val milOn = (mil and MIL_ON_MASK) == MIL_ON_MASK
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
    override val handler = { response: ObdRawResponse -> bytesToInt(response.bufferedValue).toString() }
}

class TimeSinceMILOnCommand : ObdCommand() {
    override val tag = "TIME_TRAVELED_MIL_ON"
    override val name = "Time run with MIL on"
    override val mode = "01"
    override val pid = "4D"

    override val defaultUnit = "min"
    override val handler = { response: ObdRawResponse -> bytesToInt(response.bufferedValue).toString() }
}
