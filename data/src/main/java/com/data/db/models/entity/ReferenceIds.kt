package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "ref_ids", primaryKeys = ["subject_id", "competency_id", "type", "grade"], indices = [Index(value = ["subject_id", "competency_id", "type", "grade"])])
data class ReferenceIds(

    @SerializedName("subject_id")
    @ColumnInfo(name = "subject_id")
    val subjectId: Int,

    @SerializedName("is_active")
    @ColumnInfo(name = "is_active")
    val isActive: Boolean? = null,

    @SerializedName("ref_ids")
    @ColumnInfo(name = "ref_ids")
    val refIds: MutableList<String>,

    @SerializedName("competency_id")
    @ColumnInfo(name = "competency_id")
    val competencyId: Int,

    @SerializedName("type")
    @ColumnInfo(name = "type")
    val type: String,

    @SerializedName("assessment_type_id")
    @ColumnInfo(name = "assessment_type_id")
    val assessmentTypeId: Int? = null,

    val grade: Int

)