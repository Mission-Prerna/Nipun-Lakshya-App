package com.samagra.ancillaryscreens.data.otp

data class LoginRequest(
    val loginId: String,
    val applicationId: String,
    val password: String
)