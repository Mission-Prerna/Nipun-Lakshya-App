package com.morziz.network.models

data class ErrorModel(
    val type: ErrorType? = null,
    val message: String? = null,
    val heading: String? = null
)

enum class ErrorType {
    AUTHENTICATION_ERROR, INTERNAL_SERVER_ERROR, INVALID_OTP,
    INTERNET, GPS, USER_NOT_FOUND,VALIDATION_EXCEPTION, TOO_MANY_REQUEST, NO_STOPS_NEARBY
}
