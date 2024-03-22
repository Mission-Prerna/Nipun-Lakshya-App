package com.samagra.commons.models.submitresultsdata

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SubmitResultsModel(
    @SerializedName("submission_timestamp") var submissionDate: Long? = null,
    @SerializedName("grade") var grade: Int? = null,
    @SerializedName("subject_id") var subjectId: Int? = null,
    @SerializedName("mentor_id") var mentor_id: Int? = null,
    @SerializedName("actor_id") var actorId: Int? = null,
    @SerializedName("block_id") var blockId: Int? = null,
    @SerializedName("assessment_type_id") var assessmentTypeId: Int? = null,
    @SerializedName("udise") var udise: Long? = null,
    @SerializedName("no_of_student") var noOfStudent: Int? = null,
    @SerializedName("results") var students: ArrayList<StudentResults> = arrayListOf(),
    @SerializedName("app_version_code") var appVersionCode: Int? = null,
    @SerializedName("flowUUID") var flowUUID : String? = null
): Serializable
