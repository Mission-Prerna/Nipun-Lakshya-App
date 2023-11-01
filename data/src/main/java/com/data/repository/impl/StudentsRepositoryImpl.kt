package com.data.repository.impl

import com.data.db.dao.AssessmentSubmissionsDao
import com.data.db.dao.StudentsAssessmentHistoryDao
import com.data.db.dao.StudentsDao
import com.data.db.models.StudentAssessmentHistoryCompleteInfo
import com.data.db.models.entity.Student
import com.data.db.models.entity.StudentAssessmentHistory
import com.data.db.models.helper.SchoolAssessmentCount
import com.data.db.models.helper.StudentWithAssessmentHistory
import com.data.network.Result
import com.data.network.StudentsService
import com.data.repository.StudentsRepository
import com.samagra.commons.AppPreferences.getUserAuth
import com.samagra.commons.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StudentsRepositoryImpl @Inject constructor(
    private val service: StudentsService,
    private val studentsDao: StudentsDao,
    private val studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao,
    private val submissionsDao: AssessmentSubmissionsDao
) : StudentsRepository() {

    override fun getStudents(grade: Int): Flow<MutableList<Student>> {
        return studentsDao.getStudents(grade)
    }

    override suspend fun getStudentNameById(id: String): String {
        return studentsDao.getStudentNameById(id)
    }
    override suspend fun getStudentGradeById(id: String): Int {
        return studentsDao.getStudentGradeById(id)
    }

    override fun getGradesList(): Flow<List<Int>> {
        return studentsDao.getGradesList()
    }

    override suspend fun fetchStudents(udise: Long, addDummyStudents: Boolean): Result<Unit> {
        return withContext(Dispatchers.Default) {
            try {
                val response =
                    service.getStudents(
                        udise = udise.toString(),
                        token = Constants.BEARER_ + getUserAuth()
                    )
                if (response.isSuccessful) {
                    //Add school udise in each entry
                    response.body()?.map {
                        it.copy(schoolUdise = udise)
                    }?.let { students ->
                        studentsDao.insert(students)
                    }
                    if (addDummyStudents) {
                        addDummyStudents(udise)
                    }
                    return@withContext Result.Success(Unit)
                }
                return@withContext Result.Error(
                    Exception(
                        response.errorBody()?.string() ?: response.message()
                    )
                )
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }
    }

    override suspend fun addDummyStudents(schoolUdise: Long) {
        studentsDao.insert(getDummyStudents(schoolUdise = schoolUdise))
    }

    override suspend fun fetchStudentsAssessmentHistory(
        udise: Long,
        grade: String,
        month: Int,
        year: Int
    ): Result<StudentAssessmentHistoryCompleteInfo?> {
        try {
            val response = service.getStudentAssessmentHistoryInfo(
                udise.toString(),
                Constants.BEARER_ + getUserAuth(),
                "hi",
                grade,
                month,
                year
            )
            if (response.isSuccessful) {
                val studentsAssessmentHistoryInfo = response.body()
                var allStudents = mutableListOf<StudentAssessmentHistory>()
                studentsAssessmentHistoryInfo?.forEach { entry ->
                    val modifiedStudents = entry.students.map { originalStudent ->
                        StudentAssessmentHistory(
                            id = originalStudent.id,
                            status = originalStudent.status,
                            lastAssessmentDate = originalStudent.lastAssessmentDate,
                            month = month,
                            year = year,
                            udise = udise
                        )
                    }
                    allStudents.addAll(modifiedStudents)
                }

                allStudents = modifyEntriesWihOfflineData(allStudents, month, year)
                studentsAssessmentHistoryDao.insert(allStudents)
                return Result.Success(studentsAssessmentHistoryInfo?.get(0))
            }
            return Result.Error(Exception(response.errorBody()?.string() ?: response.message()))
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun getPendingStudents(
        cycleId: Int,
        udise: Long
    ): List<StudentWithAssessmentHistory> {
        return studentsAssessmentHistoryDao.getPendingStudentsWithStatus(udise, cycleId)
    }

    override fun getSchoolAssessmentCount(cycleId: Int, udise: Long): Flow<SchoolAssessmentCount> {
        return studentsAssessmentHistoryDao.getSchoolAssessmentCount(udise = udise, cycleId = cycleId)
    }

    override fun getStudentsAssessmentHistory(
        udise: Long,
        grade: Int,
        cycleId: Int
    ): Flow<MutableList<StudentWithAssessmentHistory>> {
        return studentsAssessmentHistoryDao.getStudentsByGradeCycle(udise, grade, cycleId)
    }

    private suspend fun modifyEntriesWihOfflineData(
        modifiedStudents: MutableList<StudentAssessmentHistory>,
        month: Int,
        year: Int
    ): MutableList<StudentAssessmentHistory> {
        val localEntriesMap = studentsAssessmentHistoryDao.getAllHistories(month, year)
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

    override fun getStudentsAssessmentHistory(
        grade: Int,
        month: Int,
        year: Int
    ): Flow<MutableList<StudentWithAssessmentHistory>> {
        return studentsAssessmentHistoryDao.getStudentsByGradeMonthYear(grade, month, year)
    }

    override fun getSchoolsStudentsAssessmentHistory(
        schoolUdise: Long
    ): Flow<List<StudentWithAssessmentHistory>> {
        return studentsAssessmentHistoryDao.getStudentsForUdise(schoolUdise)
    }

    private fun getDummyStudents(schoolUdise: Long): MutableList<Student> {
        val dummyStudents = mutableListOf<Student>()
        val range = -3..-1
        for (i in range) {
            val virtualId = i.toString()
            dummyStudents.add(
                Student(
                    id = virtualId,
                    name = "",
                    i * -1,
                    rollNo = i.toLong(),
                    isPlaceHolderStudent = true,
                    schoolUdise = schoolUdise
                )
            )
        }
        return dummyStudents
    }


}