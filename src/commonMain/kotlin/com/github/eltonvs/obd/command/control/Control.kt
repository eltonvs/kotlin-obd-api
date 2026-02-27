package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.RegexPatterns.BUS_INIT_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.STARTS_WITH_ALPHANUM_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.formatFloat
import com.github.eltonvs.obd.command.removeAll

private const val MODULE_VOLTAGE_DIVISOR = 1000f
private const val TIMING_ADVANCE_DIVISOR = 2f
private const val TIMING_ADVANCE_OFFSET = 64f
private const val VIN_CAN_FRAME_PREFIX_LENGTH = 9
private const val HEX_CHUNK_SIZE = 2
private const val HEX_RADIX = 16

class ModuleVoltageCommand : ObdCommand() {
    override val tag = "CONTROL_MODULE_VOLTAGE"
    override val name = "Control Module Power Supply"
    override val mode = "01"
    override val pid = "42"

    override val defaultUnit = "V"
    override val handler = { response: ObdRawResponse ->
        formatFloat(bytesToInt(response.bufferedValue) / MODULE_VOLTAGE_DIVISOR, 2)
    }
}

class TimingAdvanceCommand : ObdCommand() {
    override val tag = "TIMING_ADVANCE"
    override val name = "Timing Advance"
    override val mode = "01"
    override val pid = "0E"

    override val defaultUnit = "°"
    override val handler = { response: ObdRawResponse ->
        formatFloat(
            bytesToInt(response.bufferedValue, bytesToProcess = 1) / TIMING_ADVANCE_DIVISOR - TIMING_ADVANCE_OFFSET,
            2,
        )
    }
}

class VINCommand : ObdCommand() {
    override val tag = "VIN"
    override val name = "Vehicle Identification Number (VIN)"
    override val mode = "09"
    override val pid = "02"

    override val defaultUnit = ""
    override val handler = { response: ObdRawResponse ->
        parseVIN(removeAll(response.value, WHITESPACE_PATTERN, BUS_INIT_PATTERN))
    }

    private fun parseVIN(rawValue: String): String {
        val workingData =
            if (rawValue.contains(":")) {
                // CAN(ISO-15765) protocol.
                // 9 is xxx490201, xxx is bytes of information to follow.
                val value = rawValue.replace(".:".toRegex(), "").substring(VIN_CAN_FRAME_PREFIX_LENGTH)
                if (STARTS_WITH_ALPHANUM_PATTERN.containsMatchIn(convertHexToString(value))) {
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
        hex
            .chunked(HEX_CHUNK_SIZE) {
                it.toString().toInt(HEX_RADIX).toChar()
            }.joinToString("")
}
