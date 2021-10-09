package com.samagra.ancillaryscreens.data.login

data class UserDetailsData(
    val active: Boolean,
    val registrations: List<Registration>,
    val usernameStatus: String,
    val verified: Boolean
)