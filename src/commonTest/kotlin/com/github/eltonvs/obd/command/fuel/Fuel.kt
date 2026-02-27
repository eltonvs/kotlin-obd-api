package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class FuelConsumptionRateCommandTests {
    @Test
    fun `test valid fuel consumption responses handler`() {
        listOf(
            "415E10E3" to 216.2f,
            "415E1234" to 233f,
            "415E0000" to 0f,
            "415EFFFF" to 3276.75f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelConsumptionRateCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 1) + "L/h", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class FuelTypeCommandTests {
    @Test
    fun `test valid fuel type responses handler`() {
        listOf(
            "415100" to "Not Available",
            "415101" to "Gasoline",
            "415102" to "Methanol",
            "415103" to "Ethanol",
            "415104" to "Diesel",
            "415105" to "GPL/LGP",
            "415106" to "Natural Gas",
            "415107" to "Propane",
            "415108" to "Electric",
            "415109" to "Biodiesel + Gasoline",
            "41510A" to "Biodiesel + Methanol",
            "41510B" to "Biodiesel + Ethanol",
            "41510C" to "Biodiesel + GPL/LGP",
            "41510D" to "Biodiesel + Natural Gas",
            "41510E" to "Biodiesel + Propane",
            "41510F" to "Biodiesel + Electric",
            "415110" to "Biodiesel + Gasoline/Electric",
            "415111" to "Hybrid Gasoline",
            "415112" to "Hybrid Ethanol",
            "415113" to "Hybrid Diesel",
            "415114" to "Hybrid Electric",
            "415115" to "Hybrid Mixed",
            "415116" to "Hybrid Regenerative",
            "415116FF" to "Hybrid Regenerative",
            "4151FF" to "Unknown",
            "4151FFFF" to "Unknown",
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelTypeCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(expected, obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class GenericFuelLevelCommandTests {
    @Test
    fun `test valid fuel level responses handler`() {
        listOf(
            "412F10" to 6.3f,
            "412FC8" to 78.4f,
            "412F00" to 0f,
            "412FFF" to 100f,
            "412FFFFF" to 100f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelLevelCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 1) + '%', obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test valid ethanol level responses handler`() {
        listOf(
            "412F10" to 6.3f,
            "412FC8" to 78.4f,
            "412F00" to 0f,
            "412FFF" to 100f,
            "412FFFFF" to 100f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                EthanolLevelCommand().run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 1) + '%', obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class GenericFuelTrimCommandTests {
    @Test
    fun `test valid fuel trim short term bank 1 responses handler`() {
        listOf(
            "410610" to -87.5f,
            "410643" to -47.7f,
            "410680" to 0f,
            "4106C8" to 56.25f,
            "410600" to -100f,
            "4106FF" to 99.2f,
            "4106FFFF" to 99.2f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelTrimCommand(FuelTrimCommand.FuelTrimBank.SHORT_TERM_BANK_1).run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 1) + '%', obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test valid fuel trim short term bank 2 responses handler`() {
        listOf(
            "410610" to -87.5f,
            "410643" to -47.7f,
            "410680" to 0f,
            "4106C8" to 56.25f,
            "410600" to -100f,
            "4106FF" to 99.2f,
            "4106FFFF" to 99.2f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelTrimCommand(FuelTrimCommand.FuelTrimBank.SHORT_TERM_BANK_2).run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 1) + '%', obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test valid fuel trim long term bank 1 responses handler`() {
        listOf(
            "410610" to -87.5f,
            "410643" to -47.7f,
            "410680" to 0f,
            "4106C8" to 56.25f,
            "410600" to -100f,
            "4106FF" to 99.2f,
            "4106FFFF" to 99.2f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelTrimCommand(FuelTrimCommand.FuelTrimBank.LONG_TERM_BANK_1).run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 1) + '%', obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test valid fuel trim long term bank 2 responses handler`() {
        listOf(
            "410610" to -87.5f,
            "410643" to -47.7f,
            "410680" to 0f,
            "4106C8" to 56.25f,
            "410600" to -100f,
            "4106FF" to 99.2f,
            "4106FFFF" to 99.2f,
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse =
                FuelTrimCommand(FuelTrimCommand.FuelTrimBank.LONG_TERM_BANK_2).run {
                    handleResponse(rawResponse)
                }
            assertEquals(formatFloat(expected, 1) + '%', obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
