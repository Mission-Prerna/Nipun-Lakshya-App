package com.samagra.commons.models.geofencing

import com.google.gson.annotations.SerializedName

data class GeofencingInitials(
    @SerializedName("fencing_radius") var fencingRadius: Int? = null
)