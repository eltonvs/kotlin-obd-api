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
        val codeCount = mil.toInt() and 0x7F
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

    override val handler =
        { it: ObdRawResponse -> parseTroubleCodesList(it.value).joinToString(separator = ",") }

    abstract val carriageNumberPattern: Pattern

    var troubleCodesList = mutableListOf<String>()

    private fun parseTroubleCodesList(rawValue: String): List<String> {
        if (rawValue.isBlank()) {
            return emptyList()
        }
        val rawValueN = rawValue.replace("[\\s:]".toRegex(), "")

        val canOneFrame: String = removeAll(rawValueN, CARRIAGE_PATTERN, WHITESPACE_PATTERN)
        val canOneFrameLength: Int = canOneFrame.length

        val workingData = when {
            (canOneFrameLength <= 16) && (canOneFrameLength % 4 == 0) -> canOneFrame.drop(4)
            rawValueN.contains(":") -> removeAll(CARRIAGE_COLON_PATTERN, rawValueN).drop(7)
            else -> removeAll(rawValueN, carriageNumberPattern, WHITESPACE_PATTERN)
        }

        /* For each chunk of 4 chars:
                it:  "0100"
                HEX: 0   1    0   0
                BIN: 00000001 00000000
                        [][][    hex    ]
                        | / /
           DTC: P0100 */
        val troubleCodesList = mutableListOf<String>()
        for (chunk in workingData.chunked(4)) {
            val b1 = chunk.first().toString().toInt(radix = 16)
            val ch1 = (b1 shr 2) and 0b11
            val ch2 = b1 and 0b11
            val troubleCode = "${DTC_LETTERS[ch1]}${HEX_ARRAY[ch2]}${chunk.drop(1)}".padEnd(5, '0')

            if (troubleCode == "P0000") {
                break // Stop adding trouble codes once we encounter "P0000"
            }

            troubleCodesList.add(troubleCode)
        }

        return troubleCodesList
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