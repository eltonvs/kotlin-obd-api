package com.github.eltonvs.obd.command.pressure

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt


class BarometricPressureCommand : ObdCommand() {
    override val tag = "BAROMETRIC_PRESSURE"
    override val name = "Barometric Pressure"
    override val mode = "01"
    override val pid = "33"

    override val defaultUnit = "kPa"
    override val handler = { it: ObdRawResponse -> bytesToInt(it.bufferedValue, bytesToProcess = 1).toString() }
}

class IntakeManifoldPressureCommand : ObdCommand() {
    override val tag = "INTAKE_MANIFOLD_PRESSURE"
    override val name = "Intake Manifold Pressure"
    override val mode = "01"
    override val pid = "0B"

    override val defaultUnit = "kPa"
    override val handler = { it: ObdRawResponse -> bytesToInt(it.bufferedValue, bytesToProcess = 1).toString() }
}

class FuelPressureCommand : ObdCommand() {
    override val tag = "FUEL_PRESSURE"
    override val name = "Fuel Pressure"
    override val mode = "01"
    override val pid = "0A"

    override val defaultUnit = "kPa"
    override val handler = { it: ObdRawResponse -> (bytesToInt(it.bufferedValue, bytesToProcess = 1) * 3).toString() }
}

class FuelRailPressureCommand : ObdCommand() {
    override val tag = "FUEL_RAIL_PRESSURE"
    override val name = "Fuel Rail Pressure"
    override val mode = "01"
    override val pid = "22"

    override val defaultUnit = "kPa"
    override val handler = { it: ObdRawResponse -> "%.3f".format(bytesToInt(it.bufferedValue) * 0.079) }
}

class FuelRailGaugePressureCommand : ObdCommand() {
    override val tag = "FUEL_RAIL_GAUGE_PRESSURE"
    override val name = "Fuel Rail Gauge Pressure"
    override val mode = "01"
    override val pid = "23"

    override val defaultUnit = "kPa"
    override val handler = { it: ObdRawResponse -> (bytesToInt(it.bufferedValue) * 10).toString() }
}
