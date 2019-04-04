package com.github.eltonvs.obd.command

import java.util.regex.Pattern


object RegexPatterns {
    val WHITESPACE_PATTERN: Pattern = Pattern.compile("\\s")
    val BUS_INIT_PATTERN: Pattern = Pattern.compile("(BUS INIT)|(BUSINIT)|(\\.)")
    val SEARCHING_PATTERN: Pattern = Pattern.compile("SEARCHING")
    val CARRIAGE_PATTERN: Pattern = Pattern.compile("[\r\n]")
    val CARRIAGE_COLON_PATTERN: Pattern = Pattern.compile("[\r\n].:")
    val COLON_PATTERN: Pattern = Pattern.compile(":")
    val DIGITS_LETTERS_PATTERN: Pattern = Pattern.compile("([0-9A-F])+")
    val STARTS_WITH_ALPHANUM_PATTERN: Pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
}


fun removeAll(pattern: Pattern, input: String): String {
    return pattern.matcher(input).replaceAll("")
}