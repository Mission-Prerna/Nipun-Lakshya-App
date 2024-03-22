package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.data.models.submissions.SubmitResultsModel

@Keep
@Entity(
    tableName = "assessment_submissions",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["student_id"]
        )
    ],
    indices = [Index(value = ["student_id"])]
)
class AssessmentSubmission {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "student_id")
    var studentId: String = ""

    @ColumnInfo(name = "student_submissions")
    var studentSubmissions: SubmitResultsModel? = null
}
