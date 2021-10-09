package com.samagra.ancillaryscreens.data.otp

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    val otp: String,
    val phone_no: String
)

data class CreatePinRequest(
    @SerializedName("pin") val pin: String,
)