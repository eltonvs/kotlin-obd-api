package com.github.eltonvs.obd.command

import kotlin.math.pow

private const val BYTE_BITS = 8
private const val PERCENT_SCALE = 100f
private const val MAX_PERCENTAGE_VALUE = 255f

fun bytesToInt(
    bufferedValue: IntArray,
    start: Int = 2,
    bytesToProcess: Int = -1,
): Long {
    var bufferToProcess = bufferedValue.drop(start)
    if (bytesToProcess != -1) {
        bufferToProcess = bufferToProcess.take(bytesToProcess)
    }
    return bufferToProcess.foldIndexed(0L) { index, total, current ->
        total + current * 2f.pow((bufferToProcess.size - index - 1) * BYTE_BITS).toLong()
    }
}

fun calculatePercentage(
    bufferedValue: IntArray,
    bytesToProcess: Int = -1,
): Float = (bytesToInt(bufferedValue, bytesToProcess = bytesToProcess) * PERCENT_SCALE) / MAX_PERCENTAGE_VALUE

fun Int.getBitAt(
    position: Int,
    last: Int = 32,
) = this shr (last - position) and 1

fun Long.getBitAt(
    position: Int,
    last: Int = 32,
) = (this shr (last - position) and 1).toInt()
