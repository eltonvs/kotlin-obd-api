package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.RegexPatterns.BUS_INIT_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.COLON_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN


fun <T> T.pipe(vararg functions: (T) -> T): T =
    functions.fold(this) { value, f -> f(value) }


data class ObdRawResponse(
    val value: String,
    val elapsedTime: Long
) {
    private val valueProcessorPipeline by lazy {
        arrayOf<(String) -> String>(
            {
                /*
                 * Imagine the following response 41 0c 00 0d.
                 *
                 * ELM sends strings!! So, ELM puts spaces between each "byte". And pay
                 * attention to the fact that I've put the word byte in quotes, because 41
                 * is actually TWO bytes (two chars) in the socket. So, we must do some more
                 * processing...
                 */
                removeAll(WHITESPACE_PATTERN, it) // removes all [ \t\n\x0B\f\r]
            },
            {
                /*
                 * Data may have echo or informative text like "INIT BUS..." or similar.
                 * The response ends with two carriage return characters. So we need to take
                 * everything from the last carriage return before those two (trimmed above).
                 */
                removeAll(BUS_INIT_PATTERN, it)
            },
            {
                removeAll(COLON_PATTERN, it)
            }
        )
    }

    val processedValue by lazy { value.pipe(*valueProcessorPipeline) }

    val bufferedValue by lazy { processedValue.chunked(2) { it.toString().toInt(radix = 16) }.toIntArray() }
}

data class ObdResponse(
    val command: ObdCommand,
    val rawResponse: ObdRawResponse,
    val value: String,
    val unit: String = ""
) {
    val formattedValue: String get() = command.format(this)
}