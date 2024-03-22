package com.data.repository

import com.data.db.models.StudentAssessmentHistoryCompleteInfo
import com.data.db.models.entity.Student
import com.data.db.models.helper.SchoolAssessmentCount
import com.data.db.models.helper.StudentWithAssessmentHistory
import com.data.network.Result
import com.samagra.commons.basemvvm.BaseRepository
import kotlinx.coroutines.flow.Flow

abstract class StudentsRepository : BaseRepository() {
    abstract suspend fun insertStudents(students: List<Student>)
    abstract fun getStudents(grade: Int): Flow<MutableList<Student>>
    abstract suspend fun getStudentNameById(id: String): String
    abstract suspend fun getStudentGradeById(id: String): Int
    abstract fun getGradesList(): Flow<List<Int>>
    abstract suspend fun fetchStudents(
        udise: Long,
        addDummyStudents: Boolean
    ): Result<String>

    abstract suspend fun fetchStudentsAssessmentHistory(
        udise: Long,
        grade: String,
        month: Int,
        year: Int
    ): Result<StudentAssessmentHistoryCompleteInfo?>

    abstract fun getStudentsAssessmentHistory(
        udise: Long,
        grade: Int,
        month: Int,
        year: Int
    ): Flow<MutableList<StudentWithAssessmentHistory>>

    abstract fun getStudentsAssessmentHistory(
        udise: Long,
        grade: Int,
        cycleId: Int
    ): Flow<MutableList<StudentWithAssessmentHistory>>

    abstract suspend fun addDummyStudents(schoolUdise: Long)

    abstract fun getSchoolsStudentsAssessmentHistory(
        schoolUdise: Long
    ): Flow<List<StudentWithAssessmentHistory>>

    abstract fun getSchoolAssessmentCount(cycleId: Int, udise: Long): Flow<SchoolAssessmentCount>

    abstract suspend fun getPendingStudents(
        cycleId: Int,
        udise: Long
    ): List<StudentWithAssessmentHistory>

}