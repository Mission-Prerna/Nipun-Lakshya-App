package com.samagra.commons

import timber.log.Timber

fun getPercentage(dividend: Int, divisor: Int): Int {
    var percentage = 0
    try {
        percentage = (dividend * 100) / divisor
    } catch (e: Exception) {
        e.printStackTrace()
        //todo add telemetry event
    }
    return percentage
}

fun getLastCharAsInt(text: String): Int {
    return try {
        text.substring(text.length - 1).toInt()
    } catch (e: Exception) {
        Timber.e("" + e)
        0
    }
}