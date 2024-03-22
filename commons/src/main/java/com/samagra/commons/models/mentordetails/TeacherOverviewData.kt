package com.samagra.commons.models.mentordetails

import com.google.gson.annotations.SerializedName

data class TeacherOverviewData(
    @SerializedName("assessments_total") val assessmentTotal: Int? = null,
    @SerializedName("nipun_total") val nipunTotal: Int? = null,
    @SerializedName("assessments_today") val assessmentsToday: Int? = null,
    @SerializedName("nipun_today") val nipunToday: Int? = null,
)