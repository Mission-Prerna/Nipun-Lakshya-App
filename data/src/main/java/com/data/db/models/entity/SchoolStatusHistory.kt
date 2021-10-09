package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(
    tableName = "school_status_history",
    primaryKeys = ["udise", "cycle_id"],
    foreignKeys = [
        ForeignKey(
            entity = School::class,
            parentColumns = ["udise"],
            childColumns = ["udise"] // This references the "id" column in the School table
        )
    ]
)
data class SchoolStatusHistory(
    var udise: Long,
    var status: String,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    var updatedAt: Long,
    @ColumnInfo(name = "cycle_id", defaultValue = "0")
    @SerializedName("cycle_id")
    var cycleId: Int
)