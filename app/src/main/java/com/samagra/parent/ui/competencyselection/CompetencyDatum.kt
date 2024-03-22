package com.samagra.parent.ui.competencyselection

import java.io.Serializable

data class CompetencyDatum(
    var id: Int,
    var learningOutcome: String,
    var subjectId: Int? = null
) : Serializable {
    override fun equals(other: Any?): Boolean {
        return other is CompetencyDatum && other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

}