package com.chatbot

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chatbot.notification.ChatbotNotificationHandler
import com.chatbot.notification.NotificationTelemetryType
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.morziz.network.utils.NetworkUtils
import com.samagra.commons.AppPreferences
import com.samagra.commons.constants.Constants
import timber.log.Timber

class ChatBotVM : ViewModel() {

    private val chatBotRepository = ChatBotRepository()
    val botsLiveData = MutableLiveData<String?>()

    val chatbotBaseUrl = "file:///android_asset/chatbot/index.html"

    val gson by lazy { Gson() }

    fun submitChat(chatId: String, chat: String) = AppPreferences.submitChat(chatId, chat)

    fun saveStarredMessages(savedMsg: String) = AppPreferences.saveStarredMessages(savedMsg)

    fun getInjection(botToFocus: String, botIds: String): String {
        val injectionBuilder = StringBuilder()
        injectionBuilder.append(
            "<script type='text/javascript'>" +
                    "localStorage.setItem('auth', '${AppPreferences.getUserAuth()}' );" +
                    "localStorage.setItem('mobile', '${AppPreferences.getUserMobile()}');" +
                    "localStorage.setItem('botList','${botIds}' );" +
                    "localStorage.setItem('botDetails', '${AppPreferences.getChatBotDetails()}');" +
                    "localStorage.setItem('starredChats', '${AppPreferences.getStarredMsgs()}');" +
                    "localStorage.setItem('chatHistory', '${AppPreferences.getChatHistory()}' );"
        )

        if (botToFocus.isNotEmpty())
            injectionBuilder.append("localStorage.setItem('botToFocus', '$botToFocus' );")

        chatBotRepository.getChatbotUrls().forEach {
            injectionBuilder.append("localStorage.setItem('${it.key}', '${it.value}' );")
        }

        injectionBuilder.append(
            "window.location.replace('${chatbotBaseUrl}');" +
                    "</script>"
        )
        return injectionBuilder.toString()
    }

    fun saveBotDetails(botDetailsJson: String) =
        AppPreferences.saveChatBotDetails(botDetailsJson)

    fun clearLocalStorage() = AppPreferences.clearLocal()

    fun fetchBots(ctx: Context) {
        try {
            val userPhone = AppPreferences.getUserMobile()
            Timber.d("fetchBots: $userPhone")
            if (NetworkUtils.isNetworkAvailable(ctx)) {
                chatBotRepository.fetchMentorBots(userPhone, {
                    botsLiveData.postValue(it)
                }, {
                    botsLiveData.postValue(AppPreferences.chatBotList)
                })
            } else {
                botsLiveData.postValue(AppPreferences.chatBotList)
            }
        } catch (e: Exception) {
            Timber.e(e)
            botsLiveData.postValue(null)
        }
    }

    fun logNotificationReadTelemetry(context: Context, data: Map<String, String>) {
        ChatbotNotificationHandler.triggerNotificationTelemetry(
            context = context,
            type = NotificationTelemetryType.READ,
            messageData = data
        )
    }

    fun isAccessAllowedToUser() = AppPreferences.getSelectedUserType() == Constants.USER_TEACHER
    fun getPropertiesMapFromJson(eventProperties: String?): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        if (eventProperties.isNullOrEmpty().not()) {
            val eventPropertiesObject = JsonParser.parseString(eventProperties).asJsonObject
            eventPropertiesObject.keySet().forEach {
                result[it] = eventPropertiesObject[it]
            }
        }
        result["userId"] = AppPreferences.getUserId()
        Timber.d("getPropertiesMapFromJson: map: $result")
        return result
    }

    fun getUserIdMap(): String = gson.toJson(mapOf(getUserIdPair()))
    fun getUserIdPair() = "userId" to AppPreferences.getUserId()
}
