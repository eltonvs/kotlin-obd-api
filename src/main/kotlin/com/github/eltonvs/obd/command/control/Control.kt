package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.RegexPatterns.BUS_INIT_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.STARTS_WITH_ALPHANUM_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.removeAll


class ModuleVoltageCommand : ObdCommand() {
    override val tag = "CONTROL_MODULE_VOLTAGE"
    override val name = "Control Module Power Supply"
    override val mode = "01"
    override val pid = "42"

    override val defaultUnit = "V"
    override val handler = { it: ObdRawResponse -> "%.2f".format(bytesToInt(it.bufferedValue) / 1000f) }
}

class TimingAdvanceCommand : ObdCommand() {
    override val tag = "TIMING_ADVANCE"
    override val name = "Timing Advance"
    override val mode = "01"
    override val pid = "0E"

    override val defaultUnit = "°"
    override val handler = { it: ObdRawResponse -> "%.2f".format(bytesToInt(it.bufferedValue, bytesToProcess = 1) / 2f - 64) }
}

class VINCommand : ObdCommand() {
    override val tag = "VIN"
    override val name = "Vehicle Identification Number (VIN)"
    override val mode = "09"
    override val pid = "02"

    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> parseVIN(removeAll(it.value, WHITESPACE_PATTERN, BUS_INIT_PATTERN)) }

    private fun parseVIN(rawValue: String): String {
        val workingData =
            if (rawValue.contains(":")) {
                // CAN(ISO-15765) protocol.
                // 9 is xxx490201, xxx is bytes of information to follow.
                val value = rawValue.replace(".:".toRegex(), "").substring(9)
                if (STARTS_WITH_ALPHANUM_PATTERN.matcher(convertHexToString(value)).find()) {
                    rawValue.replace("0:49", "").replace(".:".toRegex(), "")
                } else {
                    value
                }
            } else {
                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                rawValue.replace("49020.".toRegex(), "")
            }
        return convertHexToString(workingData).replace("[\u0000-\u001f]".toRegex(), "")
    }

    private fun convertHexToString(hex: String): String =
        hex.chunked(2) { Integer.parseInt(it.toString(), 16).toChar() }.joinToString("")
}