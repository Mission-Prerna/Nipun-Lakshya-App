package com.samagra.parent.ui.competencyselection

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CompetencyModelNew(
    @SerializedName("competency_id")
    var id: Int ,
    @SerializedName("grade")
    var grade: Int,
    @SerializedName("subject_id")
    var subjectId: Int,
    @SerializedName("learning_outcome")
    var learningOutcome: String,
    @SerializedName("flow_state")
    var flowState: Int
    ) : Serializable