package com.samagra.ancillaryscreens.fcm.model

import com.google.gson.annotations.SerializedName

data class UpsertTokenRequest(
    @SerializedName("token") val token: String,
)