package com.chatbot.notification

import com.chatbot.notification.model.NotificationTelemetryRequestModel
import retrofit2.http.Body

import retrofit2.http.POST


interface ChatbotNotificationTelemetryService {
    @POST("firebase/web")
    suspend fun postTelemetry(
        @Body notificationTelemetryRequestModel: NotificationTelemetryRequestModel
    )
}
