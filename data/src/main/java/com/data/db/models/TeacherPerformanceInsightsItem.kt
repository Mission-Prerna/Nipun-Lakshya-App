package com.data.db.models

import androidx.room.Entity

@Entity(tableName = "teacher_performance_insights_list", primaryKeys = ["month","year","type"])
data class TeacherPerformanceInsightsItem(
    val insights: List<Insight>,
    val period: String,
    val type: String,
    val month: Int,
    val year: Int,
    val updated_at: Long
)