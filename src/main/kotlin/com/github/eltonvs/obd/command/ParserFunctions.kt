package com.github.eltonvs.obd.command

import kotlin.math.pow


fun bytesToInt(bufferedValue: IntArray, start: Int = 2): Long =
    bufferedValue.drop(start).foldIndexed(0L) { index, total, current ->
        total + current * 2f.pow((bufferedValue.size - start - index - 1) * 8).toLong()
    }

fun calculatePercentage(bufferedValue: IntArray): Float = (bytesToInt(bufferedValue) * 100f) / 255f

fun Long.getBitAt(position: Int, last: Int = 32) = (this shr (last - position) and 1).toInt()
