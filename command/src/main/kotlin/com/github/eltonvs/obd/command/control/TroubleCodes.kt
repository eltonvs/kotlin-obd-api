package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import java.util.regex.Pattern


class DTCNumberCommand : ObdCommand() {
    override val tag = "DTC_NUMBER"
    override val name = "Diagnostic Trouble Codes Number"
    override val mode = "01"
    override val pid = "01"

    override val defaultUnit = " codes"
    override val handler = { it: ObdRawResponse ->
        val mil = it.bufferedValue[2]
        val codeCount = mil and 0x7F
        codeCount.toString()
    }
}

class DistanceSinceCodesClearedCommand : ObdCommand() {
    override val tag = "DISTANCE_TRAVELED_AFTER_CODES_CLEARED"
    override val name = "Distance traveled since codes cleared"
    override val mode = "01"
    override val pid = "31"

    override val defaultUnit = "Km"
    override val handler = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}

class TimeSinceCodesClearedCommand : ObdCommand() {
    override val tag = "TIME_SINCE_CODES_CLEARED"
    override val name = "Time since codes cleared"
    override val mode = "01"
    override val pid = "4E"

    override val defaultUnit = "min"
    override val handler = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}

class ResetTroubleCodesCommand : ObdCommand() {
    override val tag = "RESET_TROUBLE_CODES"
    override val name = "Reset Trouble Codes"
    override val mode = "04"
    override val pid = ""
}

abstract class BaseTroubleCodesCommand : ObdCommand() {
    override val pid = ""

    override val handler = { it: ObdRawResponse -> parseTroubleCodesList(it.value).joinToString(separator = ",") }

    abstract val carriageNumberPattern: Pattern

    private fun parseTroubleCodesList(rawValue: String): List<String> {
        val canOneFrame: String = removeAll(CARRIAGE_PATTERN, rawValue)
        val canOneFrameLength: Int = canOneFrame.length

        val workingData: String
        val startIndex: Int
        when {
            (canOneFrameLength <= 16) and (canOneFrameLength % 4 == 0) -> {
                // CAN(ISO-15765) protocol one frame.
                workingData = canOneFrame  // 43yy[codes]
                startIndex = 4  // Header is 43yy, yy showing the number of data items.
            }
            rawValue.contains(":") -> {
                // CAN(ISO-15765) protocol two and more frames.
                workingData = removeAll(CARRIAGE_COLON_PATTERN, rawValue)  // xxx43yy[codes]
                startIndex = 7  // Header is xxx43yy, xxx is bytes of information to follow, yy showing the
            }
            else -> {
                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                workingData = removeAll(carriageNumberPattern, rawValue)
                startIndex = 0
            }
        }

        val troubleCodesList = workingData.substring(startIndex).chunked(5) {
            val b1 = hexStringToByteArray(it.first()).toInt()
            val ch1 = ((b1 and 0xC0) shr 6)
            val ch2 = ((b1 and 0x30) shr 4)
            "${DTC_LETTERS[ch1]}${HEX_ARRAY[ch2]}${it.drop(1)}"
        }

        return if (troubleCodesList.contains("P0000")) listOf() else troubleCodesList
    }

    private fun removeAll(pattern: Pattern, str: String) = pattern.matcher(str).replaceAll("")

    private fun hexStringToByteArray(s: Char): Byte = (Character.digit(s, 16) shl 4).toByte()

    protected companion object {
        private val DTC_LETTERS = charArrayOf('P', 'C', 'B', 'U')
        private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
        private val CARRIAGE_PATTERN = Pattern.compile("[\r\n]")
        private val CARRIAGE_COLON_PATTERN = Pattern.compile("[\r\n].:")
    }
}

class TroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "TROUBLE_CODES"
    override val name = "Trouble Codes"
    override val mode = "03"

    override val carriageNumberPattern: Pattern = Pattern.compile("^43|[\r\n]43|[\r\n]")
}

class PendingTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "PENDING_TROUBLE_CODES"
    override val name = "Pending Trouble Codes"
    override val mode = "07"

    override val carriageNumberPattern: Pattern = Pattern.compile("^47|[\r\n]47|[\r\n]")
}

class PermanentTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "PERMANENT_TROUBLE_CODES"
    override val name = "Permanent Trouble Codes"
    override val mode = "0A"

    override val carriageNumberPattern: Pattern = Pattern.compile("^4A|[\r\n]4A|[\r\n]")
}