package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.ObdRawResponse
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class CommandedEgrCommandParameterizedTests(private val rawValue: String, private val expected: Float) {
  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun values() = listOf(
            arrayOf("414545", 27.1f),
            arrayOf("414500", 0f),
            arrayOf("4145FF", 100f)
    )
  }

  @Test
  fun `test valid commanded egr responses handler`() {
    val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
    val obdResponse = CommandedEgrCommand().run {
      handleResponse(rawResponse)
    }
    assertEquals("%.1f".format(expected) + '%', obdResponse.formattedValue)
  }
}
