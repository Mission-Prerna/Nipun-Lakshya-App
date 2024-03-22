package com.samagra.ancillaryscreens.fcm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class UpdateTokenWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("doWork: ")
        val token = FirebaseMessaging.getInstance().token.await()
        Timber.d("doWork: FCMToken: $token")
        val prefs = CommonsPrefsHelperImpl(appContext, "prefs")
        if (prefs.isLoggedIn) {
            NotificationRepository().postFCMToken(
                token = token,
                prefs = prefs,
                onSuccess = {}
            ) { e: Exception? ->
                if (e != null) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Timber.d("doWork: FCMToken upload failure")
                }
            }
        }
        Timber.d("doWork: Completed")
        return Result.success()
    }
}