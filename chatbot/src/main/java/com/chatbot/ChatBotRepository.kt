package com.chatbot

import com.chatbot.model.ChatbotTelemetryAction
import com.chatbot.model.ChatbotUrlResponseObject
import com.chatbot.model.MentorBotActionRequest
import com.google.gson.Gson
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.commons.AppPreferences
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.isChatBotEnabled
import okhttp3.ResponseBody
import timber.log.Timber

object ChatBotRepository {

    private val userConfiguredBots = mutableListOf<String>()
    private val userStartedBots = mutableListOf<String>()

    private val firebaseConfig by lazy {
        RemoteConfigUtils.getFirebaseRemoteConfigInstance()
    }

    private val gson by lazy { Gson() }

    private val apiClient by lazy {
        Network.getClient(
            ClientType.RETROFIT,
            ChatbotService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )
    }

    suspend fun fetchMentorBots() =
        apiClient?.getBots(apiKey = AppPreferences.getUserBearer())

    fun getChatbotUrls(): Map<String, String> {
        val response = ChatbotUrlResponseObject.fromJson(
            json = if (BuildConfig.DEBUG) {
                com.samagra.commons.BuildConfig.DEFAULT_CHATBOT_URLS
            } else {
                firebaseConfig.getString(RemoteConfigUtils.CHATBOT_URLS)
            },
            gson = gson
        )
        return mapOf(
            "socketUrl" to response.socketUrl,
            "chatHistoryUrl" to response.chatHistoryUrl,
            "botDetailsUrl" to response.botDetailsUrl,
        )
    }

    @Throws(Throwable::class)
    suspend fun getChatbotsWithAction(
        action: ChatbotTelemetryAction
    ): List<String>? {
        return apiClient?.getBotsWithAction(
            AppPreferences.getUserBearer(),
            action.identifier
        )
    }

    fun isChatbotEnabledForActor(): Boolean {
        val mentor = AppPreferences.getUser() ?: return true
        Timber.d("isChatbotEnabledForActor: ${mentor.actorId}")
        return isChatBotEnabled(mentor.actorId)
    }

    suspend fun setBotWithAction(botId: String, action: ChatbotTelemetryAction) {
        return setBotsWithAction(listOf(botId), action)
    }

    fun setUserConfiguredBots(userBots: List<String>) {
        userConfiguredBots.apply {
            clear()
            addAll(userBots)
        }
    }


    fun setUserStartedBots(userBots: List<String>) {
        userStartedBots.apply {
            clear()
            addAll(userBots)
        }
    }

    fun getUserConfiguredBots() = userConfiguredBots
    fun getUserStartedBots() = userStartedBots
    suspend fun setBotsWithAction(botIds: List<String>, action: ChatbotTelemetryAction) {
        try {
            val response = apiClient?.setBotWithAction(
                apiKey = AppPreferences.getUserBearer(),
                mentorBotActionRequest = botIds.map {
                    MentorBotActionRequest(
                        botId = it,
                        action = action.identifier
                    )
                }
            )
            if (action == ChatbotTelemetryAction.STARTED) {
                userStartedBots.addAll(botIds)
            }
            Timber.d("setChatbotTelemetry: $response")
        } catch (t: Throwable) {
            Timber.e(t, "setChatbotTelemetry: ${t.message}")
        }
    }

    suspend fun downloadAsset(url: String): ResponseBody? {
        return try {
            apiClient?.downloadAsset(url)
        } catch (t: Throwable) {
            Timber.e(t, "downloadAsset: ")
            null
        }
    }
}
