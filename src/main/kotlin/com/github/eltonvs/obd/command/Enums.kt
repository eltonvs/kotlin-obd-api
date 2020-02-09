package com.github.eltonvs.obd.command


enum class ObdProtocols(val displayName: String, internal val command: String) {
    // Unknown protocol
    UNKNOWN("Unknown Protocol", ""),
    // Auto select protocol and save.
    AUTO("Auto", "0"),
    // 41.6 kbaud
    SAE_J1850_PWM("SAE J1850 PWM", "1"),
    // 10.4 kbaud
    SAE_J1850_VPW("SAE J1850 VPW", "2"),
    // 5 baud init
    ISO_9141_2("ISO 9141-2", "3"),
    // 5 baud init
    ISO_14230_4_KWP("ISO 14230-4 (KWP 5BAUD)", "4"),
    // Fast init
    ISO_14230_4_KWP_FAST("ISO 14230-4 (KWP FAST)", "5"),
    // 11 bit ID, 500 kbaud
    ISO_15765_4_CAN("ISO 15765-4 (CAN 11/500)", "6"),
    // 29 bit ID, 500 kbaud
    ISO_15765_4_CAN_B("ISO 15765-4 (CAN 29/500)", "7"),
    // 11 bit ID, 250 kbaud
    ISO_15765_4_CAN_C("ISO 15765-4 (CAN 11/250)", "8"),
    // 29 bit ID, 250 kbaud
    ISO_15765_4_CAN_D("ISO 15765-4 (CAN 29/250)", "9"),
    // 29 bit ID, 250 kbaud (user adjustable)
    SAE_J1939_CAN("SAE J1939 (CAN 29/250)", "A"),
}

enum class AdaptiveTimingMode(val displayName: String, internal val command: String) {
    OFF("Off", "0"),
    AUTO_1("Auto 1", "1"),
    AUTO_2("Auto 2", "2"),
}

enum class Switcher(internal val command: String) {
    ON("1"),
    OFF("0"),
}

enum class Monitors(
    internal val displayName: String,
    internal val isSparkIgnition: Boolean? = null,
    internal val bitPos: Int
) {
    // Common
    MISFIRE("Misfire", bitPos = 0),
    FUEL_SYSTEM("Fuel System", bitPos = 1),
    COMPREHENSIVE_COMPONENT("Comprehensive Component", bitPos = 2),
    // Spark Ignition Monitors
    CATALYST("Catalyst (CAT)", true, 0),
    HEATED_CATALYST("Heated Catalyst", true, 1),
    EVAPORATIVE_SYSTEM("Evaporative (EVAP) System", true, 2),
    SECONDARY_AIR_SYSTEM("Secondary Air System", true, 3),
    AC_REFRIGERANT("A/C Refrigerant", true, 4),
    OXYGEN_SENSOR("Oxygen (O2) Sensor", true, 5),
    OXYGEN_SENSOR_HEATER("Oxygen Sennsor Heater", true, 6),
    EGR_SYSTEM("EGR (Exhaust Gas Recirculation) and/or VVT System", true, 7),
    // Compression Ignition Monitors
    NMHC_CATALYST("NMHC Catalyst", false, 0),
    NOX_SCR_MONITOR("NOx/SCR Aftertreatment", false, 1),
    BOOST_PRESSURE("Boost Pressure", false, 3),
    EXHAUST_GAS_SENSOR("Exhaust Gas Sensor", false, 5),
    PM_FILTER("PM Filter", false, 6),
    EGR_VVT_SYSTEM("EGR (Exhaust Gas Recirculation) and/or VVT System", false, 7),
}
