package com.data.repository.impl

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.data.R
import com.data.db.dao.CycleDetailsDao
import com.data.db.dao.ExaminerPerformanceInsightsDao
import com.data.db.dao.SchoolSubmissionsDao
import com.data.db.dao.SchoolsDao
import com.data.db.dao.SchoolsStatusHistoryDao
import com.data.db.dao.StudentsAssessmentHistoryDao
import com.data.db.models.ExaminerInsight
import com.data.db.models.StudentAssessmentHistoryCompleteInfo
import com.data.db.models.entity.School
import com.data.db.models.entity.SchoolStatusHistory
import com.data.db.models.entity.SchoolSubmission
import com.data.db.models.entity.StudentAssessmentHistory
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.data.db.models.helper.SchoolWithReportHistory
import com.data.db.models.helper.StudentWithAssessmentHistory
import com.data.helper.ObjectConvertor.toSchool
import com.data.models.submissions.StudentNipunStates
import com.data.network.AssessmentService
import com.data.network.Result
import com.data.network.SchoolService
import com.data.repository.SchoolsRepository
import com.samagra.commons.AppPreferences
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Arrays
import javax.inject.Inject

private const val keyForSchool = "school"

class SchoolRepositoryImpl @Inject constructor(
    private val schoolStatusHistoryDao: SchoolsStatusHistoryDao,
    private val schoolsDao: SchoolsDao,
    private val assessmentService: AssessmentService,
    private val schoolService: SchoolService,
    private val studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao,
    private val examinerPerformanceInsightsDao: ExaminerPerformanceInsightsDao,
    private val schoolSubmissionsDao: SchoolSubmissionsDao,
    private val cycleDetailsDao: CycleDetailsDao
) : SchoolsRepository() {
    override suspend fun getSchoolsAssessmentHistory(schoolUdise: Long): SchoolWithReportHistory {
        return schoolStatusHistoryDao.getSchoolStatuses(udise = schoolUdise)
    }

    override suspend fun getSchoolsStatusHistory(): Flow<List<SchoolDetailsWithReportHistory>> {
        return schoolStatusHistoryDao.getSchoolsWithStatus()
    }

    // For fetching data for students of particular school in examiner flow
    override suspend fun fetchStudentStatusHistories(
        udise: Long,
        grades: List<Int>, cycleId: Int
    ): Result<StudentAssessmentHistoryCompleteInfo?> {
        try {
            val response = assessmentService.getSchoolStatusHistories(
                udise.toString(),
                grades.joinToString(","),
                cycleId = cycleId,
                "hi",
                Constants.BEARER_ + AppPreferences.getUserAuth()
            ).execute()
            if (response.isSuccessful) {
                val studentsAssessmentHistoryInfo = response.body()
                var allStudents = mutableListOf<StudentAssessmentHistory>()
                studentsAssessmentHistoryInfo?.forEach { entry ->
                    val modifiedStudents = entry.students.map { originalStudent ->
                        StudentAssessmentHistory(
                            id = originalStudent.id,
                            status = originalStudent.status,
                            lastAssessmentDate = originalStudent.lastAssessmentDate,
                            month = 0,
                            year = 0,
                            cycleId = cycleId,
                            udise = udise
                        )
                    }
                    allStudents.addAll(modifiedStudents)
                }

                deleteUnusedLocalEntries(
                    modifiedStudents = allStudents,
                    cycleId = cycleId,
                    grades = grades
                )

                allStudents = modifyEntriesWihOfflineData(
                    modifiedStudents = allStudents,
                    cycleId = cycleId
                )
                studentsAssessmentHistoryDao.insert(studentAssessmentHistoryList = allStudents)
                return Result.Success(studentsAssessmentHistoryInfo?.get(0))
            }
            return Result.Error(Exception(response.errorBody()?.string() ?: response.message()))
        } catch (e: Exception) {
            Timber.e(e)
            return Result.Error(e)
        }
    }

    private suspend fun deleteUnusedLocalEntries(
        modifiedStudents: MutableList<StudentAssessmentHistory>,
        cycleId: Int,
        grades: List<Int>
    ) {
        val localHistories = studentsAssessmentHistoryDao.getAllHistories(cycleId, grades)
        val additionalEntries = mutableListOf<String>()
        val nonServerEntries = mutableListOf<String>()
        val serverStudentIds: List<String> = modifiedStudents.map { it.id }
        localHistories.forEach { studentHistoryFromLocal ->
            if (serverStudentIds.contains(studentHistoryFromLocal.id)) {
                additionalEntries.add(studentHistoryFromLocal.id)
            } else {
                nonServerEntries.add(studentHistoryFromLocal.id)
            }
        }
        Log.i("Morziz-Opti ", "Delete Ids  - " + Arrays.toString(nonServerEntries.toTypedArray()))
        val deleteForIds = studentsAssessmentHistoryDao.deleteForIds(nonServerEntries)
        Log.i("Morziz-Opti ", "Deleted " + deleteForIds)
    }

    override suspend fun fetchSchoolStatusHistories(cycleId: Int): Result<Unit> {
        try {
            val response = schoolService.fetchSchoolStatusHistory(
                cycleId = cycleId,
                Constants.BEARER_ + AppPreferences.getUserAuth()
            ).execute()
            if (response.isSuccessful) {
                val statusHistories = response.body()
                statusHistories?.forEach {
                    it.cycleId = cycleId
                }
                val data =
                    modifySchoolHistoriesWihOfflineData(
                        histories = statusHistories,
                        cycleId = cycleId
                    )
                try {
                    schoolStatusHistoryDao.insert(data)
                } catch (e: SQLiteConstraintException) {
                    Timber.e(e, "insert with safety")
                    schoolStatusHistoryDao.insertWithSafety(data)
                }
                return Result.Success(Unit)
            }
            return Result.Error(Exception(response.errorBody()?.string() ?: response.message()))
        } catch (e: Exception) {
            Timber.e(e)
            return Result.Error(e)
        }
    }

    override suspend fun postSchoolSubmission(
        cycleId: Int,
        udise: Long,
        ctx: Context
    ): Result<Unit> {
        val submission = SchoolSubmission()
        submission.cycleId = cycleId
        submission.udise = udise
        schoolSubmissionsDao.insert(submission)

        val cycleDetails = cycleDetailsDao.getCycleDetails(cycleId = cycleId)
        val students =
            studentsAssessmentHistoryDao.getStudentsWithStatus(cycleId = cycleId, udise = udise)
        if (students.isEmpty()) {
            return Result.Error(RuntimeException(ctx.getString(R.string.no_students_synced_try_later)))
        }
        val isGrade1Nipun =
            checkIfNipun(students.filter { it.grade == 1 }, cycleDetails.class1NipunPercentage)
        val isGrade2Nipun =
            checkIfNipun(students.filter { it.grade == 2 }, cycleDetails.class2NipunPercentage)
        val isGrade3Nipun =
            checkIfNipun(students.filter { it.grade == 3 }, cycleDetails.class3NipunPercentage)
        val isPassed = isGrade1Nipun && isGrade2Nipun && isGrade3Nipun
        val schoolHistory = SchoolStatusHistory(
            udise = udise,
            status = if (isPassed) StudentNipunStates.pass else StudentNipunStates.fail,
            updatedAt = System.currentTimeMillis(),
            cycleId = cycleId
        )
        schoolStatusHistoryDao.insert(schoolHistory)
        return Result.Success(Unit)
    }

    private fun checkIfNipun(
        students: List<StudentWithAssessmentHistory>,
        passPercentage: Int
    ): Boolean {
        val passedStudentPercent =
            (students.count { it.status == StudentNipunStates.pass } * 100) / students.size
        return passedStudentPercent >= passPercentage
    }

    override suspend fun updateHomeStats(cycleId: Int, udise: Long) {
        val examinerInsights =
            examinerPerformanceInsightsDao.getExaminerPerformanceInsights().first()

        var totalSchoolsAccessed = 0

        val schoolStatusHistory =
            schoolStatusHistoryDao.getSchoolStatusByCycleAndUdise(cycleId, udise)

        schoolStatusHistory.forEach {
            if (it.status == StudentNipunStates.pass || it.status == StudentNipunStates.fail) {
                ++totalSchoolsAccessed
            }
        }

        val newExaminerInsights = examinerInsights.map { item ->
            val modifiedInsights = item.insights.map { insight ->
                when (insight.type) {
                    keyForSchool -> {
                        val modifiedCount = totalSchoolsAccessed
                        ExaminerInsight(
                            if (modifiedCount != 0) modifiedCount + insight.count else insight.count,
                            insight.label,
                            insight.type
                        )
                    }

                    else -> {
                        insight
                    }
                }
            }
            if (item.insights[0].type == keyForSchool) {   // updating time only for object where school is a part of
                val newUpdatedTime = System.currentTimeMillis()
                item.copy(insights = modifiedInsights, updated_at = newUpdatedTime)
            } else {
                item.copy(insights = modifiedInsights)
            }
        }
        examinerPerformanceInsightsDao.insert(newExaminerInsights)
    }

    private fun modifySchoolHistoriesWihOfflineData(
        histories: List<SchoolStatusHistory>?,
        cycleId: Int
    ): MutableList<SchoolStatusHistory> {
        val localEntriesMap = schoolStatusHistoryDao.getSchoolStatuses(cycleId)
            .associateBy { it.udise }
        // Modify the entries based on offline data & If there is no local entry, use the server entry
        return histories?.map { serverEntry ->
            localEntriesMap[serverEntry.udise]?.let { localEntry ->
                if (localEntry.updatedAt > serverEntry.updatedAt) {
                    localEntry
                } else {
                    serverEntry
                }
            } ?: serverEntry
        }?.toMutableList() ?: mutableListOf()
    }

    private suspend fun modifyEntriesWihOfflineData(
        modifiedStudents: MutableList<StudentAssessmentHistory>,
        cycleId: Int
    ): MutableList<StudentAssessmentHistory> {
        val localHistories = studentsAssessmentHistoryDao.getAllHistories(cycleId)
        val localEntriesMap = localHistories
            .associateBy { it.id }
        // Modify the entries based on offline data & If there is no local entry, use the server entry
        return modifiedStudents.map { serverEntry ->
            localEntriesMap[serverEntry.id]?.let { localEntry ->
                if (localEntry.lastAssessmentDate > serverEntry.lastAssessmentDate) {
                    localEntry
                } else {
                    serverEntry
                }
            } ?: serverEntry
        }.toMutableList()
    }

    override suspend fun getSchools(): List<School> {
        return schoolsDao.getSchools()
    }

    override fun insertSchools(schoolList: ArrayList<SchoolsData>?) {
        if (schoolList != null) {
            val updatedList = schoolList.map { it.toSchool() }
            schoolsDao.insert(schools = updatedList)
        }
    }


}