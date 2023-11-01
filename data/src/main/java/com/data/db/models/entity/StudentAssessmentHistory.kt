package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.util.*

@Keep
@Entity(
    tableName = "students_assessment_history",
    primaryKeys = ["id", "month", "year", "cycle_id", "udise"],
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["id"] // This references the "id" column in the Student table
        )
    ]
)
data class StudentAssessmentHistory(
    val id: String,
    val status: String,
    @ColumnInfo(name = "last_assessment_date")
    @SerializedName("last_assessment_date")
    val lastAssessmentDate: Long,
    val month: Int = 0,
    val year: Int = 0,
    @ColumnInfo(name = "cycle_id", defaultValue = "0")
    @SerializedName("cycle_id")
    val cycleId: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val udise: Long = 0
)

