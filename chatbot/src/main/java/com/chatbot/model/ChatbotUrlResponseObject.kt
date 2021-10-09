package com.chatbot.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ChatbotUrlResponseObject(
    @SerializedName("socketUrl") val socketUrl: String,
    @SerializedName("chatHistoryUrl") val chatHistoryUrl: String,
    @SerializedName("botDetailsUrl") val botDetailsUrl: String,
    @SerializedName("servicesUrl") val servicesUrl: String,
) {
    companion object {
        @JvmStatic
        fun fromJson(json: String, gson: Gson = Gson()): ChatbotUrlResponseObject = gson.fromJson(json, ChatbotUrlResponseObject::class.java)
    }
}