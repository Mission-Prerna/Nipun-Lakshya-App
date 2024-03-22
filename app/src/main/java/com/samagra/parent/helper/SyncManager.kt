package com.samagra.parent.helper

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.SYNC_INTERVAL_MINUTES_PER_ACTOR
import com.samagra.parent.BuildConfig
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

object SyncManager {

    private const val FINAL_SYNC_REQUEST = "final_sync_request"

    fun init(ctx: Context) {
        checkAndScheduleWorker(ctx)
    }

    private fun createPeriodicWorkRequest(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val prefs = CommonsPrefsHelperImpl(context, "prefs")
        val actorId = prefs.mentorDetailsData.actorId

        val repeatInterval: Long

        if (BuildConfig.DEBUG){
            repeatInterval = 15
        } else {
            val syncIntervalsListJson = RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getString(SYNC_INTERVAL_MINUTES_PER_ACTOR).trimIndent()
            val syncIntervalsList = JSONObject(syncIntervalsListJson)
            repeatInterval = if (syncIntervalsList.has(actorId.toString())) {
                syncIntervalsList.getLong(actorId.toString())
            } else {
                15
            }
        }

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            repeatInterval = repeatInterval,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 30,
            flexTimeIntervalUnit = TimeUnit.SECONDS,
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FINAL_SYNC_REQUEST,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    private fun checkAndScheduleWorker(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val future = workManager.getWorkInfosForUniqueWork(FINAL_SYNC_REQUEST)
        future.addListener({
            try {
                val workers = future.get()
                if (workers.size == 0) {
                    createPeriodicWorkRequest(context)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }, { command -> command.run() })
    }
}