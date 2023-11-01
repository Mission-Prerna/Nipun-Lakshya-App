package com.chatbot

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.chatbot.model.ChatbotTelemetryAction
import com.chatbot.model.ChatbotUrlResponseObject
import com.chatbot.model.MentorBotActionRequest
import com.google.gson.Gson
import com.hasura.model.FetchMentorBotsQuery
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.commons.AppPreferences
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.isChatBotEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun fetchMentorBots(
        phoneNo: String,
        success: (List<String>) -> Unit,
        failure: (String) -> Unit
    ) {
        withContext(Dispatchers.Default) {
            val query = FetchMentorBotsQuery.builder().phone_no(phoneNo).build()
            val client = Network.getClient(
                clientType = ClientType.GRAPHQL,
                clazz = ApolloClient::class.java,
                identity = CommonConstants.IDENTITY_HASURA
            )
            client?.query(query)
                ?.enqueue(object : ApolloCall.Callback<FetchMentorBotsQuery.Data>() {
                    override fun onResponse(response: Response<FetchMentorBotsQuery.Data>) {
                        if (response.errors.isNullOrEmpty()) {
                            if (response.data != null && response.data!!.mentor().isNotEmpty()) {
                                val bots: MutableList<String> = ArrayList()
                                response.data!!.mentor().forEach { mentorInfo ->
                                    mentorInfo.segmentations().forEach {
                                        it.segment().bots().forEach { bot ->
                                            bots.add(bot.bot_id().toString())
                                        }
                                    }
                                }
                                AppPreferences.chatBotList = gson.toJson(bots)
                                success(bots)
                            }
                        } else {
                            val builder = StringBuilder()
                            response.errors?.forEach {
                                builder.append(it.message)
                            }
                            failure(builder.toString())
                        }
                    }

                    override fun onFailure(e: ApolloException) {
                        failure(e.message ?: "Bot fetch Api failed")
                    }
                })
        }
    }

    fun getChatbotUrls(): Map<String, String> {

        val DEFAULT_CHATBOT_URLS = "{\"socketUrl\":\"ws://159.65.151.114:3005\",\"chatHistoryUrl\":\"http://159.65.151.114:9080/\",\"botDetailsUrl\":\"http://159.65.151.114:9999\",\"servicesUrl\":\"http://159.65.151.114:9080/\"}"

        val response = ChatbotUrlResponseObject.fromJson(
            json = if (BuildConfig.DEBUG) DEFAULT_CHATBOT_URLS else firebaseConfig.getString(RemoteConfigUtils.CHATBOT_URLS),
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
