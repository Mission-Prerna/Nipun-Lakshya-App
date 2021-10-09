package com.samagra.ancillaryscreens.data.otp

data class VerifyOtpRequest(
    val otp: String,
    val phone_no: String
)

