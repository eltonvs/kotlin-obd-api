package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.bytesToInt


class MILOnCommand : ObdCommand() {
    override val tag = "MIL_ON"
    override val name = "MIL on"
    override val mode = "01"
    override val pid = "01"

    override val handler = { it: ObdRawResponse ->
        val mil = it.bufferedValue[2]
        val milOn = (mil and 0x80) == 128
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
    override val handler = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}

class TimeSinceMILOnCommand : ObdCommand() {
    override val tag = "TIME_TRAVELED_MIL_ON"
    override val name = "Time run with MIL on"
    override val mode = "01"
    override val pid = "4D"

    override val defaultUnit = "min"
    override val handler = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}