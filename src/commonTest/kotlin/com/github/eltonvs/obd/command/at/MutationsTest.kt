package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.AdaptiveTimingMode
import kotlin.test.Test
import kotlin.test.assertEquals

class MutationsTest {
    @Test
    fun `adaptive timing command uses the expected AT syntax`() {
        assertEquals("AT AT1", SetAdaptiveTimingCommand(AdaptiveTimingMode.AUTO_1).rawCommand)
    }

    @Test
    fun `timeout command uses uppercase zero padded hex`() {
        assertEquals("AT ST 0A", SetTimeoutCommand(10).rawCommand)
    }

    @Test
    fun `timeout command masks values to one byte`() {
        assertEquals("AT ST 23", SetTimeoutCommand(0x123).rawCommand)
    }
}
