package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.calculatePercentage
import com.github.eltonvs.obd.command.formatFloat

private const val SINGLE_BYTE = 1
private const val FUEL_CONSUMPTION_FACTOR = 0.05
private const val HUNDRED_PERCENT = 100f
private const val HALF_SCALE = 128f

private const val FUEL_CODE_NOT_AVAILABLE = 0x00
private const val FUEL_CODE_GASOLINE = 0x01
private const val FUEL_CODE_METHANOL = 0x02
private const val FUEL_CODE_ETHANOL = 0x03
private const val FUEL_CODE_DIESEL = 0x04
private const val FUEL_CODE_GPL = 0x05
private const val FUEL_CODE_NATURAL_GAS = 0x06
private const val FUEL_CODE_PROPANE = 0x07
private const val FUEL_CODE_ELECTRIC = 0x08
private const val FUEL_CODE_BIODIESEL_GASOLINE = 0x09
private const val FUEL_CODE_BIODIESEL_METHANOL = 0x0A
private const val FUEL_CODE_BIODIESEL_ETHANOL = 0x0B
private const val FUEL_CODE_BIODIESEL_GPL = 0x0C
private const val FUEL_CODE_BIODIESEL_NATURAL_GAS = 0x0D
private const val FUEL_CODE_BIODIESEL_PROPANE = 0x0E
private const val FUEL_CODE_BIODIESEL_ELECTRIC = 0x0F
private const val FUEL_CODE_BIODIESEL_GASOLINE_ELECTRIC = 0x10
private const val FUEL_CODE_HYBRID_GASOLINE = 0x11
private const val FUEL_CODE_HYBRID_ETHANOL = 0x12
private const val FUEL_CODE_HYBRID_DIESEL = 0x13
private const val FUEL_CODE_HYBRID_ELECTRIC = 0x14
private const val FUEL_CODE_HYBRID_MIXED = 0x15
private const val FUEL_CODE_HYBRID_REGENERATIVE = 0x16

class FuelConsumptionRateCommand : ObdCommand() {
    override val tag = "FUEL_CONSUMPTION_RATE"
    override val name = "Fuel Consumption Rate"
    override val mode = "01"
    override val pid = "5E"

    override val defaultUnit = "L/h"
    override val handler = { response: ObdRawResponse ->
        formatFloat((bytesToInt(response.bufferedValue) * FUEL_CONSUMPTION_FACTOR).toFloat(), 1)
    }
}

class FuelTypeCommand : ObdCommand() {
    override val tag = "FUEL_TYPE"
    override val name = "Fuel Type"
    override val mode = "01"
    override val pid = "51"

    override val handler = { response: ObdRawResponse ->
        getFuelType(bytesToInt(response.bufferedValue, bytesToProcess = SINGLE_BYTE).toInt())
    }

    private fun getFuelType(code: Int): String = FUEL_TYPE_BY_CODE[code] ?: "Unknown"

    private companion object {
        private val FUEL_TYPE_BY_CODE =
            mapOf(
                FUEL_CODE_NOT_AVAILABLE to "Not Available",
                FUEL_CODE_GASOLINE to "Gasoline",
                FUEL_CODE_METHANOL to "Methanol",
                FUEL_CODE_ETHANOL to "Ethanol",
                FUEL_CODE_DIESEL to "Diesel",
                FUEL_CODE_GPL to "GPL/LGP",
                FUEL_CODE_NATURAL_GAS to "Natural Gas",
                FUEL_CODE_PROPANE to "Propane",
                FUEL_CODE_ELECTRIC to "Electric",
                FUEL_CODE_BIODIESEL_GASOLINE to "Biodiesel + Gasoline",
                FUEL_CODE_BIODIESEL_METHANOL to "Biodiesel + Methanol",
                FUEL_CODE_BIODIESEL_ETHANOL to "Biodiesel + Ethanol",
                FUEL_CODE_BIODIESEL_GPL to "Biodiesel + GPL/LGP",
                FUEL_CODE_BIODIESEL_NATURAL_GAS to "Biodiesel + Natural Gas",
                FUEL_CODE_BIODIESEL_PROPANE to "Biodiesel + Propane",
                FUEL_CODE_BIODIESEL_ELECTRIC to "Biodiesel + Electric",
                FUEL_CODE_BIODIESEL_GASOLINE_ELECTRIC to "Biodiesel + Gasoline/Electric",
                FUEL_CODE_HYBRID_GASOLINE to "Hybrid Gasoline",
                FUEL_CODE_HYBRID_ETHANOL to "Hybrid Ethanol",
                FUEL_CODE_HYBRID_DIESEL to "Hybrid Diesel",
                FUEL_CODE_HYBRID_ELECTRIC to "Hybrid Electric",
                FUEL_CODE_HYBRID_MIXED to "Hybrid Mixed",
                FUEL_CODE_HYBRID_REGENERATIVE to "Hybrid Regenerative",
            )
    }
}

class FuelLevelCommand : ObdCommand() {
    override val tag = "FUEL_LEVEL"
    override val name = "Fuel Level"
    override val mode = "01"
    override val pid = "2F"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        formatFloat(calculatePercentage(response.bufferedValue, bytesToProcess = SINGLE_BYTE), 1)
    }
}

class EthanolLevelCommand : ObdCommand() {
    override val tag = "ETHANOL_LEVEL"
    override val name = "Ethanol Level"
    override val mode = "01"
    override val pid = "52"

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        formatFloat(calculatePercentage(response.bufferedValue, bytesToProcess = SINGLE_BYTE), 1)
    }
}

class FuelTrimCommand(
    fuelTrimBank: FuelTrimBank,
) : ObdCommand() {
    override val tag = fuelTrimBank.name
    override val name = fuelTrimBank.displayName
    override val mode = "01"
    override val pid = fuelTrimBank.pid

    override val defaultUnit = "%"
    override val handler = { response: ObdRawResponse ->
        val value = bytesToInt(response.bufferedValue, bytesToProcess = SINGLE_BYTE)
        val normalized = value * (HUNDRED_PERCENT / HALF_SCALE) - HUNDRED_PERCENT
        formatFloat(normalized, 1)
    }

    enum class FuelTrimBank(
        val displayName: String,
        internal val pid: String,
    ) {
        SHORT_TERM_BANK_1("Short Term Fuel Trim Bank 1", "06"),
        SHORT_TERM_BANK_2("Short Term Fuel Trim Bank 2", "07"),
        LONG_TERM_BANK_1("Long Term Fuel Trim Bank 1", "08"),
        LONG_TERM_BANK_2("Long Term Fuel Trim Bank 2", "09"),
    }
}
