package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.data.FlowType
import com.data.db.models.helper.FlowStateStatus
import com.google.gson.annotations.SerializedName

@Keep
@Entity(
    tableName = "assessment_state",
    foreignKeys = [
        ForeignKey(
            entity = Competency::class,
            parentColumns = ["id"],
            childColumns = ["competency_id"]
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["student_id"]
        )
    ]
)
data class AssessmentState(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @SerializedName("studentId")
    @ColumnInfo(name = "student_id")
    val studentId: String,

    @SerializedName("refIds")
    @ColumnInfo(name = "ref_ids")
    val refIds: MutableList<String>,

    @ColumnInfo(name = "competency_id")
    val competencyId: Int? = null,

    @ColumnInfo(name = "flow_type")
    val flowType: FlowType,

    var result: String? = null,

    @ColumnInfo(name = "state_status")
    var stateStatus: FlowStateStatus = FlowStateStatus.PENDING

    )