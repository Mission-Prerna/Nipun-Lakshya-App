package com.data.models.history

import com.data.db.models.entity.SchoolStatusHistory

class SchoolStatusHistoryResponse (
    val grade: Int,
    val period: String,
    val students: List<SchoolStatusHistory>
)