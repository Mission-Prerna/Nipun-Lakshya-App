package com.data.db.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.data.db.models.helper.AssessmentSchool
import com.google.gson.annotations.SerializedName

@Entity(tableName = "assessment_school_histories",
    primaryKeys = ["grade", "month", "year"],
    indices = [Index(value = ["grade", "month", "year"])])
class AssessmentSchoolHistory(
    val grade: Int,
    var total: Int,
    var assessed: Int,
    var successful: Int,
    val period: String,
    val year : Int,
    val month : Int,
    @ColumnInfo(name = "updated_at")
    var updatedAt : Long
) : AssessmentSchool