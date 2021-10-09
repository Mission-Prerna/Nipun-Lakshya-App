package com.samagra.commons.models.mentordetails

import com.google.gson.annotations.SerializedName
import com.samagra.commons.models.schoolsresponsedata.SchoolsData


data class MentorDetailsRemoteResponse(
    @SerializedName("mentor") val mentor: MentorRemoteResponse? = null,
    @SerializedName("school_list") val schoolList: ArrayList<SchoolsData>? = null,
    @SerializedName("home_overview") val homeOverview: HomeOverviewRemoteResponse? = null
)