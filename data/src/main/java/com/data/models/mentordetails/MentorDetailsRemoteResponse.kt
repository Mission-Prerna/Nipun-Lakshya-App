package com.data.models.mentordetails

import com.data.db.models.entity.CycleDetails
import com.google.gson.annotations.SerializedName
import com.samagra.commons.models.schoolsresponsedata.SchoolsData


data class MentorDetailsRemoteResponse(
    @SerializedName("mentor") val mentor: MentorRemoteResponse? = null,
    @SerializedName("school_list") val schoolList: ArrayList<SchoolsData>? = null,
    @SerializedName("home_overview") val homeOverview: HomeOverviewRemoteResponse? = null,
    @SerializedName("examiner_cycle_details") val cycleDetails: CycleDetails? = null
)