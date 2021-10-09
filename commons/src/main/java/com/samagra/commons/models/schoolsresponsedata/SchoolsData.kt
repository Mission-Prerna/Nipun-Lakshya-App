package com.samagra.commons.models.schoolsresponsedata

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import java.io.Serializable

open class SchoolsData(
    var udise: Long?,
    @SerializedName("school_name")
    var schoolName: String?,
    @SerializedName("school_id")
    var schoolId: Int?,
    @SerializedName("is_visited")
    var visitStatus: Boolean?,
    @SerializedName("district_name")
    var district: String?,
    @SerializedName("district_id")
    var districtId: Int?,
    @SerializedName("block_name")
    var block: String?,
    @SerializedName("block_id")
    var blockId: Int?,
    @SerializedName("nyay_panchayat_name")
    var nyayPanchayat: String?,
    @SerializedName("nyay_panchayat_id")
    var nyayPanchayatId: Int?,
    @SerializedName("lat")
    var schoolLat: Double?,
    @SerializedName("long")
    var schoolLong: Double?,
    @SerializedName("geo_fence_enabled")
    var geofencingEnabled: Boolean?,
    ) : Serializable, RealmObject() {
    constructor() : this(null, null, null,null, null, null, null, null,null,null,null,null,null)
}