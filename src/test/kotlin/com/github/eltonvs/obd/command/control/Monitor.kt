package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.Monitors
import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

private val completeStatus = SensorStatus(available = true, complete = true)
private val incompleteStatus = SensorStatus(available = true, complete = false)
private val notAvailableCompleteStatus = SensorStatus(available = false, complete = true)
private val notAvailableIncompleteStatus = SensorStatus(available = false, complete = false)

@RunWith(Parameterized::class)
class MonitorStatusSinceCodesClearedCommandTests(private val rawValue: String, private val expected: SensorStatusData) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf(
                "8307FF00", SensorStatusData(
                    milOn = true,
                    dtcCount = 3,
                    isSpark = true,
                    items = Monitors.values().filter { it.isSparkIgnition ?: true }.map { it to completeStatus }.toMap()
                )
            ),
            arrayOf(
                "00790303", SensorStatusData(
                    milOn = false,
                    dtcCount = 0,
                    isSpark = false,
                    items = mapOf(
                        Monitors.MISFIRE to incompleteStatus,
                        Monitors.FUEL_SYSTEM to notAvailableIncompleteStatus,
                        Monitors.COMPREHENSIVE_COMPONENT to notAvailableIncompleteStatus,
                        Monitors.MNHC_CATALYST to incompleteStatus,
                        Monitors.NOX_SCR_MONITOR to incompleteStatus,
                        Monitors.BOOST_PRESSURE to notAvailableCompleteStatus,
                        Monitors.EXHAUST_GAS_SENSOR to notAvailableCompleteStatus,
                        Monitors.PM_FILTER to notAvailableCompleteStatus,
                        Monitors.EGR_VVT_SYSTEM to notAvailableCompleteStatus
                    )
                )
            )
        )
    }

    @Test
    fun `test valid monitor status since CC responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = MonitorStatusSinceCodesClearedCommand().also {
            it.handleResponse(rawResponse)
        }
        assertEquals(expected, obdResponse.data)
    }
}

@RunWith(Parameterized::class)
class MonitorStatusCurrentDriveCycleCommandTests(private val rawValue: String, private val expected: SensorStatusData) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf(
                "8307FF00", SensorStatusData(
                    milOn = true,
                    dtcCount = 3,
                    isSpark = true,
                    items = Monitors.values().filter { it.isSparkIgnition ?: true }.map { it to completeStatus }.toMap()
                )
            ),
            arrayOf(
                "00790303", SensorStatusData(
                    milOn = false,
                    dtcCount = 0,
                    isSpark = false,
                    items = mapOf(
                        Monitors.MISFIRE to incompleteStatus,
                        Monitors.FUEL_SYSTEM to notAvailableIncompleteStatus,
                        Monitors.COMPREHENSIVE_COMPONENT to notAvailableIncompleteStatus,
                        Monitors.MNHC_CATALYST to incompleteStatus,
                        Monitors.NOX_SCR_MONITOR to incompleteStatus,
                        Monitors.BOOST_PRESSURE to notAvailableCompleteStatus,
                        Monitors.EXHAUST_GAS_SENSOR to notAvailableCompleteStatus,
                        Monitors.PM_FILTER to notAvailableCompleteStatus,
                        Monitors.EGR_VVT_SYSTEM to notAvailableCompleteStatus
                    )
                )
            )
        )
    }

    @Test
    fun `test valid monitor status current drive cycle responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = MonitorStatusCurrentDriveCycleCommand().also {
            it.handleResponse(rawResponse)
        }
        assertEquals(expected, obdResponse.data)
    }
}
