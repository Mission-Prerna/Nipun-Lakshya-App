package com.data.db.models

import androidx.room.Entity

@Entity(tableName = "examiner_performance_insights_list", primaryKeys = ["cycle_id","period"])
data class ExaminerPerformanceInsightsItem(
    val insights: List<ExaminerInsight>,
    val period: String,
    val cycle_id: Int,
    val updated_at: Long
)