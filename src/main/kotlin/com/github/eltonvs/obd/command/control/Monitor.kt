package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.Monitors
import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.getBitAt

data class SensorStatus(val available: Boolean, val complete: Boolean)
data class SensorStatusData(
    val milOn: Boolean,
    val dtcCount: Int,
    val isSpark: Boolean,
    val items: Map<Monitors, SensorStatus>
)

abstract class BaseMonitorStatus : ObdCommand() {
    override val mode = "01"

    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse ->
        parseData(it.bufferedValue.takeLast(4)).let { "" }
    }

    var data: SensorStatusData? = null

    /**
     * Parses the Monitor Status data
     *
     *           ┌Components not ready
     *           |┌Fuel not ready
     *           ||┌Misfire not ready
     *           |||┌Spark vs. Compression
     *           ||||┌Components supported
     *           |||||┌Fuel supported
     * ┌MIL      ||||||┌Misfire supported
     * |         |||||||
     * 10000011 00000111 11111111 00000000
     *  [# DTC] X        [supprt] [~ready]
     */
    private fun parseData(values: List<Int>) {
        if (values.size != 4) {
            return
        }
        val milOn = values[0].getBitAt(1, 8) == 1
        val dtcCount = values[0] and 0x7F
        val isSpark = values[1].getBitAt(5, 8) == 0

        val monitorMap = HashMap<Monitors, SensorStatus>()
        Monitors.values().forEach {
            val normalizedPos = 8 - it.bitPos
            if (it.isSparkIgnition == null) {
                val isAvailable = values[1].getBitAt(normalizedPos, 8) == 1
                val isComplete = values[1].getBitAt(normalizedPos - 4, 8) == 0
                monitorMap[it] = SensorStatus(isAvailable, isComplete)
            } else if (it.isSparkIgnition == isSpark) {
                val isAvailable = values[2].getBitAt(normalizedPos, 8) == 1
                val isComplete = values[3].getBitAt(normalizedPos, 8) == 0
                monitorMap[it] = SensorStatus(isAvailable, isComplete)
            }
        }
        data = SensorStatusData(milOn, dtcCount, isSpark, monitorMap)
    }
}

class MonitorStatusSinceCodesClearedCommand : BaseMonitorStatus() {
    override val tag = "MONITOR_STATUS_SINCE_CODES_CLEARED"
    override val name = "Monitor Status Since Codes Cleared"
    override val pid = "01"
}

class MonitorStatusCurrentDriveCycleCommand : BaseMonitorStatus() {
    override val tag = "MONITOR_STATUS_CURRENT_DRIVE_CYCLE"
    override val name = "Monitor Status Current Drive Cycle"
    override val pid = "41"
}
