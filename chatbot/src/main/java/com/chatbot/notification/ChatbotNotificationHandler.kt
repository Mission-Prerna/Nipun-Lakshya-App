package com.chatbot.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.chatbot.ChatBotRepository
import com.chatbot.R
import com.chatbot.notification.repo.ChatbotNotificationRepo
import com.chatbot.notification.repo.ChatbotNotificationRepoImpl
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.DeeplinkConstants
import timber.log.Timber

object ChatbotNotificationHandler {

    private val repo: ChatbotNotificationRepo by lazy { ChatbotNotificationRepoImpl() }

    private const val BOT_ID = "botId"
    private const val CLICK_ACTION = "click_action"
    private const val TITLE = "title"
    private const val BODY = "body"
    private const val EXTERNAL_ID = "externalId"
    private const val DEST_ADD = "destAdd"
    private const val FCM_DEST_ADD = "fcmDestAdd"

    fun  canHandleRemoteMessage(remoteMessage: RemoteMessage) =
        remoteMessage.data.containsKey(BOT_ID)

    fun handleRemoteMessage(
        context: Context,
        remoteMessage: RemoteMessage
    ) {
        Timber.d("handleRemoteMessage data: ${remoteMessage.data}")
        showChatbotNotification(context, remoteMessage.data)
        triggerNotificationTelemetry(
            context = context,
            type = NotificationTelemetryType.DELIVERED,
            messageData = remoteMessage.data
        )
    }

    private fun showChatbotNotification(context: Context, messageData: Map<String, String>) {
        val clickAction = messageData[CLICK_ACTION]
        val title = messageData[TITLE] ?: return
        val body = messageData[BODY] ?: return

        val notificationChannel = Constants.NL_CHATBOT_NOTIFICATION_CHANNEL
        val intent = Intent(Intent.ACTION_VIEW)
        if (clickAction.isNullOrEmpty().not())
            intent.data = Uri.parse(clickAction)
        else
            intent.data = Uri.parse(DeeplinkConstants.HOME)
        intent.putExtras(bundleOf(*messageData.toList().toTypedArray()))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                1221,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                context.getString(R.string.chatbot_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(context, notificationChannel)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.outline_smart_toy_24)
            .setContentIntent(pendingIntent)

        // Show the notification
        val notificationManager =
            context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            title.hashCode(),
            notificationBuilder.build()
        )
    }

    fun triggerNotificationTelemetry(
        context: Context,
        type: NotificationTelemetryType,
        messageData: Map<String, String>
    ) {
        repo.postNotificationTelemetry(
            context = context,
            type = type,
            externalId = messageData[EXTERNAL_ID] ?: "",
            destAdd = messageData[DEST_ADD] ?: "",
            fcmDestAdd = messageData[FCM_DEST_ADD] ?: "",
            botId = messageData[BOT_ID] ?: "",
        )
    }
}