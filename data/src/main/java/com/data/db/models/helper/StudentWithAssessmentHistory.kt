package com.data.db.models.helper

import androidx.room.ColumnInfo
import java.util.*

data class StudentWithAssessmentHistory(
    val id: String,
    val name: String,
    @ColumnInfo(name = "roll_no")
    val rollNo: Long,
    val month: Int,
    val grade: Int,
    val status: String?,
    val last_assessment_date: Date?,
    @ColumnInfo(name = "is_place_holder_student")
    var isPlaceHolderStudent: Boolean = false
)