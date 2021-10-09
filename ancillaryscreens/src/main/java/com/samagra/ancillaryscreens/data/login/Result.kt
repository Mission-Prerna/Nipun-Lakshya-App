package com.samagra.ancillaryscreens.data.login

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("data")
    val userData: UserData,
    val responseMsg: String
)