package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt

private const val RATIO_BYTES_TO_PROCESS = 2
private const val RATIO_SCALE = 2f
private const val RATIO_DIVISOR = 65_536f

private fun calculateFuelAirRatio(rawValue: IntArray): Float =
    bytesToInt(rawValue, bytesToProcess = RATIO_BYTES_TO_PROCESS) * (RATIO_SCALE / RATIO_DIVISOR)

class CommandedEquivalenceRatioCommand : ObdCommand() {
    override val tag = "COMMANDED_EQUIVALENCE_RATIO"
    override val name = "Fuel-Air Commanded Equivalence Ratio"
    override val mode = "01"
    override val pid = "44"

    override val defaultUnit = "F/A"
    override val handler = { response: ObdRawResponse -> "%.2f".format(calculateFuelAirRatio(response.bufferedValue)) }
}

class FuelAirEquivalenceRatioCommand(
    oxygenSensor: OxygenSensor,
) : ObdCommand() {
    override val tag = "FUEL_AIR_EQUIVALENCE_RATIO_${oxygenSensor.name}"
    override val name = "Fuel-Air Equivalence Ratio - ${oxygenSensor.displayName}"
    override val mode = "01"
    override val pid = oxygenSensor.pid

    override val defaultUnit = "F/A"
    override val handler = { response: ObdRawResponse -> "%.2f".format(calculateFuelAirRatio(response.bufferedValue)) }

    enum class OxygenSensor(
        val displayName: String,
        internal val pid: String,
    ) {
        OXYGEN_SENSOR_1("Oxygen Sensor 1", "34"),
        OXYGEN_SENSOR_2("Oxygen Sensor 2", "35"),
        OXYGEN_SENSOR_3("Oxygen Sensor 3", "36"),
        OXYGEN_SENSOR_4("Oxygen Sensor 4", "37"),
        OXYGEN_SENSOR_5("Oxygen Sensor 5", "38"),
        OXYGEN_SENSOR_6("Oxygen Sensor 6", "39"),
        OXYGEN_SENSOR_7("Oxygen Sensor 7", "3A"),
        OXYGEN_SENSOR_8("Oxygen Sensor 8", "3B"),
    }
}
