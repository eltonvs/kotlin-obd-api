package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.ATCommand
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.ObdRawResponse


class DescribeProtocolCommand : ATCommand() {
    override val tag = "DESCRIBE_PROTOCOL"
    override val name = "Describe Protocol"
    override val pid = "DP"
}

class DescribeProtocolNumberCommand : ATCommand() {
    override val tag = "DESCRIBE_PROTOCOL_NUMBER"
    override val name = "Describe Protocol Number"
    override val pid = "DPN"

    override val handler = { it: ObdRawResponse -> parseProtocolNumber(it).displayName }

    private fun parseProtocolNumber(rawResponse: ObdRawResponse): ObdProtocols {
        val result = rawResponse.value
        val protocolNumber = result[if (result.length == 2) 1 else 0].toString()

        return ObdProtocols.values().find { it.command == protocolNumber } ?: ObdProtocols.UNKNOWN
    }
}

class IgnitionMonitorCommand : ATCommand() {
    override val tag = "IGNITION_MONITOR"
    override val name = "Ignition Monitor"
    override val pid = "IGN"

    override val handler = { it: ObdRawResponse -> it.value.trim().uppercase() }
}

class AdapterVoltageCommand : ATCommand() {
    override val tag = "ADAPTER_VOLTAGE"
    override val name = "OBD Adapter Voltage"
    override val pid = "RV"
}