package com.data.db.models

import androidx.room.Entity

@Entity(tableName = "mentor_performance_insights_list", primaryKeys = ["month","year"])
data class MentorPerformanceInsightsItem(
    val totalInsights: List<MentorInsight>,
    val gradesInsights: List<MentorInsight>,
    val month: Int,
    val year: Int,
    val updated_at: Long
)