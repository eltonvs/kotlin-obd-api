package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdCommand


private fun calculate(rawValue: String): Int {
    val a = rawValue[2].toInt()
    val b = rawValue[3].toInt()
    return a * 256 + b
}

private fun calculatePercentage(rawValue: String): Float {
    val a = rawValue[2].toInt()
    return (a * 100f) / 255f
}

class FuelConsumptionRateCommand : ObdCommand() {
    override val tag = "FUEL_CONSUMPTION_RATE"
    override val name = "Fuel Consumption Rate"
    override val mode = "01"
    override val pid = "5E"

    override val defaultUnit = "L/h"
    override val handler = { x: String -> "%.1f".format(calculate(x) * 0.05) }
}

class FuelTypeCommand : ObdCommand() {
    override val tag = "FUEL_TYPE"
    override val name = "Fuel Type"
    override val mode = "01"
    override val pid = "51"

    override val handler = { x: String -> getFuelType(x[2].toInt()) }

    private fun getFuelType(code: Int): String = when (code) {
        0x01 -> "Gasoline"
        0x02 -> "Methanol"
        0x03 -> "Ethanol"
        0x04 -> "Diesel"
        0x05 -> "GPL/LGP"
        0x06 -> "Natural Gas"
        0x07 -> "Propane"
        0x08 -> "Electric"
        0x09 -> "Biodiesel + Gasoline"
        0x0A -> "Biodiesel + Methanol"
        0x0B -> "Biodiesel + Ethanol"
        0x0C -> "Biodiesel + GPL/LGP"
        0x0D -> "Biodiesel + Natural Gas"
        0x0E -> "Biodiesel + Propane"
        0x0F -> "Biodiesel + Electric"
        0x10 -> "Biodiesel + Gasoline/Electric"
        0x11 -> "Hybrid Gasoline"
        0x12 -> "Hybrid Ethanol"
        0x13 -> "Hybrid Diesel"
        0x14 -> "Hybrid Electric"
        0x15 -> "Hybrid Mixed"
        0x16 -> "Hybrid Regenerative"
        else -> "Unknown"
    }
}

class FuelLevelCommand : ObdCommand() {
    override val tag = "FUEL_LEVEL"
    override val name = "Fuel Level"
    override val mode = "01"
    override val pid = "2F"

    override val defaultUnit = "%"
    override val handler = { x: String -> "%.1f".format(calculatePercentage(x)) }
}

class EthanolLevelCommand : ObdCommand() {
    override val tag = "ETHANOL_LEVEL"
    override val name = "Ethanol Level"
    override val mode = "01"
    override val pid = "52"

    override val defaultUnit = "%"
    override val handler = { x: String -> "%.1f".format(calculatePercentage(x)) }
}

class FuelTrimCommand(private val fuelTrimBank: FuelTrimBank) : ObdCommand() {
    override val tag = fuelTrimBank.name
    override val name = fuelTrimBank.displayName
    override val mode = "01"
    override val pid = fuelTrimBank.pid

    override val defaultUnit = "%"
    override val handler = { x: String -> "%.1f".format((100f / 128) * x[2].toInt() - 100) }

    enum class FuelTrimBank(val displayName: String, val pid: String) {
        SHORT_TERM_BANK_1("Short Term Fuel Trim Bank 1", "06"),
        SHORT_TERM_BANK_2("Short Term Fuel Trim Bank 2", "07"),
        LONG_TERM_BANK_1("Long Term Fuel Trim Bank 1", "08"),
        LONG_TERM_BANK_2("Long Term Fuel Trim Bank 2", "09"),
    }
}