package com.samagra.commons.models.mentordetails

import com.google.gson.annotations.SerializedName

data class Grades(
    @SerializedName("grade") var grade: Int? = null,
    @SerializedName("total_assessments") var totalAssessments: Int? = null
)