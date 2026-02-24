package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.Monitors
import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.getBitAt

private const val MONITOR_BYTES_COUNT = 4
private const val MIL_BIT_POSITION = 1
private const val LAST_BIT_POSITION = 8
private const val SPARK_CHECK_BIT_POSITION = 5
private const val COMMON_MONITOR_COMPLETION_OFFSET = 4
private const val DTC_COUNT_MASK = 0x7F
private const val BIT_SET = 1
private const val DATA_BYTE_0 = 0
private const val DATA_BYTE_1 = 1
private const val DATA_BYTE_2 = 2
private const val DATA_BYTE_3 = 3

data class SensorStatus(
    val available: Boolean,
    val complete: Boolean,
)

data class SensorStatusData(
    val milOn: Boolean,
    val dtcCount: Int,
    val isSpark: Boolean,
    val items: Map<Monitors, SensorStatus>,
)

abstract class BaseMonitorStatus : ObdCommand() {
    override val mode = "01"

    override val defaultUnit = ""
    override val handler = { response: ObdRawResponse ->
        parseData(response.bufferedValue.takeLast(MONITOR_BYTES_COUNT))
        ""
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
     *  [# DTC] X [supprt] [~ready]
     */
    private fun parseData(values: List<Int>) {
        if (values.size != MONITOR_BYTES_COUNT) {
            return
        }
        val milOn = values[DATA_BYTE_0].getBitAt(MIL_BIT_POSITION, LAST_BIT_POSITION) == BIT_SET
        val dtcCount = values[DATA_BYTE_0] and DTC_COUNT_MASK
        val isSpark = values[DATA_BYTE_1].getBitAt(SPARK_CHECK_BIT_POSITION, LAST_BIT_POSITION) == 0

        val monitorMap = HashMap<Monitors, SensorStatus>()
        Monitors.values().forEach {
            val normalizedPos = LAST_BIT_POSITION - it.bitPos
            if (it.isSparkIgnition == null) {
                val isAvailable = values[DATA_BYTE_1].getBitAt(normalizedPos, LAST_BIT_POSITION) == BIT_SET
                val isComplete =
                    values[DATA_BYTE_1].getBitAt(
                        normalizedPos - COMMON_MONITOR_COMPLETION_OFFSET,
                        LAST_BIT_POSITION,
                    ) == 0
                monitorMap[it] = SensorStatus(isAvailable, isComplete)
            } else if (it.isSparkIgnition == isSpark) {
                val isAvailable = values[DATA_BYTE_2].getBitAt(normalizedPos, LAST_BIT_POSITION) == BIT_SET
                val isComplete = values[DATA_BYTE_3].getBitAt(normalizedPos, LAST_BIT_POSITION) != BIT_SET
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
