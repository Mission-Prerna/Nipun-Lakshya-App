package com.samagra.ancillaryscreens.data.otp

data class ApiResponseModel<T>(
    val status: Status,
    val loginResponse:T,
    val error: ArrayList<HttpErrorDatum>
)