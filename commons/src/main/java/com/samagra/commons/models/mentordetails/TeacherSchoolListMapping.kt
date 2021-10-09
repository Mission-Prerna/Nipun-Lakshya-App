package com.samagra.commons.models.mentordetails

import com.google.gson.annotations.SerializedName
import com.samagra.commons.models.schoolsresponsedata.SchoolsData

data class TeacherSchoolListMapping(
    @SerializedName("school_list") val schoolList: SchoolsData? = null
)