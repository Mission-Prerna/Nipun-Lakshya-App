package com.samagra.commons.models.surveydata

import com.google.gson.annotations.SerializedName

data class SurveyModel(
    @SerializedName("submission_timestamp") var submissionTimestamp: Long? = null,
    @SerializedName("grade") var grade: Int? = null,
    @SerializedName("actor_id") var actorId: Int? = null,
    @SerializedName("subject_id") var subjectId: Int? = null,
    @SerializedName("udise") var udise: Long? = null,
    @SerializedName("app_version_code") var appVersionCode: Int? = null,
    @SerializedName("questions") var questions: ArrayList<SurveyResultData> = arrayListOf()
)