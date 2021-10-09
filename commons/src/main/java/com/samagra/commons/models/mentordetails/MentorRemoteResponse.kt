package com.samagra.commons.models.mentordetails

import com.google.gson.annotations.SerializedName

data class MentorRemoteResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("designation_id") val designationId: Int? = null,
    @SerializedName("district_id") val districtId: Int? = null,
    @SerializedName("district_name") val districtName: String? = null,
    @SerializedName("block_id") val blockId: Int? = null,
    @SerializedName("block_town_name") val blockTownName: String? = null,
    @SerializedName("officer_name") val officerName: String? = null,
    @SerializedName("phone_no") val phoneNo: String? = null,
    @SerializedName("actor_id") val actorId: Int? = null,
    @SerializedName("teacher_school_list_mapping") val teacherSchoolListMapping: TeacherSchoolListMapping? = null
)