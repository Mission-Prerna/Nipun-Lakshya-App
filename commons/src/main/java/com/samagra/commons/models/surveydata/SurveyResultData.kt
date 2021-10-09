package com.samagra.commons.models.surveydata

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SurveyResultData(
    @SerializedName("question_id")
    var qId: String,
    @SerializedName("value")
    var value: String,
) : Serializable