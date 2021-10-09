package com.data.db.models.helper

import androidx.room.ColumnInfo

data class SchoolWithReportHistory(
    @ColumnInfo(name = "school_name")
    val schoolname: String?,
    val udise: Long,
    @ColumnInfo(name = "updated_at")
    val assessmentDate: Long,
    val status: String,
)