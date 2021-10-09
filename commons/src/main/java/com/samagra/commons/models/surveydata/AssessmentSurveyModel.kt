package com.samagra.commons.models.surveydata

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class AssessmentSurveyModel(
    var submissionTimeStamp: Long?,
    @SerializedName("username")
    var userName: String?,
    @SerializedName("grade")
    var grade: Int,
    @SerializedName("subject")
    var subject: String?,
    @SerializedName("results")
    var results: String?,
    @SerializedName("school_udise")
    var schoolUdise: Long,
    @SerializedName("actor")
    var actor: String?,
) : RealmObject() {
    constructor() : this(0L,null, 0, null, null, 0, null)
}
