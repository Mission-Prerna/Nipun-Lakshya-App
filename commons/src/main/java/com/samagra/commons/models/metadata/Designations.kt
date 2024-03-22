package com.samagra.commons.models.metadata

import com.google.gson.annotations.SerializedName

data class Designations(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null
)