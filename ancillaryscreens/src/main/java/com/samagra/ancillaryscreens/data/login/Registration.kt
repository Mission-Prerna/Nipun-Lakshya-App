package com.samagra.ancillaryscreens.data.login

data class Registration(
    val roles: List<String>,
    val username: String,
    val usernameStatus: String,
    val verified: Boolean
)