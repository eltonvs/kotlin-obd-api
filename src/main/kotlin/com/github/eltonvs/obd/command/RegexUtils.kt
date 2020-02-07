package com.github.eltonvs.obd.command

import java.util.regex.Pattern


object RegexPatterns {
    val WHITESPACE_PATTERN: Pattern = Pattern.compile("\\s")
    val BUS_INIT_PATTERN: Pattern = Pattern.compile("(BUS INIT)|(BUSINIT)|(\\.)")
    val SEARCHING_PATTERN: Pattern = Pattern.compile("SEARCHING")
    val CARRIAGE_PATTERN: Pattern = Pattern.compile("[\r\n]")
    val CARRIAGE_COLON_PATTERN: Pattern = Pattern.compile("[\r\n].:")
    val COLON_PATTERN: Pattern = Pattern.compile(":")
    val DIGITS_LETTERS_PATTERN: Pattern = Pattern.compile("([0-9A-F:])+")
    val STARTS_WITH_ALPHANUM_PATTERN: Pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)

    // Error patterns
    const val BUSINIT_ERROR_MESSAGE_PATTERN = "BUS INIT... ERROR"
    const val MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN = "?"
    const val NO_DATE_MESSAGE_PATTERN = "NO DATA"
    const val STOPPED_MESSAGE_PATERN = "STOPPED"
    const val UNABLE_TO_CONNECT_MESSAGE_PATTERN = "UNABLE TO CONNECT"
    const val ERROR_MESSAGE_PATTERN = "ERROR"
    const val UNSUPPORTED_COMMAND_MESSAGE_PATTERN = "7F 0[0-A] 1[1-2]"
}


fun removeAll(pattern: Pattern, input: String): String {
    return pattern.matcher(input).replaceAll("")
}

fun removeAll(input: String, vararg patterns: Pattern) =
    patterns.fold(input) { acc, pattern -> removeAll(pattern, acc) }