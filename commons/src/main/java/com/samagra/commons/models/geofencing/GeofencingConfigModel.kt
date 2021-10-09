package com.samagra.commons.models.geofencing

import com.google.gson.annotations.SerializedName

data class GeofencingConfigModel(
  @SerializedName("enabled") var enabled: Boolean? = null,
  @SerializedName("actors_disabled") var actorsDisabled: java.util.ArrayList<Int>? = arrayListOf(),
  @SerializedName("location_mismatch_error_dialog") var dialogProps: LocationMismatchDialogModel? = LocationMismatchDialogModel(),
  @SerializedName("geofencing_initials") var geofencingInitials: GeofencingInitials? = GeofencingInitials()
)