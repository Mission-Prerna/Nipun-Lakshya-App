package com.samagra.data.configmodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NipunCriteriaModel(
    @SerializedName("grade")
    var grade: Int?,
    @SerializedName("subject")
    var subject: String?,
    @SerializedName("flow_type")
    var flow_type: String?,
    @SerializedName("nipun_criteria")
    var nipun_criteria: String?
) : Serializable