package com.samagra.commons.models.metadata

import com.google.gson.annotations.SerializedName

//TODO make all vals & add serialized name to all models used here.
data class MetaDataRemoteResponse(
    @SerializedName("actors") val actors: ArrayList<Actors>? = null,
    @SerializedName("designations") val designations: ArrayList<Designations>? = null,
    @SerializedName("subjects") val subjects: ArrayList<Subjects>? = null,
    @SerializedName("assessment_types") val assessmentTypes: ArrayList<AssessmentTypes>? = null,
    @SerializedName("competency_mapping") val competencyMapping: ArrayList<CompetencyModel>? = null,
    @SerializedName("workflow_ref_ids") val workflowRefIds: ArrayList<WorkflowRefIds>? = null
)