package com.samagra.commons.models.metadata

import com.google.gson.annotations.SerializedName

data class WorkflowRefIds(
    @SerializedName("competency_id") val competencyId: Int? = null,
    @SerializedName("grade") val grade: Int? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("ref_ids") val refIds: ArrayList<String>? = null,
    @SerializedName("subject_id") val subjectId: Int? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("assessment_type_id") val assessmentTypeId: Int? = null
)