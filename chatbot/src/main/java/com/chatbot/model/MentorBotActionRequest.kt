package com.chatbot.model

import com.google.gson.annotations.SerializedName

data class MentorBotActionRequest(
    @SerializedName("botId") val botId: String,
    @SerializedName("action") val action: Int
)
