package com.samagra.parent.helper

import android.util.Log
import com.samagra.grove.logging.Grove

class AssessmentDataManager {

     fun getTotalSeconds(totalTimeTaken: String): Int {
        return try {
            val split = totalTimeTaken.split(":")
            val hSec = split[0].toInt() * 60 * 60
            val mSec = split[1].toInt() * 60
            val sec = split[2].toInt()

            (hSec + mSec + sec)
        } catch (e: Exception) {
            Grove.e("exception on calculating average time taken on home screen")
            0
        }
    }

    fun getTimeInMinutes(totalSec: Long): Long {
        return if (totalSec > 0) {
//            Log.e("-->>", "total min ${totalSec / 60} remaining sec ${totalSec % 60}")
            totalSec / 60
        } else {
            0
        }
    }

    fun getAverageMinutes(totalSec: Long, totalStudentsAssessed: Int): Long {
        return if (totalSec > 0 && totalStudentsAssessed > 0) {
//            Log.e("-->>", "total min ${totalSec / 60} remaining sec ${totalSec % 60}")
            (totalSec / 60) / totalStudentsAssessed
        } else {
            0
        }
    }

}