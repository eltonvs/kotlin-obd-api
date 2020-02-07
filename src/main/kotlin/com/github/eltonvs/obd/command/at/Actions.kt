package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.ATCommand


class ResetAdapterCommand : ATCommand() {
    override val tag = "RESET_ADAPTER"
    override val name = "Reset OBD Adapter"
    override val pid = "Z"
}

class WarmStartCommand : ATCommand() {
    override val tag = "WARM_START"
    override val name = "OBD Warm Start"
    override val pid = "WS"
}

class SlowInitiationCommand : ATCommand() {
    override val tag = "SLOW_INITIATION"
    override val name = "OBD Slow Initiation"
    override val pid = "SI"
}

class LowPowerModeCommand : ATCommand() {
    override val tag = "LOW_POWER_MODE"
    override val name = "OBD Low Power Mode"
    override val pid = "LP"
}

class BufferDumpCommand : ATCommand() {
    override val tag = "BUFFER_DUMP"
    override val name = "OBD Buffer Dump"
    override val pid = "BD"
}

class BypassInitializationCommand : ATCommand() {
    override val tag = "BYPASS_INITIALIZATION"
    override val name = "OBD Bypass Initialization Sequence"
    override val pid = "BI"
}

class ProtocolCloseCommand : ATCommand() {
    override val tag = "PROTOCOL_CLOSE"
    override val name = "OBD Protocol Close"
    override val pid = "PC"
}