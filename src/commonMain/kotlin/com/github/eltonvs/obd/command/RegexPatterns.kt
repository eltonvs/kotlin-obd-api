package com.github.eltonvs.obd.command

object RegexPatterns {
    val WHITESPACE_PATTERN: Regex = "\\s".toRegex()
    val BUS_INIT_PATTERN: Regex = "(BUS INIT)|(BUSINIT)|(\\.)".toRegex()
    val SEARCHING_PATTERN: Regex = "SEARCHING".toRegex()
    val CARRIAGE_PATTERN: Regex = "[\r\n]".toRegex()
    val CARRIAGE_COLON_PATTERN: Regex = "[\r\n].:".toRegex()
    val COLON_PATTERN: Regex = ":".toRegex()
    val DIGITS_LETTERS_PATTERN: Regex = "([0-9A-F:])+".toRegex()
    val STARTS_WITH_ALPHANUM_PATTERN: Regex = "[^a-z0-9 ]".toRegex(RegexOption.IGNORE_CASE)

    // Negative response: 7F <service 01-0A> <NRC 11 or 12>, matched against the
    // whitespace-stripped response so adapter echo or headers may surround it.
    val UNSUPPORTED_COMMAND_PATTERN: Regex = "7F0[0-9A]1[12]".toRegex()

    // Error patterns
    const val BUSINIT_ERROR_MESSAGE_PATTERN = "BUS INIT... ERROR"
    const val MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN = "?"
    const val NO_DATE_MESSAGE_PATTERN = "NO DATA"
    const val STOPPED_MESSAGE_PATERN = "STOPPED"
    const val UNABLE_TO_CONNECT_MESSAGE_PATTERN = "UNABLE TO CONNECT"
    const val ERROR_MESSAGE_PATTERN = "ERROR"
}

fun removeAll(
    pattern: Regex,
    input: String,
): String = pattern.replace(input, "")

fun removeAll(
    input: String,
    vararg patterns: Regex,
) = patterns.fold(input) { acc, pattern -> removeAll(pattern, acc) }
