package com.data.db.models

data class Insight(
    val count: Int,
    val label: String,
    val student_ids: List<String>,
    val identifier: String
)