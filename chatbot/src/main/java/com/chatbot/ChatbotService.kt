package com.chatbot

import com.chatbot.model.MentorBotActionRequest
import okhttp3.ResponseBody
import retrofit2.http.*

interface ChatbotService {

    @GET("mentor/bot/telemetry")
    suspend fun getBotsWithAction(
        @Header("authorization") apiKey: String,
        @Query("action") action: Int
    ): List<String>?

    @POST("mentor/bot/telemetry")
    suspend fun setBotWithAction(
        @Header("authorization") apiKey: String,
        @Body mentorBotActionRequest: List<MentorBotActionRequest>
    ): Any

    @GET
    @Streaming
    suspend fun downloadAsset(@Url url: String): ResponseBody
}