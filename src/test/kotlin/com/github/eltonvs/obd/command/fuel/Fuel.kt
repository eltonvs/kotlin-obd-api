package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class FuelConsumptionRateCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("415E10E3", 216.2f),
            arrayOf("415E1234", 233f),
            arrayOf("415E0000", 0f),
            arrayOf("415EFFFF", 3276.75f)
        )
    }

    @Test
    fun `test valid fuel consumption responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelConsumptionRateCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1fL/h".format(expected), obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class FuelTypeCommandParameterizedTests(private val rawValue: String, private val expected: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("415100", "Not Available"),
            arrayOf("415101", "Gasoline"),
            arrayOf("415102", "Methanol"),
            arrayOf("415103", "Ethanol"),
            arrayOf("415104", "Diesel"),
            arrayOf("415105", "GPL/LGP"),
            arrayOf("415106", "Natural Gas"),
            arrayOf("415107", "Propane"),
            arrayOf("415108", "Electric"),
            arrayOf("415109", "Biodiesel + Gasoline"),
            arrayOf("41510A", "Biodiesel + Methanol"),
            arrayOf("41510B", "Biodiesel + Ethanol"),
            arrayOf("41510C", "Biodiesel + GPL/LGP"),
            arrayOf("41510D", "Biodiesel + Natural Gas"),
            arrayOf("41510E", "Biodiesel + Propane"),
            arrayOf("41510F", "Biodiesel + Electric"),
            arrayOf("415110", "Biodiesel + Gasoline/Electric"),
            arrayOf("415111", "Hybrid Gasoline"),
            arrayOf("415112", "Hybrid Ethanol"),
            arrayOf("415113", "Hybrid Diesel"),
            arrayOf("415114", "Hybrid Electric"),
            arrayOf("415115", "Hybrid Mixed"),
            arrayOf("415116", "Hybrid Regenerative"),
            arrayOf("415116FF", "Hybrid Regenerative"),
            arrayOf("4151FF", "Unknown"),
            arrayOf("4151FFFF", "Unknown")
        )
    }

    @Test
    fun `test valid fuel type responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelTypeCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals(expected, obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class GenericFuelLevelCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("412F10", 6.3f),
            arrayOf("412FC8", 78.4f),
            arrayOf("412F00", 0f),
            arrayOf("412FFF", 100f),
            arrayOf("412FFFFF", 100f)
        )
    }

    @Test
    fun `test valid fuel level responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelLevelCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid ethanol level responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = EthanolLevelCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}


@RunWith(Parameterized::class)
class GenericFuelTrimCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() = listOf(
            arrayOf("410610", -87.5f),
            arrayOf("410643", -47.7f),
            arrayOf("410680", 0f),
            arrayOf("4106C8", 56.25f),
            arrayOf("410600", -100f),
            arrayOf("4106FF", 99.2f),
            arrayOf("4106FFFF", 99.2f)
        )
    }

    @Test
    fun `test valid fuel trim short term bank 1 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelTrimCommand(FuelTrimCommand.FuelTrimBank.SHORT_TERM_BANK_1).run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid fuel trim short term bank 2 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelTrimCommand(FuelTrimCommand.FuelTrimBank.SHORT_TERM_BANK_2).run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid fuel trim long term bank 1 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelTrimCommand(FuelTrimCommand.FuelTrimBank.LONG_TERM_BANK_1).run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid fuel trim long term bank 2 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse = FuelTrimCommand(FuelTrimCommand.FuelTrimBank.LONG_TERM_BANK_2).run {
            handleResponse(rawResponse)
        }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}