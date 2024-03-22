package com.data.repository

import android.content.Context
import com.data.db.models.StudentAssessmentHistoryCompleteInfo
import com.data.db.models.entity.School
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.data.db.models.helper.SchoolWithReportHistory
import com.data.network.Result
import com.samagra.commons.basemvvm.BaseRepository
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import kotlinx.coroutines.flow.Flow
import kotlin.collections.ArrayList

abstract class SchoolsRepository : BaseRepository() {
    abstract suspend fun getSchoolsAssessmentHistory(
        schoolUdise: Long
    ): SchoolWithReportHistory

    abstract suspend fun getSchools(): List<School>

    abstract fun insertSchools(schoolList: ArrayList<SchoolsData>?)

    abstract suspend fun getSchoolsStatusHistory(): Flow<List<SchoolDetailsWithReportHistory>>

    abstract suspend fun fetchStudentStatusHistories(
        udise: Long,
        grades: List<Int>, cycleId: Int
    ): Result<StudentAssessmentHistoryCompleteInfo?>

    abstract suspend fun fetchSchoolStatusHistories(
        cycleId: Int
    ): Result<Unit>

    abstract suspend fun postSchoolSubmission(
        cycleId: Int,
        udise: Long,
        context: Context
    ) : Result<Unit>

    abstract suspend fun updateHomeStats(cycleId: Int, udise: Long)
}