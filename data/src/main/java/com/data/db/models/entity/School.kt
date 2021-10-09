package com.data.db.models.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
@Entity(tableName = "schools", indices = [Index(value = ["udise"], unique = true)])
data class School(
    var udise: Long,
    @ColumnInfo(name = "school_name")
    @SerializedName("school_name")
    var schoolName: String?,
    @PrimaryKey
    @ColumnInfo(name = "school_id")
    @SerializedName("school_id")
    var schoolId: Int,
    @ColumnInfo(name = "is_visited")
    @SerializedName("is_visited")
    var visitStatus: Boolean?,
    @ColumnInfo(name = "district_name")
    @SerializedName("district_name")
    var district: String?,
    @ColumnInfo(name = "district_id")
    @SerializedName("district_id")
    var districtId: Int?,
    @ColumnInfo(name = "block_name")
    @SerializedName("block_name")
    var block: String?,
    @ColumnInfo(name = "block_id")
    @SerializedName("block_id")
    var blockId: Int?,
    @ColumnInfo(name = "nyay_panchayat_name")
    @SerializedName("nyay_panchayat_name")
    var nyayPanchayat: String?,
    @ColumnInfo(name = "nyay_panchayat_id")
    @SerializedName("nyay_panchayat_id")
    var nyayPanchayatId: Int?,
    @SerializedName("lat")
    @ColumnInfo(name = "lat")
    var schoolLat: Double?,
    @ColumnInfo(name = "long")
    @SerializedName("long")
    var schoolLong: Double?,
    @ColumnInfo(name = "geo_fence_enabled")
    @SerializedName("geo_fence_enabled")
    var geofencingEnabled: Boolean?,
) : Serializable