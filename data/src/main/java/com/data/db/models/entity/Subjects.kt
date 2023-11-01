package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "subjects")
data class Subjects(
    @PrimaryKey(autoGenerate = false)
    val id: Int? = null,
    val name: String? = null
)