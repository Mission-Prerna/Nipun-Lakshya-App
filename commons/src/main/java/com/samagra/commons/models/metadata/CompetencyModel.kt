package com.samagra.commons.models.metadata

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CompetencyModel(
    var id: Int,
    var isSelected: Boolean = false,
    @SerializedName("grade")
    val grade: Int,
    @SerializedName("subject_id")
    val subjectId: Int,
    @SerializedName("learning_outcome")
    val learningOutcome: String,
    @SerializedName("Month")
    val month: String,
    @SerializedName("competency_id")
    val cId: Int,
    @SerializedName("flow_state")
    val flowState: Int,
) : Serializable