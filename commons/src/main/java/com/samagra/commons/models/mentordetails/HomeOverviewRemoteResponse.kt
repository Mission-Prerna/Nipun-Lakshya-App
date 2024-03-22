package com.samagra.commons.models.mentordetails

import com.google.gson.annotations.SerializedName

data class HomeOverviewRemoteResponse(
    @SerializedName("visited_schools") val visitedSchools: Int? = null,
    @SerializedName("total_assessments") val totalAssessments: Int? = null,
    @SerializedName("average_assessment_time") val averageAssessmentTime: Int? = null,
    @SerializedName("grades") val grades: ArrayList<Grades>? = null,
    @SerializedName("teacher_overview") val teacherOverView: TeacherOverviewData? = null
)