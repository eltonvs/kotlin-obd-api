package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.domain.ObdResponse

class ObdDeviceConnection {
    val command = ObdCommand()
    val response = ObdResponse()
}