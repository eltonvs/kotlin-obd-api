package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.calculatePercentage

class CommandedEgrCommand : ObdCommand() {
  override val tag = "COMMANDED_EGR"
  override val name = "Commanded EGR"
  override val mode = "01"
  override val pid = "2C"

  override val defaultUnit = "%"
  override val handler = { it: ObdRawResponse -> "%.1f".format(calculatePercentage(it.bufferedValue)) }
}
