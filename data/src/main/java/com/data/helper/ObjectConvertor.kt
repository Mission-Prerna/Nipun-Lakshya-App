package com.data.helper

import com.data.db.models.entity.School
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.samagra.commons.models.schoolsresponsedata.SchoolsData

object ObjectConvertor {

    fun SchoolsData.toSchool(): School {
        return School(
            schoolId = this.schoolId!!,
            block = this.block,
            udise = this.udise!!,
            schoolName = this.schoolName,
            blockId = this.blockId,
            district = this.district,
            districtId = this.districtId,
            nyayPanchayat = this.nyayPanchayat,
            nyayPanchayatId = this.nyayPanchayatId,
            schoolLat = this.schoolLat,
            schoolLong = this.schoolLong,
            geofencingEnabled = this.geofencingEnabled,
            visitStatus = this.visitStatus
        )
    }

    fun SchoolDetailsWithReportHistory.toSchool(): School {
        return School(
            schoolId = this.schoolId!!,
            block = this.block,
            udise = this.udise!!,
            schoolName = this.schoolName,
            blockId = this.blockId,
            district = this.district,
            districtId = this.districtId,
            nyayPanchayat = this.nyayPanchayat,
            nyayPanchayatId = this.nyayPanchayatId,
            schoolLat = this.schoolLat,
            schoolLong = this.schoolLong,
            geofencingEnabled = this.geofencingEnabled,
            visitStatus = this.visitStatus
        )
    }

    fun School.toSchoolData() : SchoolsData {
        return SchoolsData(
            schoolId = this.schoolId,
            block = this.block,
            udise = this.udise,
            schoolName = this.schoolName,
            blockId = this.blockId,
            district = this.district,
            districtId = this.districtId,
            nyayPanchayat = this.nyayPanchayat,
            nyayPanchayatId = this.nyayPanchayatId,
            schoolLat = this.schoolLat,
            schoolLong = this.schoolLong,
            geofencingEnabled = this.geofencingEnabled,
            visitStatus = this.visitStatus
        )
    }
}