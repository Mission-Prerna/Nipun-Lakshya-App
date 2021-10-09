package com.samagra.ancillaryscreens.data.otp

data class ErrorDatum(
    val errorCode: String,
    val errorText: String
)

data class HttpErrorDatum(
    val statusCode: String,
    val message: String
)