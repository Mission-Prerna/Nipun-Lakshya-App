package com.data.db.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.samagra.commons.models.schoolsresponsedata.SchoolsData

@Entity(tableName = "cycle_details")
data class CycleDetails(
    @ColumnInfo(name = "end_date")
    @SerializedName("end_date")
    val endDate: String,
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "start_date")
    @SerializedName("start_date")
    val startDate: String,
    @ColumnInfo(name = "class_1_nipun_percentage")
    @SerializedName("class_1_nipun_percentage")
    val class1NipunPercentage: Int = 75,
    @ColumnInfo(name = "class_2_nipun_percentage")
    @SerializedName("class_2_nipun_percentage")
    val class2NipunPercentage: Int = 75,
    @ColumnInfo(name = "class_3_nipun_percentage")
    @SerializedName("class_3_nipun_percentage")
    val class3NipunPercentage: Int = 75,
)