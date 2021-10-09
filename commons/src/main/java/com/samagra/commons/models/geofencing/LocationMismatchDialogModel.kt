package com.samagra.commons.models.geofencing

import com.google.gson.annotations.SerializedName

data class LocationMismatchDialogModel(
    @SerializedName("title") var title: String? = null,
    @SerializedName("description") var description: String? = null
)