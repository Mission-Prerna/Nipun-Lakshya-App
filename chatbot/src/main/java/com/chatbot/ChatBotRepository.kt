package com.chatbot

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.chatbot.model.ChatbotUrlResponseObject
import com.google.gson.Gson
import com.hasura.model.FetchMentorBotsQuery
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.commons.AppPreferences
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatBotRepository {

    private val firebaseConfig by lazy {
        RemoteConfigUtils.getFirebaseRemoteConfigInstance()
    }

    private val gson by lazy { Gson() }

    fun fetchMentorBots(
        phoneNo: String,
        success: (String) -> Unit,
        failure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
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
                                val botStr = gson.toJson(bots)
                                AppPreferences.chatBotList = botStr
                                success(botStr)
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
        val response = ChatbotUrlResponseObject.fromJson(
            json = firebaseConfig.getString(RemoteConfigUtils.CHATBOT_URLS),
            gson = gson
        )
        return mapOf(
            "socketUrl" to response.socketUrl,
            "chatHistoryUrl" to response.chatHistoryUrl,
            "botDetailsUrl" to response.botDetailsUrl,
        )
    }
}