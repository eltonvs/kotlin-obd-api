package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.buffered
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Exercises the connection against a source that really blocks the reading
 * thread, the way a socket or serial port does, which the commonTest Buffer
 * based tests cannot do.
 */
class BlockingSourceTest {
    @Test
    fun `gives up on a silent source after the legacy timeout budget`() =
        runBlocking {
            val input = BlockingSource()
            ObdDeviceConnection(input.buffered(), Buffer(), Dispatchers.IO).use { connection ->
                val response =
                    withTimeout(3_000) {
                        connection.run(BlockingTestCommand(tag = "SPEED", pid = "0D"), maxRetries = 1)
                    }
                assertEquals("", response.value)
                assertTrue(
                    response.rawResponse.elapsedTime in 450..1_500,
                    "expected ~500ms, was ${response.rawResponse.elapsedTime}ms",
                )
            }
            input.close()
        }

    @Test
    fun `returns as soon as the terminator arrives on a blocking source`() =
        runBlocking {
            val input = BlockingSource()
            ObdDeviceConnection(input.buffered(), Buffer(), Dispatchers.IO).use { connection ->
                launch(Dispatchers.IO) {
                    delay(50)
                    input.feed("41")
                    delay(30)
                    input.feed("0D40>")
                }
                val response =
                    withTimeout(3_000) {
                        connection.run(BlockingTestCommand(tag = "SPEED", pid = "0D"))
                    }
                assertEquals("410D40", response.value)
                assertTrue(
                    response.rawResponse.elapsedTime < 500,
                    "expected well under the 2500ms budget, was ${response.rawResponse.elapsedTime}ms",
                )
            }
            input.close()
        }

    @Test
    fun `returns already buffered data with a zero retry budget`() =
        runBlocking {
            val input = BlockingSource()
            ObdDeviceConnection(input.buffered(), Buffer(), Dispatchers.IO).use { connection ->
                input.feed("410D40>")
                val response =
                    withTimeout(3_000) {
                        connection.run(BlockingTestCommand(tag = "SPEED", pid = "0D"), maxRetries = 0)
                    }
                assertEquals("410D40", response.value)
            }
            input.close()
        }

    @Test
    fun `end of stream ends the read immediately`() =
        runBlocking {
            val input = BlockingSource()
            ObdDeviceConnection(input.buffered(), Buffer(), Dispatchers.IO).use { connection ->
                input.feed("410D40")
                input.endOfStream()
                val first =
                    withTimeout(3_000) {
                        connection.run(BlockingTestCommand(tag = "SPEED", pid = "0D"))
                    }
                assertEquals("410D40", first.value)
                assertTrue(
                    first.rawResponse.elapsedTime < 500,
                    "expected immediate return on EOF, was ${first.rawResponse.elapsedTime}ms",
                )

                val second =
                    withTimeout(3_000) {
                        connection.run(BlockingTestCommand(tag = "RPM", pid = "0C"))
                    }
                assertEquals("", second.value)
                assertTrue(
                    second.rawResponse.elapsedTime < 500,
                    "expected immediate return on a dead source, was ${second.rawResponse.elapsedTime}ms",
                )
            }
            input.close()
        }

    @Test
    fun `a failing source propagates its error`() =
        runBlocking {
            val input = BlockingSource()
            ObdDeviceConnection(input.buffered(), Buffer(), Dispatchers.IO).use { connection ->
                input.fail(IOException("socket reset"))
                assertFailsWith<IOException> {
                    withTimeout(3_000) {
                        connection.run(BlockingTestCommand(tag = "SPEED", pid = "0D"))
                    }
                }
            }
            input.close()
        }
}

/** A RawSource whose reads block until [feed], [endOfStream] or [fail] is called. */
private class BlockingSource : RawSource {
    private sealed interface Event {
        class Data(
            val bytes: ByteArray,
        ) : Event

        object Eof : Event

        class Failure(
            val cause: IOException,
        ) : Event
    }

    private val events = LinkedBlockingQueue<Event>()

    @Volatile
    private var closed = false

    fun feed(text: String) = events.put(Event.Data(text.encodeToByteArray()))

    fun endOfStream() = events.put(Event.Eof)

    fun fail(cause: IOException) = events.put(Event.Failure(cause))

    override fun readAtMostTo(
        sink: Buffer,
        byteCount: Long,
    ): Long {
        if (closed) return -1
        return when (val event = events.take()) {
            is Event.Data -> {
                sink.write(event.bytes)
                event.bytes.size.toLong()
            }

            Event.Eof -> {
                events.put(Event.Eof)
                -1
            }

            is Event.Failure -> {
                throw event.cause
            }
        }
    }

    override fun close() {
        closed = true
        events.put(Event.Eof)
    }
}

private class BlockingTestCommand(
    override val tag: String,
    override val pid: String,
    override val mode: String = "01",
) : ObdCommand() {
    override val name: String = tag
    override val skipDigitCheck: Boolean = true
    override val handler: (ObdRawResponse) -> String = { it.processedValue }
}
