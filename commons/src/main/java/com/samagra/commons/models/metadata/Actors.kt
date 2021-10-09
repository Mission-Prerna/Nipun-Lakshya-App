package com.samagra.commons.models.metadata

import com.google.gson.annotations.SerializedName

data class Actors(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null
)