package com.data.models.metadata

import com.data.db.models.entity.Actor
import com.data.db.models.entity.AssessmentType
import com.data.db.models.entity.Competency
import com.data.db.models.entity.Designation
import com.data.db.models.entity.ReferenceIds
import com.data.db.models.entity.Subjects
import com.google.gson.annotations.SerializedName

data class MetaDataRemoteResponse(
    @SerializedName("actors") val actors: ArrayList<Actor>? = null,
    @SerializedName("designations") val designations: ArrayList<Designation>? = null,
    @SerializedName("subjects") val subjects: ArrayList<Subjects>? = null,
    @SerializedName("assessment_types") val assessmentTypes: ArrayList<AssessmentType>? = null,
    @SerializedName("competency_mapping") val competencyMapping: ArrayList<Competency>? = null,
    @SerializedName("workflow_ref_ids") val workflowRefIds: ArrayList<ReferenceIds>? = null
)
