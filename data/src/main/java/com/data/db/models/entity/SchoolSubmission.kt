package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.data.models.submissions.SubmitResultsModel

@Keep
@Entity(
    tableName = "school_submissions",
    foreignKeys = [
        ForeignKey(
            entity = School::class,
            parentColumns = ["udise"],
            childColumns = ["udise"]
        )
    ],
    indices = [Index(value = ["udise"])]
)
class SchoolSubmission {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "udise")
    var udise: Long = 0

    @ColumnInfo(name = "cycle_id", defaultValue = "0")
    var cycleId: Int = 0
}
