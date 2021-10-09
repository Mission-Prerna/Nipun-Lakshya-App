package com.samagra.commons.models.overviewdata

import com.google.gson.annotations.SerializedName

data class HomeOverviewModel(
    @SerializedName("visited_schools")
    var visitedSchools: Int? = null,
    @SerializedName("total_assessments")
    var totalAssessments: Int? = null,
    @SerializedName("average_assessment_time")
    var averageAssessmentTime: Int? = null,
    @SerializedName("grades")
    var grades: ArrayList<Grades> = arrayListOf()
)