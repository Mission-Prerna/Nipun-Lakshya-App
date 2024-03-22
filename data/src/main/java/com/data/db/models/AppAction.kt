package com.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_actions")
data class AppAction(
    @PrimaryKey
    val id: Long = 0,

    @ColumnInfo(name = "domain")
    val domain: String,

    @ColumnInfo(name = "action")
    val action: String,

    @ColumnInfo(name = "requested_at")
    val requested_at: Long,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)