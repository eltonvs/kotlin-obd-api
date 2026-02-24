package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class FuelConsumptionRateCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Float,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("415E10E3", 216.2f),
                arrayOf<Any>("415E1234", 233f),
                arrayOf<Any>("415E0000", 0f),
                arrayOf<Any>("415EFFFF", 3276.75f),
            )
    }

    @Test
    fun `test valid fuel consumption responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            FuelConsumptionRateCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1fL/h".format(expected), obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class FuelTypeCommandParameterizedTests(
    private val rawValue: String,
    private val expected: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("415100", "Not Available"),
                arrayOf<Any>("415101", "Gasoline"),
                arrayOf<Any>("415102", "Methanol"),
                arrayOf<Any>("415103", "Ethanol"),
                arrayOf<Any>("415104", "Diesel"),
                arrayOf<Any>("415105", "GPL/LGP"),
                arrayOf<Any>("415106", "Natural Gas"),
                arrayOf<Any>("415107", "Propane"),
                arrayOf<Any>("415108", "Electric"),
                arrayOf<Any>("415109", "Biodiesel + Gasoline"),
                arrayOf<Any>("41510A", "Biodiesel + Methanol"),
                arrayOf<Any>("41510B", "Biodiesel + Ethanol"),
                arrayOf<Any>("41510C", "Biodiesel + GPL/LGP"),
                arrayOf<Any>("41510D", "Biodiesel + Natural Gas"),
                arrayOf<Any>("41510E", "Biodiesel + Propane"),
                arrayOf<Any>("41510F", "Biodiesel + Electric"),
                arrayOf<Any>("415110", "Biodiesel + Gasoline/Electric"),
                arrayOf<Any>("415111", "Hybrid Gasoline"),
                arrayOf<Any>("415112", "Hybrid Ethanol"),
                arrayOf<Any>("415113", "Hybrid Diesel"),
                arrayOf<Any>("415114", "Hybrid Electric"),
                arrayOf<Any>("415115", "Hybrid Mixed"),
                arrayOf<Any>("415116", "Hybrid Regenerative"),
                arrayOf<Any>("415116FF", "Hybrid Regenerative"),
                arrayOf<Any>("4151FF", "Unknown"),
                arrayOf<Any>("4151FFFF", "Unknown"),
            )
    }

    @Test
    fun `test valid fuel type responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            FuelTypeCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals(expected, obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class GenericFuelLevelCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Float,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("412F10", 6.3f),
                arrayOf<Any>("412FC8", 78.4f),
                arrayOf<Any>("412F00", 0f),
                arrayOf<Any>("412FFF", 100f),
                arrayOf<Any>("412FFFFF", 100f),
            )
    }

    @Test
    fun `test valid fuel level responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            FuelLevelCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid ethanol level responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            EthanolLevelCommand().run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}

@RunWith(Parameterized::class)
class GenericFuelTrimCommandParameterizedTests(
    private val rawValue: String,
    private val expected: Float,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun values() =
            listOf(
                arrayOf<Any>("410610", -87.5f),
                arrayOf<Any>("410643", -47.7f),
                arrayOf<Any>("410680", 0f),
                arrayOf<Any>("4106C8", 56.25f),
                arrayOf<Any>("410600", -100f),
                arrayOf<Any>("4106FF", 99.2f),
                arrayOf<Any>("4106FFFF", 99.2f),
            )
    }

    @Test
    fun `test valid fuel trim short term bank 1 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            FuelTrimCommand(FuelTrimCommand.FuelTrimBank.SHORT_TERM_BANK_1).run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid fuel trim short term bank 2 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            FuelTrimCommand(FuelTrimCommand.FuelTrimBank.SHORT_TERM_BANK_2).run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid fuel trim long term bank 1 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            FuelTrimCommand(FuelTrimCommand.FuelTrimBank.LONG_TERM_BANK_1).run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }

    @Test
    fun `test valid fuel trim long term bank 2 responses handler`() {
        val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
        val obdResponse =
            FuelTrimCommand(FuelTrimCommand.FuelTrimBank.LONG_TERM_BANK_2).run {
                handleResponse(rawResponse)
            }
        assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
    }
}
