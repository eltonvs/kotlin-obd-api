package com.github.eltonvs.obd.command.pressure

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt

private const val SINGLE_BYTE = 1
private const val FUEL_PRESSURE_MULTIPLIER = 3
private const val FUEL_RAIL_PRESSURE_FACTOR = 0.079
private const val FUEL_RAIL_GAUGE_PRESSURE_MULTIPLIER = 10

class BarometricPressureCommand : ObdCommand() {
    override val tag = "BAROMETRIC_PRESSURE"
    override val name = "Barometric Pressure"
    override val mode = "01"
    override val pid = "33"

    override val defaultUnit = "kPa"
    override val handler = { response: ObdRawResponse ->
        bytesToInt(response.bufferedValue, bytesToProcess = SINGLE_BYTE).toString()
    }
}

class IntakeManifoldPressureCommand : ObdCommand() {
    override val tag = "INTAKE_MANIFOLD_PRESSURE"
    override val name = "Intake Manifold Pressure"
    override val mode = "01"
    override val pid = "0B"

    override val defaultUnit = "kPa"
    override val handler = { response: ObdRawResponse ->
        bytesToInt(response.bufferedValue, bytesToProcess = SINGLE_BYTE).toString()
    }
}

class FuelPressureCommand : ObdCommand() {
    override val tag = "FUEL_PRESSURE"
    override val name = "Fuel Pressure"
    override val mode = "01"
    override val pid = "0A"

    override val defaultUnit = "kPa"
    override val handler = { response: ObdRawResponse ->
        (bytesToInt(response.bufferedValue, bytesToProcess = SINGLE_BYTE) * FUEL_PRESSURE_MULTIPLIER).toString()
    }
}

class FuelRailPressureCommand : ObdCommand() {
    override val tag = "FUEL_RAIL_PRESSURE"
    override val name = "Fuel Rail Pressure"
    override val mode = "01"
    override val pid = "22"

    override val defaultUnit = "kPa"
    override val handler = { response: ObdRawResponse ->
        "%.3f".format(bytesToInt(response.bufferedValue) * FUEL_RAIL_PRESSURE_FACTOR)
    }
}

class FuelRailGaugePressureCommand : ObdCommand() {
    override val tag = "FUEL_RAIL_GAUGE_PRESSURE"
    override val name = "Fuel Rail Gauge Pressure"
    override val mode = "01"
    override val pid = "23"

    override val defaultUnit = "kPa"
    override val handler = { response: ObdRawResponse ->
        (bytesToInt(response.bufferedValue) * FUEL_RAIL_GAUGE_PRESSURE_MULTIPLIER).toString()
    }
}
