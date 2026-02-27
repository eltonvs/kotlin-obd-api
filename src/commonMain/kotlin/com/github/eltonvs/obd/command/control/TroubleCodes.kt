package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.RegexPatterns.CARRIAGE_COLON_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.CARRIAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.removeAll

private const val STATUS_BYTE_INDEX = 2
private const val DTC_COUNT_MASK = 0x7F
private const val CAN_ONE_FRAME_MAX_LENGTH = 16
private const val DTC_HEX_CHUNK_SIZE = 4
private const val CAN_ONE_FRAME_HEADER_SIZE = 4
private const val CAN_MULTI_FRAME_HEADER_SIZE = 7
private const val DTC_TYPE_SHIFT = 2
private const val DTC_TYPE_MASK = 0b11
private const val DTC_CODE_MASK = 0b11
private const val PAD_DTC_LENGTH = 5
private const val PAD_DTC_CHAR = '0'

class DTCNumberCommand : ObdCommand() {
    override val tag = "DTC_NUMBER"
    override val name = "Diagnostic Trouble Codes Number"
    override val mode = "01"
    override val pid = "01"

    override val defaultUnit = " codes"
    override val handler = { response: ObdRawResponse ->
        val mil = response.bufferedValue[STATUS_BYTE_INDEX]
        val codeCount = mil and DTC_COUNT_MASK
        codeCount.toString()
    }
}

class DistanceSinceCodesClearedCommand : ObdCommand() {
    override val tag = "DISTANCE_TRAVELED_AFTER_CODES_CLEARED"
    override val name = "Distance traveled since codes cleared"
    override val mode = "01"
    override val pid = "31"

    override val defaultUnit = "Km"
    override val handler = { response: ObdRawResponse -> bytesToInt(response.bufferedValue).toString() }
}

class TimeSinceCodesClearedCommand : ObdCommand() {
    override val tag = "TIME_SINCE_CODES_CLEARED"
    override val name = "Time since codes cleared"
    override val mode = "01"
    override val pid = "4E"

    override val defaultUnit = "min"
    override val handler = { response: ObdRawResponse -> bytesToInt(response.bufferedValue).toString() }
}

class ResetTroubleCodesCommand : ObdCommand() {
    override val tag = "RESET_TROUBLE_CODES"
    override val name = "Reset Trouble Codes"
    override val mode = "04"
    override val pid = ""
}

abstract class BaseTroubleCodesCommand : ObdCommand() {
    override val pid = ""

    override val handler = { response: ObdRawResponse ->
        parseTroubleCodesList(response.value).joinToString(separator = ",")
    }

    abstract val carriageNumberPattern: Regex

    var troubleCodesList = listOf<String>()
        private set

    private fun parseTroubleCodesList(rawValue: String): List<String> {
        val canOneFrame: String = removeAll(rawValue, CARRIAGE_PATTERN, WHITESPACE_PATTERN)
        val canOneFrameLength = canOneFrame.length

        val workingData =
            when {
                /* CAN(ISO-15765) protocol one frame: 43yy[codes]
                   Header is 43yy, yy showing the number of data items. */
                (canOneFrameLength <= CAN_ONE_FRAME_MAX_LENGTH) and
                    (canOneFrameLength % DTC_HEX_CHUNK_SIZE == 0) -> {
                    canOneFrame.drop(CAN_ONE_FRAME_HEADER_SIZE)
                }

                /* CAN(ISO-15765) protocol two and more frames: xxx43yy[codes]
                   Header is xxx43yy, xxx is bytes of information to follow, yy showing the number of data items. */
                rawValue.contains(":") -> {
                    removeAll(CARRIAGE_COLON_PATTERN, rawValue).drop(CAN_MULTI_FRAME_HEADER_SIZE)
                }

                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                else -> {
                    removeAll(rawValue, carriageNumberPattern, WHITESPACE_PATTERN)
                }
            }

        /* For each chunk of 4 chars:
           it:  "0100"
           HEX: 0   1    0   0
           BIN: 00000001 00000000
                [][][    hex    ]
                | / /
           DTC: P0100 */
        val troubleCodesList =
            workingData.chunked(DTC_HEX_CHUNK_SIZE) {
                val b1 = it.first().toString().toInt(radix = 16)
                val ch1 = (b1 shr DTC_TYPE_SHIFT) and DTC_TYPE_MASK
                val ch2 = b1 and DTC_CODE_MASK
                "${DTC_LETTERS[ch1]}${HEX_ARRAY[ch2]}${it.drop(1)}".padEnd(PAD_DTC_LENGTH, PAD_DTC_CHAR)
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

    override val carriageNumberPattern: Regex = "^43|[\r\n]43|[\r\n]".toRegex()
}

class PendingTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "PENDING_TROUBLE_CODES"
    override val name = "Pending Trouble Codes"
    override val mode = "07"

    override val carriageNumberPattern: Regex = "^47|[\r\n]47|[\r\n]".toRegex()
}

class PermanentTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag = "PERMANENT_TROUBLE_CODES"
    override val name = "Permanent Trouble Codes"
    override val mode = "0A"

    override val carriageNumberPattern: Regex = "^4A|[\r\n]4A|[\r\n]".toRegex()
}
