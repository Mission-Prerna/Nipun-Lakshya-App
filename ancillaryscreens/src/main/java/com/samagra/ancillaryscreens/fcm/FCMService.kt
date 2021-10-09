package com.samagra.ancillaryscreens.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chatbot.notification.ChatbotNotificationHandler
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.samagra.ancillaryscreens.R
import com.samagra.commons.models.Result
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants.*
import com.samagra.commons.constants.DeeplinkConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.isChatBotEnabled
import timber.log.Timber

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val prefs = CommonsPrefsHelperImpl(this, "prefs")
        if (prefs.isLoggedIn) {
            val mentorDetails =
                Gson().fromJson(prefs.mentorDetails, Result::class.java)
            NotificationRepository().postFCMToken(token, mentorDetails.id, {}) { e: Exception? ->
                if (e != null) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val prefs = CommonsPrefsHelperImpl(this, "prefs")
        Timber.d("onMessageReceived: %s", remoteMessage.data)
        val isChatBotVisibilityEnabled =
            isChatBotEnabled(prefs.mentorDetailsData.actorId)

        if (isChatBotVisibilityEnabled != null) {
            if (isChatBotVisibilityEnabled && ChatbotNotificationHandler.canHandleRemoteMessage(
                    remoteMessage
                )
            ) {
                ChatbotNotificationHandler.handleRemoteMessage(this, remoteMessage)
            } else {
                remoteMessage.notification?.let {
                    handleFCMNotification(it)
                }
            }
        }

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
