package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "competencies", foreignKeys = [
    ForeignKey(
        entity = Subjects::class,
        parentColumns = ["id"],
        childColumns = ["subject_id"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )])
data class Competency(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("competency_id")
    val id: Int,

    @SerializedName("subject_id")
    @ColumnInfo(name = "subject_id")
    val subjectId: Int,

    val grade: Int,

    @SerializedName("learning_outcome")
    @ColumnInfo(name = "learning_outcome")
    val learningOutcome : String,

    @SerializedName("flow_state")
    @ColumnInfo(name = "flow_state")
    val flowState : Int,

    @SerializedName("Month")
    val month: String,

)