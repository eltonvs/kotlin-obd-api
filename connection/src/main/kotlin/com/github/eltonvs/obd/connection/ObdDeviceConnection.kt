package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.domain.ObdResponse

abstract class ObdDeviceConnection {
    abstract val command: ObdCommand
    abstract val response: ObdResponse
}