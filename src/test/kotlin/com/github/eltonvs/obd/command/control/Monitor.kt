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

private val expected1 = SensorStatusData(
    milOn = true,
    dtcCount = 3,
    isSpark = true,
    items = Monitors.values().filter { it.isSparkIgnition ?: true }.map { it to completeStatus }.toMap()
)
private val expected2 = SensorStatusData(
    milOn = false,
    dtcCount = 0,
    isSpark = false,
    items = mapOf(
        Monitors.MISFIRE to incompleteStatus,
        Monitors.FUEL_SYSTEM to notAvailableIncompleteStatus,
        Monitors.COMPREHENSIVE_COMPONENT to notAvailableIncompleteStatus,
        Monitors.NMHC_CATALYST to incompleteStatus,
        Monitors.NOX_SCR_MONITOR to incompleteStatus,
        Monitors.BOOST_PRESSURE to notAvailableCompleteStatus,
        Monitors.EXHAUST_GAS_SENSOR to notAvailableCompleteStatus,
        Monitors.PM_FILTER to notAvailableCompleteStatus,
        Monitors.EGR_VVT_SYSTEM to notAvailableCompleteStatus
    )
)
private val expected3 = SensorStatusData(
    milOn = false,
    dtcCount = 0,
    isSpark = true,
    items = mapOf(
        Monitors.CATALYST to completeStatus,
        Monitors.EGR_SYSTEM to incompleteStatus,
        Monitors.SECONDARY_AIR_SYSTEM to incompleteStatus,
        Monitors.COMPREHENSIVE_COMPONENT to completeStatus,
        Monitors.OXYGEN_SENSOR_HEATER to incompleteStatus,
        Monitors.HEATED_CATALYST to completeStatus,
        Monitors.FUEL_SYSTEM to completeStatus,
        Monitors.OXYGEN_SENSOR to completeStatus,
        Monitors.MISFIRE to completeStatus,
        Monitors.EVAPORATIVE_SYSTEM to notAvailableCompleteStatus,
        Monitors.AC_REFRIGERANT to notAvailableCompleteStatus
    )
)
private val expected4 = SensorStatusData(
    milOn = false,
    dtcCount = 0,
    isSpark = true,
    items = Monitors.values().filter { it.isSparkIgnition ?: true }.map { it to completeStatus }.toMap()
)
private val expected5 = SensorStatusData(
    milOn = false,
    dtcCount = 0,
    isSpark = false,
    items = mapOf(
        Monitors.FUEL_SYSTEM to notAvailableCompleteStatus,
        Monitors.NMHC_CATALYST to incompleteStatus,
        Monitors.EXHAUST_GAS_SENSOR to incompleteStatus,
        Monitors.MISFIRE to notAvailableCompleteStatus,
        Monitors.PM_FILTER to notAvailableCompleteStatus,
        Monitors.BOOST_PRESSURE to notAvailableCompleteStatus,
        Monitors.EGR_VVT_SYSTEM to notAvailableCompleteStatus,
        Monitors.NOX_SCR_MONITOR to notAvailableCompleteStatus,
        Monitors.COMPREHENSIVE_COMPONENT to notAvailableIncompleteStatus
    )
)

@RunWith(Parameterized::class)
class MonitorStatusSinceCodesClearedCommandTests(private val rawValue: String, private val expected: SensorStatusData) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("41018307FF00", expected1),
            arrayOf("41 01 83 07 FF 00", expected1),
            arrayOf("8307FF00", expected1),
            arrayOf("410100790303", expected2),
            arrayOf("41 01 00 79 03 03", expected2),
            arrayOf("00790303", expected2),
            arrayOf("41010007EBC8", expected3),
            arrayOf("41 01 00 07 EB C8", expected3),
            arrayOf("0007EBC8", expected3)
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
            arrayOf("41410007FF00", expected4),
            arrayOf("41 41 00 07 FF 00", expected4),
            arrayOf("0007FF00", expected4),
            arrayOf("414100790303", expected2),
            arrayOf("41 41 00 79 03 03", expected2),
            arrayOf("00790303", expected2),
            arrayOf("414100482135", expected5),
            arrayOf("41 41 00 48 21 35", expected5),
            arrayOf("00482135", expected5)
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
