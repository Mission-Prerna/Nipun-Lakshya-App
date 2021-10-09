package com.samagra.ancillaryscreens.data.otp

data class Status(
    val error: ErrorDatum,
    val messageID: String,
    val networkResponseCode: String,
    val phone: String,
    val provider: String,
    val providerResponseCode: String,
    val providerSuccessResponse: String,
    val status: String
)