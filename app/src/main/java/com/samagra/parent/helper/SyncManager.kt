package com.samagra.parent.helper

import android.content.Context
import androidx.work.*
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.SYNC_WORKER_INTERVAL_IN_MINUTES
import com.samagra.parent.BuildConfig
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

        val repeatInterval = if (BuildConfig.DEBUG) {
            15
        } else {
            RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getLong(SYNC_WORKER_INTERVAL_IN_MINUTES)
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