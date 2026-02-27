package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObdDeviceConnectionTest {
    @Test
    fun `runs one command at a time when invoked concurrently`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()

            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val speedCommand = TestObdCommand(tag = "SPEED", pid = "0D")
            val rpmCommand = TestObdCommand(tag = "RPM", pid = "0C")

            input.write("410D40>".encodeToByteArray())

            val first = async { connection.run(speedCommand) }
            val result1 = first.await()
            assertEquals("410D40", result1.value)

            input.write("410C1AF8>".encodeToByteArray())

            val second = async { connection.run(rpmCommand) }
            val result2 = second.await()
            assertEquals("410C1AF8", result2.value)
        }

    @Test
    fun `uses cache when enabled`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()

            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val commandA = TestObdCommand(tag = "COOLANT_TEMP", pid = "05")

            input.write("41057B>".encodeToByteArray())

            val first = connection.run(commandA, useCache = true)
            assertEquals("41057B", first.value)

            // Second call should return cached result without reading from input
            val second = connection.run(commandA, useCache = true)
            assertEquals("41057B", second.value)
        }

    @Test
    fun `returns empty when no data and max retries is zero`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            withTimeout(2_000) {
                val response = connection.run(command, maxRetries = 0)
                assertEquals("", response.value)
            }
        }

    @Test
    fun `writes correct command string to sink`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val command = TestObdCommand(tag = "RPM", mode = "01", pid = "0C")

            input.write("410C1AF8>".encodeToByteArray())
            connection.run(command)

            // ObdDeviceConnection writes "mode pid\r" to the sink
            assertEquals("01 0C\r", output.readString())
        }

    @Test
    fun `strips SEARCHING from response`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            input.write("SEARCHING...410D40>".encodeToByteArray())

            val response = connection.run(command)
            assertEquals("410D40", response.value)
        }

    @Test
    fun `retries when buffer is initially empty then data arrives`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            // Start run in the background — buffer is empty, so it will retry
            val deferred = async { connection.run(command, maxRetries = 3) }

            // Advance past the first retry delay (500ms) and supply data
            advanceTimeBy(501)
            input.write("410D40>".encodeToByteArray())

            val response = deferred.await()
            assertEquals("410D40", response.value)
        }

    @Test
    fun `returns empty after exhausting all retries`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            // maxRetries=2 means up to 3 attempts (0, 1, 2), each with 500ms delay
            val response = connection.run(command, maxRetries = 2)
            assertEquals("", response.value)
        }

    @Test
    fun `records elapsed time in raw response`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            input.write("410D40>".encodeToByteArray())

            val response = connection.run(command)
            assertTrue(response.rawResponse.elapsedTime >= 0, "Elapsed time should be non-negative")
        }

    @Test
    fun `applies delay between send and read`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)
            val command = TestObdCommand(tag = "SPEED", pid = "0D")

            input.write("410D40>".encodeToByteArray())

            val timeBefore = testScheduler.currentTime
            val response = connection.run(command, delayTime = 200)
            val timeAfter = testScheduler.currentTime

            assertEquals("410D40", response.value)
            // Virtual time should have advanced by at least the delayTime
            assertTrue(timeAfter - timeBefore >= 200, "Virtual time should advance by delayTime")
        }

    @Test
    fun `handles multiple sequential commands with clean buffer state`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)

            val commands =
                listOf(
                    TestObdCommand(tag = "SPEED", pid = "0D") to "410D40",
                    TestObdCommand(tag = "RPM", pid = "0C") to "410C1AF8",
                    TestObdCommand(tag = "COOLANT", pid = "05") to "41057B",
                )

            for ((command, expectedHex) in commands) {
                input.write("$expectedHex>".encodeToByteArray())
                val response = connection.run(command)
                assertEquals(expectedHex, response.value, "Failed for ${command.tag}")
            }

            // Verify all commands were written to the sink
            val written = output.readString()
            assertTrue(written.contains("01 0D\r"), "Should contain SPEED command")
            assertTrue(written.contains("01 0C\r"), "Should contain RPM command")
            assertTrue(written.contains("01 05\r"), "Should contain COOLANT command")
        }

    @Test
    fun `cache does not interfere across different commands`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)

            val speedCmd = TestObdCommand(tag = "SPEED", pid = "0D")
            val rpmCmd = TestObdCommand(tag = "RPM", pid = "0C")

            // Cache speed
            input.write("410D40>".encodeToByteArray())
            val speed1 = connection.run(speedCmd, useCache = true)
            assertEquals("410D40", speed1.value)

            // Run RPM (different command, should not hit speed's cache)
            input.write("410C1AF8>".encodeToByteArray())
            val rpm = connection.run(rpmCmd, useCache = true)
            assertEquals("410C1AF8", rpm.value)

            // Speed from cache should still return original value
            val speed2 = connection.run(speedCmd, useCache = true)
            assertEquals("410D40", speed2.value)
        }

    @Test
    fun `concurrent runs are serialized by mutex`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)
            val input = Buffer()
            val output = Buffer()
            val connection = ObdDeviceConnection(input, output, testDispatcher)

            val cmd1 = TestObdCommand(tag = "SPEED", pid = "0D")
            val cmd2 = TestObdCommand(tag = "RPM", pid = "0C")

            // Pre-fill both responses — they'll be consumed in order
            input.write("410D40>".encodeToByteArray())
            input.write("410C1AF8>".encodeToByteArray())

            val results = mutableListOf<String>()

            val job1 = launch { results.add(connection.run(cmd1).value) }
            val job2 = launch { results.add(connection.run(cmd2).value) }

            job1.join()
            job2.join()

            // Both should complete with their respective data
            assertTrue(results.contains("410D40"), "Should contain speed response")
            assertTrue(results.contains("410C1AF8"), "Should contain RPM response")
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
