package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayDeque
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ObdDeviceConnectionTest {
    @Test
    fun `runs one command at a time when invoked concurrently`() =
        runBlocking {
            val input = ScriptedInputStream()
            val output =
                ScriptedOutputStream(
                    input = input,
                    responses =
                        mapOf(
                            "01 0D" to ResponsePlan(payload = "410D40>", autoEnqueue = false),
                            "01 0C" to ResponsePlan(payload = "410C1AF8>"),
                        ),
                )

            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val speedCommand = TestObdCommand(tag = "SPEED", pid = "0D")
            val rpmCommand = TestObdCommand(tag = "RPM", pid = "0C")

            val first = async { connection.run(speedCommand) }
            withTimeout(1_000) {
                while (output.writes.isEmpty()) {
                    delay(10)
                }
            }

            val secondStarted = CompletableDeferred<Unit>()
            val second =
                async {
                    secondStarted.complete(Unit)
                    connection.run(rpmCommand)
                }
            secondStarted.await()
            repeat(20) { yield() }
            assertEquals(listOf("01 0D"), output.writes)

            input.enqueue("410D40>")
            first.await()
            second.await()
            assertEquals(listOf("01 0D", "01 0C"), output.writes)
        }

    @Test
    fun `uses cache safely when called concurrently`() =
        runBlocking {
            val input = ScriptedInputStream()
            val output =
                ScriptedOutputStream(
                    input = input,
                    responses = mapOf("01 05" to ResponsePlan(payload = "41057B>")),
                )

            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val commandA = TestObdCommand(tag = "COOLANT_TEMP", pid = "05")
            val commandB = TestObdCommand(tag = "COOLANT_TEMP", pid = "05")

            val first = async { connection.run(commandA, useCache = true) }
            val second = async { connection.run(commandB, useCache = true) }

            assertEquals("41057B", first.await().value)
            assertEquals("41057B", second.await().value)
            assertEquals(1, output.writes.size)
        }

    @Test
    fun `handles EOF while reading response`() =
        runBlocking {
            val input = DisconnectAfterPayloadInputStream(payload = "410D40")
            val output = ByteArrayOutputStream()
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            val response = connection.run(command)

            assertEquals("410D40", response.value)
            assertEquals("410D40", response.rawResponse.value)
        }

    @Test
    fun `returns promptly when first byte arrives after a short delay`() =
        runBlocking {
            val input = ScriptedInputStream()
            val output =
                ScriptedOutputStream(
                    input = input,
                    responses =
                        mapOf(
                            "01 0D" to ResponsePlan(payload = "410D40>", autoEnqueue = false),
                        ),
                )
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            launch {
                waitUntilWriteCount(output, 1)
                delay(20)
                input.enqueue("410D40>")
            }

            val response =
                connection.runWithReadPolicy(
                    command,
                    readPolicy = ObdReadPolicy(responseTimeoutMs = 1_500, interByteTimeoutMs = 25),
                )

            assertEquals("410D40", response.value)
            assertTrue(
                response.rawResponse.elapsedTime < 250,
                "expected elapsed time below 250ms, was ${response.rawResponse.elapsedTime}ms",
            )
        }

    @Test
    fun `keeps reading fragmented responses while bytes continue arriving`() =
        runBlocking {
            val input = ScriptedInputStream()
            val output =
                ScriptedOutputStream(
                    input = input,
                    responses =
                        mapOf(
                            "01 0C" to ResponsePlan(payload = "410C1AF8>", autoEnqueue = false),
                        ),
                )
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "RPM", pid = "0C")

            launch {
                waitUntilWriteCount(output, 1)
                delay(20)
                input.enqueue("41")
                delay(15)
                input.enqueue("0C")
                delay(15)
                input.enqueue("1AF8>")
            }

            val response =
                connection.runWithReadPolicy(
                    command,
                    readPolicy = ObdReadPolicy(responseTimeoutMs = 1_500, interByteTimeoutMs = 150),
                )

            assertEquals("410C1AF8", response.value)
            assertEquals("410C1AF8", response.rawResponse.value)
        }

    @Test
    fun `uses the legacy retry budget between response fragments`() =
        runBlocking {
            val input = ScriptedInputStream()
            val output =
                ScriptedOutputStream(
                    input = input,
                    responses =
                        mapOf(
                            "01 0C" to ResponsePlan(payload = "410C1AF8>", autoEnqueue = false),
                        ),
                )
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "RPM", pid = "0C")

            launch {
                waitUntilWriteCount(output, 1)
                delay(20)
                input.enqueue("SEARCHING\r")
                delay(700)
                input.enqueue("410C1AF8>")
            }

            val response =
                withTimeout(2_500) {
                    connection.run(command, maxRetries = 3)
                }

            assertEquals("410C1AF8", response.value)
            assertEquals("410C1AF8", response.rawResponse.value)
        }

    @Test
    fun `propagates cancellation while waiting for data`() {
        runBlocking {
            val input = IdleInputStream()
            val output = ByteArrayOutputStream()
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            withTimeout(2_000) {
                val runningCommand = async { connection.run(command, maxRetries = 1_000) }
                delay(100)
                runningCommand.cancel()

                assertFailsWith<CancellationException> { runningCommand.await() }
            }
        }
    }

    @Test
    fun `returns quickly when max retries is zero and no data is available`() =
        runBlocking {
            val input = IdleInputStream()
            val output = ByteArrayOutputStream()
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            withTimeout(400) {
                val response = connection.run(command, maxRetries = 0)
                assertEquals("", response.value)
                assertTrue(
                    response.rawResponse.elapsedTime < 200,
                    "expected near-immediate return, was ${response.rawResponse.elapsedTime}ms",
                )
            }
        }

    @Test
    fun `uses legacy max retries as response timeout budget`() =
        runBlocking {
            val input = IdleInputStream()
            val output = ByteArrayOutputStream()
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            withTimeout(1_500) {
                val response = connection.run(command, maxRetries = 1)
                assertEquals("", response.value)
                assertTrue(
                    response.rawResponse.elapsedTime >= 450,
                    "expected at least ~500ms, was ${response.rawResponse.elapsedTime}ms",
                )
                assertTrue(
                    response.rawResponse.elapsedTime < 1_200,
                    "expected legacy timeout below 1200ms, was ${response.rawResponse.elapsedTime}ms",
                )
            }
        }

    @Test
    fun `uses explicit read policy timeout when no data is available`() =
        runBlocking {
            val input = IdleInputStream()
            val output = ByteArrayOutputStream()
            val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            withTimeout(500) {
                val response =
                    connection.runWithReadPolicy(
                        command,
                        readPolicy = ObdReadPolicy(responseTimeoutMs = 40, interByteTimeoutMs = 25),
                    )
                assertEquals("", response.value)
                assertTrue(
                    response.rawResponse.elapsedTime >= 30,
                    "expected explicit timeout near 40ms, was ${response.rawResponse.elapsedTime}ms",
                )
                assertTrue(
                    response.rawResponse.elapsedTime < 250,
                    "expected explicit timeout below 250ms, was ${response.rawResponse.elapsedTime}ms",
                )
            }
        }
}

private class TestObdCommand(
    override val tag: String,
    override val pid: String,
    override val mode: String = "01",
) : ObdCommand() {
    override val name: String = tag
    override val skipDigitCheck: Boolean = true
    override val handler: (ObdRawResponse) -> String = { it.processedValue }
}

private data class ResponsePlan(
    val payload: String,
    val autoEnqueue: Boolean = true,
)

private class ScriptedInputStream : InputStream() {
    private val bytes = ArrayDeque<Int>()

    @Synchronized
    fun enqueue(payload: String) {
        payload.toByteArray().forEach { byte ->
            bytes.addLast(byte.toInt() and 0xFF)
        }
    }

    @Synchronized
    override fun available(): Int = bytes.size

    @Synchronized
    override fun read(): Int = if (bytes.isNotEmpty()) bytes.removeFirst() else -1
}

private class ScriptedOutputStream(
    private val input: ScriptedInputStream,
    private val responses: Map<String, ResponsePlan>,
) : OutputStream() {
    val writes = mutableListOf<String>()

    override fun write(b: Int): Unit = throw UnsupportedOperationException("write(Int) is not used in these tests")

    @Synchronized
    override fun write(
        b: ByteArray,
        off: Int,
        len: Int,
    ) {
        val rawCommand = String(b, off, len).trim()
        writes.add(rawCommand)

        val plan =
            responses[rawCommand]
                ?: throw IllegalArgumentException("Missing scripted response for command [$rawCommand]")

        if (plan.autoEnqueue) {
            input.enqueue(plan.payload)
        }
    }
}

private class DisconnectAfterPayloadInputStream(
    payload: String,
) : InputStream() {
    private val bytes = payload.toByteArray()
    private var index = 0
    private var eofReturned = false

    override fun available(): Int = if (index < bytes.size || !eofReturned) 1 else 0

    override fun read(): Int =
        if (index < bytes.size) {
            bytes[index++].toInt() and 0xFF
        } else {
            eofReturned = true
            -1
        }
}

private class IdleInputStream : InputStream() {
    override fun available(): Int = 0

    override fun read(): Int = -1
}

private suspend fun waitUntilWriteCount(
    output: ScriptedOutputStream,
    expectedCount: Int,
) {
    withTimeout(1_000) {
        while (output.writes.size < expectedCount) {
            delay(10)
        }
    }
}
