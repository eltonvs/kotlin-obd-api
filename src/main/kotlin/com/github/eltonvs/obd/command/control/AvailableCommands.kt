package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse


class AvailablePIDsCommand(private val range: AvailablePIDsRanges) : ObdCommand() {
    override val tag = "AVAILABLE_COMMANDS_${range.name}"
    override val name = "Available Commands - ${range.displayName}"
    override val mode = "01"
    override val pid = range.pid

    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse ->
        parsePIDs(it.processedValue).joinToString(",") { "%02X".format(it) }
    }

    private fun parsePIDs(rawValue: String): IntArray {
        val value = rawValue.toLong(radix = 16)
        val initialPID = range.pid.toInt(radix = 16)
        return (1..33).fold(intArrayOf()) { acc, i ->
            if (getBit(value, i) == 1) acc.plus(i + initialPID) else acc
        }
    }

    private fun getBit(number: Long, position: Int) = (number shr (32 - position) and 1).toInt()

    enum class AvailablePIDsRanges(val displayName: String, internal val pid: String) {
        PIDS_01_TO_20("PIDs from 01 to 20", "00"),
        PIDS_21_TO_40("PIDs from 21 to 40", "20"),
        PIDS_41_TO_60("PIDs from 41 to 60", "40"),
        PIDS_61_TO_80("PIDs from 61 to 80", "60"),
        PIDS_81_TO_A0("PIDs from 81 to A0", "80")
    }
}