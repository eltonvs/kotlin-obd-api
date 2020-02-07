package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.ATCommand
import com.github.eltonvs.obd.command.AdaptiveTimingMode
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.Switcher


class SelectProtocolCommand(protocol: ObdProtocols) : ATCommand() {
    private val _protocol = if (protocol == ObdProtocols.UNKNOWN) ObdProtocols.AUTO else protocol
    override val tag = "SELECT_PROTOCOL_${_protocol.name}"
    override val name = "Select Protocol - ${_protocol.displayName}"
    override val pid = "SP ${_protocol.command}"
}

class SetAdaptiveTimingCommand(value: AdaptiveTimingMode) : ATCommand() {
    override val tag = "SET_ADAPTIVE_TIMING_${value.name}"
    override val name = "Set Adaptive Timing Control ${value.displayName}"
    override val pid = "AT ${value.command}"
}

class SetEchoCommand(value: Switcher) : ATCommand() {
    override val tag = "SET_ECHO_${value.name}"
    override val name = "Set Echo ${value.name}"
    override val pid = "E${value.command}"
}

class SetHeadersCommand(value: Switcher) : ATCommand() {
    override val tag = "SET_HEADERS_${value.name}"
    override val name = "Set Headers ${value.name}"
    override val pid = "H${value.command}"
}

class SetLineFeedCommand(value: Switcher) : ATCommand() {
    override val tag = "SET_LINE_FEED_${value.name}"
    override val name = "Set Line Feed ${value.name}"
    override val pid = "L${value.command}"
}

class SetSpacesCommand(value: Switcher) : ATCommand() {
    override val tag = "SET_SPACES_${value.name}"
    override val name = "Set Spaces ${value.name}"
    override val pid = "S${value.command}"
}

class SetTimeoutCommand(timeout: Int) : ATCommand() {
    override val tag = "SET_TIMEOUT"
    override val name = "Set Timeout - $timeout"
    override val pid = "ST ${Integer.toHexString(0xFF and timeout)}"
}