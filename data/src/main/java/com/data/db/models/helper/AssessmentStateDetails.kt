package com.data.db.models.helper

import androidx.room.ColumnInfo
import com.data.FlowType
import com.data.db.models.entity.AssessmentState
import com.google.gson.annotations.SerializedName

data class AssessmentStateDetails(
    val id: Int? = null,

    @ColumnInfo(name = "student_id")
    val studentId: String,

    @ColumnInfo(name = "ref_ids")
    val refIds: MutableList<String>,

    @ColumnInfo(name = "competency_id")
    val competencyId: Int? = null,

    @ColumnInfo(name = "flow_type")
    val flowType: FlowType,

    var result: String? = null,

    @ColumnInfo(name = "state_status")
    var stateStatus: FlowStateStatus,

    @ColumnInfo(name = "learning_outcome")
    val learningOutcome: String,

    @ColumnInfo(name = "student_name")
    val studentName: String,

    @ColumnInfo(name = "subject_name")
    val subjectName: String,

    @SerializedName("grade")
    @ColumnInfo(name = "grade")
    val studentGrade: Int,

    @ColumnInfo(name = "subject_id")
    val subjectId: Int,

    ) {
    fun getAsAssessmentState(): AssessmentState {
        return AssessmentState(id, studentId, refIds, competencyId, flowType, result, stateStatus)
    }
}