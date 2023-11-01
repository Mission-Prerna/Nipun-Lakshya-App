package com.samagra.ancillaryscreens.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import com.chatbot.notification.ChatbotNotificationHandler
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samagra.ancillaryscreens.R
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants.NL_NOTIFICATION_CHANNEL
import com.samagra.commons.constants.DeeplinkConstants
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.EID_IMPRESSION
import com.samagra.commons.posthog.EVENT_NOTIFICATION_RECEIVED
import com.samagra.commons.posthog.EVENT_TYPE_SYSTEM
import com.samagra.commons.posthog.NL_APP_CHATBOT
import com.samagra.commons.posthog.NL_SPLASH_SCREEN
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.SPLASH_SCREEN
import com.samagra.commons.posthog.TYPE_VIEW
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.utils.isChatBotEnabled
import timber.log.Timber

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val prefs = CommonsPrefsHelperImpl(this, "prefs")
        if (prefs.isLoggedIn) {
            NotificationRepository().postFCMToken(
                token = token,
                prefs = prefs,
                onSuccess = {}) { e: Exception? ->
                if (e != null) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logNotificationReceived()
        val prefs = CommonsPrefsHelperImpl(this, "prefs")
        Timber.d("onMessageReceived: %s", remoteMessage.data)
        val savedMentor = prefs.mentorDetailsData
        val isChatBotVisibilityEnabled = try {
            if (savedMentor != null) {
                isChatBotEnabled(savedMentor.actorId)
            } else {
                false
            }
        } catch (t: Throwable) {
            Timber.e(t, "onMessageReceived: ")
            false
        }

        if (isChatBotVisibilityEnabled &&
            ChatbotNotificationHandler.canHandleRemoteMessage(remoteMessage)
        ) {
            ChatbotNotificationHandler.handleRemoteMessage(this, remoteMessage)
        } else {
            remoteMessage.notification?.let {
                handleFCMNotification(it)
            }
        }

    }

    private fun logNotificationReceived() {
        //Also trigger to posthog
        val properties = PostHogManager.createProperties(
            page = SPLASH_SCREEN,
            eventType = EVENT_TYPE_SYSTEM,
            eid = EID_IMPRESSION,
            context = PostHogManager.createContext(
                id = APP_ID,
                pid = NL_APP_CHATBOT,
                dataList = arrayListOf()
            ),
            eData = Edata(NL_SPLASH_SCREEN, TYPE_VIEW),
            objectData = null,
            prefs = PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(
            context = this,
            eventName = EVENT_NOTIFICATION_RECEIVED,
            properties = properties
        )
    }

    private fun handleFCMNotification(notification: RemoteMessage.Notification) {
        val notificationChannel = NL_NOTIFICATION_CHANNEL
        val intent = Intent(Intent.ACTION_VIEW)
        if (notification.clickAction.isNullOrEmpty().not())
            intent.data = Uri.parse(notification.clickAction)
        else
            intent.data = Uri.parse(DeeplinkConstants.HOME)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                getString(R.string.nl_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannel)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(pendingIntent)

        // Show the notification
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.title.hashCode(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
