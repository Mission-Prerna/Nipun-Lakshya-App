package com.chatbot.notification.repo

import android.content.Context
import android.preference.PreferenceManager
import com.chatbot.notification.ChatbotNotificationTelemetryService
import com.chatbot.notification.NotificationTelemetryType
import com.chatbot.notification.model.NotificationTelemetryRequestModel
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.DeeplinkConstants.Queries.BOT_ID
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

interface ChatbotNotificationRepo {

    fun postNotificationTelemetry(
        context: Context,
        type: NotificationTelemetryType,
        externalId: String,
        destAdd: String,
        fcmDestAdd: String,
        botId: String,
    )
}

class ChatbotNotificationRepoImpl : ChatbotNotificationRepo {

    private val client by lazy {
        Network.getClient(
            clientType = ClientType.RETROFIT,
            clazz = ChatbotNotificationTelemetryService::class.java,
            identity = Constants.CHATBOT_SERVICES_BASE_URL
        )
    }

    override fun postNotificationTelemetry(
        context: Context,
        type: NotificationTelemetryType,
        externalId: String,
        destAdd: String,
        fcmDestAdd: String,
        botId: String,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = NotificationTelemetryRequestModel(
                    text = "",
                    from = "",
                    messageId = "",
                    eventType = type.name,
                    notificationTelemetryReport = NotificationTelemetryRequestModel.NotificationTelemetryReport(
                        externalId = externalId,
                        destAdd = destAdd,
                        fcmDestAdd = fcmDestAdd
                    )
                )
                val response = client?.postTelemetry(body)
                Timber.d("postNotificationTelemetry: $response")
            } catch (e: Exception) {
                Timber.e(e, "postNotificationTelemetry: Failed")
            }

            //Also trigger to posthog
            val eventName = if (type == NotificationTelemetryType.DELIVERED)
                EVENT_CHATBOT_NOTIFICATION_RECEIVED
            else
                EVENT_CHATBOT_NOTIFICATION_OPENED
            val properties = PostHogManager.createProperties(
                page = CHATBOT_SCREEN,
                eventType = EVENT_TYPE_SYSTEM,
                eid = EID_IMPRESSION,
                context = PostHogManager.createContext(
                    id = APP_ID,
                    pid = NL_APP_CHATBOT,
                    dataList = arrayListOf(Cdata(type = BOT_ID, id = botId))
                ),
                eData = Edata(NL_CHATBOT, TYPE_VIEW),
                objectData = null,
                PreferenceManager.getDefaultSharedPreferences(context)
            )
            PostHogManager.capture(
                context = context,
                eventName = eventName,
                properties = properties
            )
        }
    }

}
