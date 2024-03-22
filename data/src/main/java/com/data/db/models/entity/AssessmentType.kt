package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "assessment_types")
data class AssessmentType(
    @PrimaryKey(autoGenerate = false)
    val id: Int? = null,
    val name: String? = null
)