package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.Monitors
import com.github.eltonvs.obd.command.ObdRawResponse
import kotlin.test.Test
import kotlin.test.assertEquals

private val completeStatus = SensorStatus(available = true, complete = true)
private val incompleteStatus = SensorStatus(available = true, complete = false)
private val notAvailableCompleteStatus = SensorStatus(available = false, complete = true)
private val notAvailableIncompleteStatus = SensorStatus(available = false, complete = false)

private val expected1 =
    SensorStatusData(
        milOn = true,
        dtcCount = 3,
        isSpark = true,
        items =
            Monitors
                .entries
                .filter { it.isSparkIgnition ?: true }
                .map { it to completeStatus }
                .toMap(),
    )
private val expected2 =
    SensorStatusData(
        milOn = false,
        dtcCount = 0,
        isSpark = false,
        items =
            mapOf(
                Monitors.MISFIRE to incompleteStatus,
                Monitors.FUEL_SYSTEM to notAvailableIncompleteStatus,
                Monitors.COMPREHENSIVE_COMPONENT to notAvailableIncompleteStatus,
                Monitors.NMHC_CATALYST to incompleteStatus,
                Monitors.NOX_SCR_MONITOR to incompleteStatus,
                Monitors.BOOST_PRESSURE to notAvailableCompleteStatus,
                Monitors.EXHAUST_GAS_SENSOR to notAvailableCompleteStatus,
                Monitors.PM_FILTER to notAvailableCompleteStatus,
                Monitors.EGR_VVT_SYSTEM to notAvailableCompleteStatus,
            ),
    )
private val expected3 =
    SensorStatusData(
        milOn = false,
        dtcCount = 0,
        isSpark = true,
        items =
            mapOf(
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
                Monitors.AC_REFRIGERANT to notAvailableCompleteStatus,
            ),
    )
private val expected4 =
    SensorStatusData(
        milOn = false,
        dtcCount = 0,
        isSpark = true,
        items =
            Monitors
                .entries
                .filter { it.isSparkIgnition ?: true }
                .map { it to completeStatus }
                .toMap(),
    )
private val expected5 =
    SensorStatusData(
        milOn = false,
        dtcCount = 0,
        isSpark = false,
        items =
            mapOf(
                Monitors.FUEL_SYSTEM to notAvailableCompleteStatus,
                Monitors.NMHC_CATALYST to incompleteStatus,
                Monitors.EXHAUST_GAS_SENSOR to incompleteStatus,
                Monitors.MISFIRE to notAvailableCompleteStatus,
                Monitors.PM_FILTER to notAvailableCompleteStatus,
                Monitors.BOOST_PRESSURE to notAvailableCompleteStatus,
                Monitors.EGR_VVT_SYSTEM to notAvailableCompleteStatus,
                Monitors.NOX_SCR_MONITOR to notAvailableCompleteStatus,
                Monitors.COMPREHENSIVE_COMPONENT to notAvailableIncompleteStatus,
            ),
    )

class MonitorStatusSinceCodesClearedCommandTests {
    @Test
    fun `test valid monitor status since CC responses handler`() {
        listOf(
            "41018307FF00" to expected1,
            "41 01 83 07 FF 00" to expected1,
            "8307FF00" to expected1,
            "410100790303" to expected2,
            "41 01 00 79 03 03" to expected2,
            "00790303" to expected2,
            "41010007EBC8" to expected3,
            "41 01 00 07 EB C8" to expected3,
            "0007EBC8" to expected3,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                MonitorStatusSinceCodesClearedCommand().also {
                    it.handleResponse(rawResponse)
                }
            assertEquals(expected, obdResponse.data, "Failed for: $rawValue")
        }
    }
}

class MonitorStatusCurrentDriveCycleCommandTests {
    @Test
    fun `test valid monitor status current drive cycle responses handler`() {
        listOf(
            "41410007FF00" to expected4,
            "41 41 00 07 FF 00" to expected4,
            "0007FF00" to expected4,
            "414100790303" to expected2,
            "41 41 00 79 03 03" to expected2,
            "00790303" to expected2,
            "414100482135" to expected5,
            "41 41 00 48 21 35" to expected5,
            "00482135" to expected5,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                MonitorStatusCurrentDriveCycleCommand().also {
                    it.handleResponse(rawResponse)
                }
            assertEquals(expected, obdResponse.data, "Failed for: $rawValue")
        }
    }
}
