package com.samagra.ancillaryscreens.data.login

import com.samagra.ancillaryscreens.data.otp.Status

data class LoginModel(
    val id: String?,
    val params: Params?,
    val responseCode: String?,
    val result: Result?,
    val status: Status?
)