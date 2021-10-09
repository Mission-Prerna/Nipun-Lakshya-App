package com.samagra.ancillaryscreens.data.login

data class UserTokenInfoData(
    val refreshToken: String?,
    val refreshTokenId: String,
    val token: String?,
    val tokenExpirationInstant: Long,
    val user: UserDetailsData
)