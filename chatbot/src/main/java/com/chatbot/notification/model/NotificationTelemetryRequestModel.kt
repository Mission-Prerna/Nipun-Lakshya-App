package com.chatbot.notification.model

import com.google.gson.annotations.SerializedName

data class NotificationTelemetryRequestModel(
    @SerializedName("text") val text: String,
    @SerializedName("from") val from: String,
    @SerializedName("messageId") val messageId: String,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("report") val notificationTelemetryReport: NotificationTelemetryReport,

    ) {
    data class NotificationTelemetryReport(
        @SerializedName("externalId") val externalId: String,
        @SerializedName("destAdd") val destAdd: String,
        @SerializedName("fcmDestAdd") val fcmDestAdd: String,
    )
}
