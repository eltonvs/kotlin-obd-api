package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.RegexPatterns.CARRIAGE_COLON_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.CARRIAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.removeAll
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

    var troubleCodesList = listOf<String>()
        private set

    private fun parseTroubleCodesList(rawValue: String): List<String> {
        val canOneFrame: String = removeAll(rawValue, CARRIAGE_PATTERN, WHITESPACE_PATTERN)
        val canOneFrameLength: Int = canOneFrame.length

        val workingData =
            when {
                /* CAN(ISO-15765) protocol one frame: 43yy[codes]
                   Header is 43yy, yy showing the number of data items. */
                (canOneFrameLength <= 16) and (canOneFrameLength % 4 == 0) -> canOneFrame.drop(4)
                /* CAN(ISO-15765) protocol two and more frames: xxx43yy[codes]
                   Header is xxx43yy, xxx is bytes of information to follow, yy showing the number of data items. */
                rawValue.contains(":") -> removeAll(CARRIAGE_COLON_PATTERN, rawValue).drop(7)
                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                else -> removeAll(rawValue, carriageNumberPattern, WHITESPACE_PATTERN)
            }

        /* For each chunk of 4 chars:
           it:  "0100"
           HEX: 0   1    0   0
           BIN: 00000001 00000000
                [][][    hex    ]
                | / /
           DTC: P0100 */
        val troubleCodesList = workingData.chunked(4) {
            val b1 = it.first().toString().toInt(radix = 16)
            val ch1 = (b1 shr 2) and 0b11
            val ch2 = b1 and 0b11
            "${DTC_LETTERS[ch1]}${HEX_ARRAY[ch2]}${it.drop(1)}".padEnd(5, '0')
        }

        val idx = troubleCodesList.indexOf("P0000")
        return (if (idx < 0) troubleCodesList else troubleCodesList.take(idx)).also {
            this.troubleCodesList = it
        }
    }

    protected companion object {
        private val DTC_LETTERS = charArrayOf('P', 'C', 'B', 'U')
        private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
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