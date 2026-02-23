package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayDeque
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ObdDeviceConnectionTest {

    @Test
    fun `runs one command at a time when invoked concurrently`() = runBlocking {
        val input = ScriptedInputStream()
        val output = ScriptedOutputStream(
            input = input,
            responses = mapOf(
                "01 0D" to ResponsePlan(payload = "410D40>", autoEnqueue = false),
                "01 0C" to ResponsePlan(payload = "410C1AF8>")
            )
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
        val second = async {
            secondStarted.complete(Unit)
            connection.run(rpmCommand)
        }
        secondStarted.await()
        delay(100)
        assertEquals(listOf("01 0D"), output.writes)

        input.enqueue("410D40>")
        first.await()
        second.await()
        assertEquals(listOf("01 0D", "01 0C"), output.writes)
    }

    @Test
    fun `uses cache safely when called concurrently`() = runBlocking {
        val input = ScriptedInputStream()
        val output = ScriptedOutputStream(
            input = input,
            responses = mapOf("01 05" to ResponsePlan(payload = "41057B>"))
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
    fun `handles EOF while reading response`() = runBlocking {
        val input = DisconnectAfterPayloadInputStream(payload = "410D40")
        val output = ByteArrayOutputStream()
        val connection = ObdDeviceConnection(input, output, Dispatchers.Default)
        val command = TestObdCommand(tag = "SPEED", pid = "0D")

        val response = connection.run(command)

        assertEquals("410D40", response.value)
        assertEquals("410D40", response.rawResponse.value)
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
}

private class TestObdCommand(
    override val tag: String,
    override val pid: String,
    override val mode: String = "01"
) : ObdCommand() {
    override val name: String = tag
    override val skipDigitCheck: Boolean = true
    override val handler: (ObdRawResponse) -> String = { it.processedValue }
}

private data class ResponsePlan(
    val payload: String,
    val autoEnqueue: Boolean = true
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
    private val responses: Map<String, ResponsePlan>
) : OutputStream() {
    val writes = mutableListOf<String>()

    override fun write(b: Int) {
        throw UnsupportedOperationException("write(Int) is not used in these tests")
    }

    @Synchronized
    override fun write(b: ByteArray, off: Int, len: Int) {
        val rawCommand = String(b, off, len).trim()
        writes.add(rawCommand)

        val plan = responses[rawCommand]
            ?: throw IllegalArgumentException("Missing scripted response for command [$rawCommand]")

        if (plan.autoEnqueue) {
            input.enqueue(plan.payload)
        }
    }
}

private class DisconnectAfterPayloadInputStream(payload: String) : InputStream() {
    private val bytes = payload.toByteArray()
    private var index = 0
    private var eofReturned = false

    override fun available(): Int {
        return if (index < bytes.size || !eofReturned) 1 else 0
    }

    override fun read(): Int {
        return if (index < bytes.size) {
            bytes[index++].toInt() and 0xFF
        } else {
            eofReturned = true
            -1
        }
    }
}

private class IdleInputStream : InputStream() {
    override fun available(): Int = 0

    override fun read(): Int = -1
}
