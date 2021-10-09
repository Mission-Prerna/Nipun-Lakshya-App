package com.samagra.parent.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessagingService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.PostHogManager.capture
import com.samagra.commons.posthog.PostHogManager.createContext
import com.samagra.commons.posthog.PostHogManager.createProperties
import com.samagra.commons.posthog.data.Cdata
import com.samagra.parent.BuildConfig
import com.samagra.parent.R
import com.samagra.parent.ui.splash.SplashActivity
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class DataSyncWorker(
    private val context: Context, params: WorkerParameters
) : Worker(context, params) {

    @SuppressLint("BinaryOperationInTimber")
    override fun doWork(): Result {
        Timber.d("doWork: ")
        val prefs = CommonsPrefsHelperImpl(context, "prefs")
        val surveys = RealmStoreHelper.getSurveys()
        val helper = SyncingHelper()
        val assessments = helper.getAssessmentSubmissions(prefs)
        Timber.d("doWork: pending assessment: $assessments")
        showTestNotification(
            isCompleted = false,
            message = "Syncing started - Assessments : ${assessments.size}, Surveys : ${surveys.size}"
        )
        showStatusNotification(
            isCompleted = false,
            message = "Assessments Submission Syncing",
            isSuccess = false
        )
        val isSuccessAssessments = helper.syncAssessments(prefs, assessments)
        this.showTestNotification(
            isCompleted = false,
            message = "Syncing in Progress - Assessments : ${assessments.size} status - $isSuccessAssessments"
        )
        Timber.d("doWork: pending surveys: $surveys")
        val isSurveySuccess = helper.syncSurveys(prefs, surveys)
        this.showTestNotification(
            true,
            "Syncing stopped - Surveys : ${surveys.size} status - $isSurveySuccess"
        )
        sendTelemetry(
            assessmentsCount = assessments.size,
            surveysCount = surveys.size,
            isAssessmentSyncSuccess = isSuccessAssessments,
            isSurveySyncSuccess = isSurveySuccess,
            prefs = prefs
        )
        val isSuccess = isSurveySuccess && isSuccessAssessments
        showStatusNotification(
            isCompleted = true,
            message = if(isSuccess) "Assessments synced successfully" else "Failed to sync the assessments. ${context.getString(R.string.app_name)} app will retry syncing after sometime",
            isSuccess = isSuccess
        )
        Timber.d("doWork: Assessment Success: $isSuccessAssessments & Survey success: $isSurveySuccess")
        prefs.markDataSynced()
        return if (isSuccess) Result.success() else Result.retry()
    }

    private fun showStatusNotification(isCompleted: Boolean, message: String?, isSuccess: Boolean) {
        if (BuildConfig.DEBUG.not()) {
            return
        }
        val notificationChannel = Constants.NL_SYNCING_NOTIFICATION_CHANNEL
        val title = context.getString(R.string.app_name) + " Submissions Syncing"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                title,
                if (isCompleted) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "YOUR_NOTIFICATION_CHANNEL_DESCRIPTION"
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext, notificationChannel
        )
            .setSmallIcon(R.mipmap.ic_launcher_mpp_round)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setOngoing(!isCompleted)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(title) // title for notification
            .setContentText(message) // message for notification;
        if (!isCompleted || !isSuccess) {
            mBuilder.setNotificationSilent()
        }
        mBuilder.setProgress(0, 0, !isCompleted)
        val intent = Intent(
            applicationContext,
            SplashActivity::class.java
        )
        val pi = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        mBuilder.setContentIntent(pi)

        val notificationManager =
            context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            title.hashCode(),
            mBuilder.build()
        )
    }

    private fun showTestNotification(isCompleted: Boolean, message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        val notificationChannel = Constants.NL_SYNCING_NOTIFICATION_CHANNEL
        val sdf = SimpleDateFormat("dd-MM-yyyy - HH:mm:ss. SSS", Locale.getDefault())
        val title = "Syncing Worker " + sdf.format(Date())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                title,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Syncing Service"
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext, notificationChannel
        )
            .setSmallIcon(R.mipmap.ic_launcher_mpp_round)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(title) // title for notification
            .setContentText(message) // message for notification;
        val intent = Intent(
            applicationContext,
            SplashActivity::class.java
        )
        val pi = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        mBuilder.setContentIntent(pi)

        val notificationManager =
            context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            title.hashCode(),
            mBuilder.build()
        )
    }

    private fun sendTelemetry(
        assessmentsCount: Int,
        surveysCount: Int,
        isAssessmentSyncSuccess: Boolean,
        isSurveySyncSuccess: Boolean,
        prefs: CommonsPrefsHelperImpl
    ) {
        val list = ArrayList<Cdata>()
        list.add(Cdata("assessmentsCount", "" + assessmentsCount))
        list.add(Cdata("surveysCount", "" + surveysCount))
        list.add(Cdata("isAssessmentSyncSuccess", "" + isAssessmentSyncSuccess))
        list.add(Cdata("isSurveySyncSuccess", "" + isSurveySyncSuccess))
        val mentorDetailsFromPrefs = prefs.mentorDetailsData
        mentorDetailsFromPrefs?.let {
            list.add(Cdata("userId", "" + it.id))
        }
        val properties = createProperties(
            SYNC_WORKER,
            EVENT_TYPE_SYSTEM,
            EID_IMPRESSION,
            createContext(APP_ID, NL_APP_SYNC_WORKER, list),
            null,
            null,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        capture(context, EVENT_WORKER_PROCESSING, properties)
    }


}