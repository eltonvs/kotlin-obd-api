package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import java.util.regex.Pattern


private fun calculate(rawValue: String): Int {
    val a = rawValue[2].toInt()
    val b = rawValue[3].toInt()
    return a * 256 + b
}

class DTCNumberCommand : ObdCommand() {
    override val tag = "DTC_NUMBER"
    override val name = "Diagnostic Trouble Codes Number"
    override val mode = "01"
    override val pid = "01"

    override val defaultUnit = " codes"
    override val handler = { rawValue: String ->
        val mil = rawValue[2].toInt()
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
    override val handler = { x: String -> calculate(x).toString() }
}

class TimeSinceCodesClearedCommand : ObdCommand() {
    override val tag = "TIME_SINCE_CODES_CLEARED"
    override val name = "Time since codes cleared"
    override val mode = "01"
    override val pid = "4E"

    override val defaultUnit = "min"
    override val handler = { x: String -> calculate(x).toString() }
}


abstract class BaseTroubleCodesCommand : ObdCommand() {
    override val pid = ""

    override val handler = { x: String -> parseTroubleCodesList(x).joinToString(separator = ",") }

    private fun parseTroubleCodesList(rawValue: String): List<String> {
        val canOneFrame: String = removeCarriage(rawValue)
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
                workingData = removeCarriageColon(rawValue)  // xxx43yy[codes]
                startIndex = 7  // Header is xxx43yy, xxx is bytes of information to follow, yy showing the
            }
            else -> {
                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                workingData = removeCarriageNumber(rawValue)
                startIndex = 0
            }
        }

        val troubleCodesList = mutableListOf<String>()
        for (begin in startIndex until workingData.length - 3 step 4) {
            val b1 = hexStringToByteArray(workingData[begin]).toInt()
            val ch1 = ((b1 and 0xC0) shr 6)
            val ch2 = ((b1 and 0x30) shr 4)
            val dtc = "${DTC_LETTERS[ch1]}${HEX_ARRAY[ch2]}${workingData.substring(begin + 1, begin + 4)}"
            if (dtc == "P0000") {
                break
            }
            troubleCodesList.add(dtc)
        }

        return troubleCodesList
    }

    private fun removeCarriage(str: String) = CARRIAGE_PATTERN.matcher(str).replaceAll("")

    private fun removeCarriageColon(str: String) = CARRIAGE_COLON_PATTERN.matcher(str).replaceAll("")

    protected abstract fun removeCarriageNumber(str: String): String

    private fun hexStringToByteArray(s: Char): Byte {
        return (Character.digit(s, 16) shl 4).toByte()
    }

    protected companion object {
        val DTC_LETTERS = charArrayOf('P', 'C', 'B', 'U')
        val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
        private val CARRIAGE_PATTERN = Pattern.compile("[\r\n]")
        private val CARRIAGE_COLON_PATTERN = Pattern.compile("[\r\n].:")
    }
}

class TroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "TROUBLE_CODES"
    override val name = "Trouble Codes"
    override val mode = "03"

    override fun removeCarriageNumber(str: String): String = CARRIAGE_NUMBER_PATTERN.matcher(str).replaceAll("")

    companion object {
        private val CARRIAGE_NUMBER_PATTERN = Pattern.compile("^43|[\r\n]43|[\r\n]")
    }
}

class PendingTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "PENDING_TROUBLE_CODES"
    override val name = "Pending Trouble Codes"
    override val mode = "07"

    override fun removeCarriageNumber(str: String): String = CARRIAGE_NUMBER_PATTERN.matcher(str).replaceAll("")

    companion object {
        private val CARRIAGE_NUMBER_PATTERN = Pattern.compile("^47|[\r\n]47|[\r\n]")
    }
}

class PermanentTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "PERMANENT_TROUBLE_CODES"
    override val name = "Permanent Trouble Codes"
    override val mode = "0A"

    override fun removeCarriageNumber(str: String): String = CARRIAGE_NUMBER_PATTERN.matcher(str).replaceAll("")

    companion object {
        private val CARRIAGE_NUMBER_PATTERN = Pattern.compile("^4A|[\r\n]4A|[\r\n]")
    }
}