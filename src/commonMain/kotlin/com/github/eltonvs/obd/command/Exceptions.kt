package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.RegexPatterns.BUSINIT_ERROR_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.DIGITS_LETTERS_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.ERROR_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.NEGATIVE_RESPONSE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.NO_DATE_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.STOPPED_MESSAGE_PATERN
import com.github.eltonvs.obd.command.RegexPatterns.UNABLE_TO_CONNECT_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN

private fun String.sanitize(): String = removeAll(WHITESPACE_PATTERN, this).uppercase()

private const val BYTE_WIDTH = 2
private const val POSITIVE_RESPONSE_PREFIX = '4'

// A negative response sits behind at most a CAN 29-bit identifier and a payload
// length byte, so anything further left in the frame is payload, not a header.
private const val MAX_HEADER_LENGTH = 10

private val NEGATIVE_RESPONSE_CODES = setOf("11", "12")

/**
 * True when any frame of the response is an OBD negative response:
 * `7F <service> <NRC>`, optionally behind a header and followed by a checksum
 * or CAN padding.
 *
 * Frames are checked one at a time, so a command echoed back by the adapter
 * arrives as its own frame rather than hiding the negative response behind it.
 *
 * Inside a frame, `7F` is only a service byte when everything before it is a
 * header, and headers vary too much between protocols to recognise directly --
 * a CAN frame ends its header with the payload length (`7E8 03`), while ISO
 * 9141-2 and KWP2000 use bare address bytes (`48 6B 11`). What can be
 * recognised is the alternative: a positive response to [command] starts with
 * `4<service>`, so a frame carrying that byte is payload and is left alone.
 * Without it, data bytes that happen to read `7F 01 11` would be reported as a
 * rejection.
 */
private fun String.hasUnsupportedCommandFrame(command: ObdCommand): Boolean {
    val mode = command.mode.sanitize()
    // AT commands have no service byte, so no positive response to tell apart.
    val positiveResponse =
        if (mode.length == BYTE_WIDTH && mode.startsWith('0')) {
            "$POSITIVE_RESPONSE_PREFIX${mode[1]}"
        } else {
            null
        }
    return split('\r', '\n').any { it.sanitize().isNegativeResponseFrame(positiveResponse) }
}

private fun String.isNegativeResponseFrame(positiveResponse: String?): Boolean {
    var match = NEGATIVE_RESPONSE_PATTERN.find(this)
    while (match != null) {
        if (isNegativeResponseAt(match.range.first, match.value.length, positiveResponse)) {
            return true
        }
        match = match.next()
    }
    return false
}

/**
 * True for what may follow a negative response inside its frame: nothing, an
 * ISO 9141 / KWP2000 checksum byte, or CAN frame padding. Whole bytes only, and
 * more than one of them only when they are padding, so that a longer stretch of
 * payload data cannot pass as a trailer.
 */
private fun String.isFrameTrailer(): Boolean {
    if (length % BYTE_WIDTH != 0) return false
    if (any { !it.isDigit() && it !in 'A'..'F' }) return false
    return length <= BYTE_WIDTH || all { it == '0' }
}

private fun String.isNegativeResponseAt(
    start: Int,
    serviceLength: Int,
    positiveResponse: String?,
): Boolean {
    if (start > MAX_HEADER_LENGTH) return false
    val codeStart = start + serviceLength
    if (codeStart + BYTE_WIDTH > length) return false
    if (substring(codeStart, codeStart + BYTE_WIDTH) !in NEGATIVE_RESPONSE_CODES) return false
    if (!substring(codeStart + BYTE_WIDTH).isFrameTrailer()) return false
    if (positiveResponse == null) return start == 0
    // Walk the header back a byte at a time, staying aligned with the 7F.
    for (offset in start - BYTE_WIDTH downTo 0 step BYTE_WIDTH) {
        if (startsWith(positiveResponse, offset)) return false
    }
    return true
}

abstract class BadResponseException(
    private val command: ObdCommand,
    private val response: ObdRawResponse,
) : RuntimeException() {
    companion object {
        fun checkForExceptions(
            command: ObdCommand,
            response: ObdRawResponse,
        ): ObdRawResponse =
            with(response.value.sanitize()) {
                when {
                    contains(BUSINIT_ERROR_MESSAGE_PATTERN.sanitize()) -> {
                        throw BusInitException(command, response)
                    }

                    contains(MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN.sanitize()) -> {
                        throw MisunderstoodCommandException(command, response)
                    }

                    contains(NO_DATE_MESSAGE_PATTERN.sanitize()) -> {
                        throw NoDataException(command, response)
                    }

                    contains(STOPPED_MESSAGE_PATERN.sanitize()) -> {
                        throw StoppedException(command, response)
                    }

                    contains(UNABLE_TO_CONNECT_MESSAGE_PATTERN.sanitize()) -> {
                        throw UnableToConnectException(command, response)
                    }

                    contains(ERROR_MESSAGE_PATTERN.sanitize()) -> {
                        throw UnknownErrorException(command, response)
                    }

                    response.value.hasUnsupportedCommandFrame(command) -> {
                        throw UnSupportedCommandException(command, response)
                    }

                    !command.skipDigitCheck && !matches(DIGITS_LETTERS_PATTERN) -> {
                        throw NonNumericResponseException(command, response)
                    }

                    else -> {
                        response
                    }
                }
            }
    }

    override fun toString(): String =
        "${this::class.simpleName ?: "Unknown"} while executing command [${command.tag}], " +
            "response [${response.value}]"
}

private typealias BRE = BadResponseException

class NonNumericResponseException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)

class BusInitException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)

class MisunderstoodCommandException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)

class NoDataException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)

class StoppedException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)

class UnableToConnectException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)

class UnknownErrorException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)

class UnSupportedCommandException(
    command: ObdCommand,
    response: ObdRawResponse,
) : BRE(command, response)
