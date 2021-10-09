package com.data.repository.impl

import com.data.db.dao.AssessmentStateDao
import com.data.db.dao.AssessmentSubmissionsDao
import com.data.db.dao.StudentsAssessmentHistoryDao
import com.data.db.dao.StudentsDao
import com.data.db.models.StudentAssessmentHistoryCompleteInfo
import com.data.db.models.entity.AssessmentSubmission
import com.data.db.models.entity.Student
import com.data.db.models.entity.StudentAssessmentHistory
import com.data.db.models.helper.SchoolAssessmentCount
import com.data.db.models.helper.StudentWithAssessmentHistory
import com.data.network.AssessmentService
import com.data.network.Result
import com.data.network.StudentsService
import com.data.repository.StudentsRepository
import com.samagra.commons.AppPreferences.getUserAuth
import com.samagra.commons.constants.Constants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.RemoteConfigUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class StudentsRepositoryImpl @Inject constructor(
    private val service: StudentsService,
    private val studentsDao: StudentsDao,
    private val studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao,
    private val submissionsDao: AssessmentSubmissionsDao,
    private val assessmentStateDao: AssessmentStateDao,
    private val assessmentService: AssessmentService
) : StudentsRepository() {

    private val TAG = "DELETION"
    private val STUDENT_DELETION_QUERY = "student_deletion"
    override suspend fun insertStudents(students: List<Student>) {
        studentsDao.insert(students)
    }

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

    override suspend fun fetchStudents(udise: Long, addDummyStudents: Boolean): Result<String> {
        return withContext(Dispatchers.Default) {
            try {
                val response =
                    service.getStudents(
                        udise = udise.toString(),
                        token = Constants.BEARER_ + getUserAuth()
                    )
                if (response.isSuccessful) {
                    //Add school udise in each entry
                    var submissions = ""
                    response.body()?.map {
                        it.copy(schoolUdise = udise)
                    }?.let { studentsFromServer ->
                        val studentsFromDb = studentsDao.getStudentsByUdise(udise)
                        Timber.tag(TAG).i("students from db : ${studentsFromDb.size}")
                        Timber.tag(TAG).i("students from server : ${studentsFromServer.size}")
                        studentsDao.insert(studentsFromServer)
                        val studentsNotInServer =
                            getStudentsNotInServer(studentsFromServer, studentsFromDb)
                        Timber.tag(TAG).i("students not in server : ${studentsNotInServer.size}")

                        if (studentsNotInServer.isNotEmpty()) {
                            val syncJob = async {
                                syncSubmissions(submissionsDao.getSubmissions())
                            }
                            submissions = syncJob.await()
                            Timber.tag(TAG).i("submissions synced for student ids : $submissions")
                        }

                        for (student in studentsNotInServer) {
                            assessmentStateDao.deleteForId(student.id)
                            Timber.tag(TAG).i("assessment state deleted for student id : ${student.id}")
                            studentsAssessmentHistoryDao.deleteForId(student.id)
                            Timber.tag(TAG).i("assessment history deleted for student id : ${student.id}")
                            studentsDao.deleteStudent(student.id)
                            Timber.tag(TAG).i("student deleted for student id : ${student.id}")
                        }
                    }
                    if (addDummyStudents) {
                        addDummyStudents(udise)
                    }
                    return@withContext Result.Success(submissions)
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
    private fun syncSubmissions(
        resultSubmissionList: List<AssessmentSubmission>
    ): String {
        Timber.i("Submissions List %s", resultSubmissionList.size)
        return try {
            if (resultSubmissionList.isNotEmpty()) {
                postStudentsAssessmentSubmissions(resultSubmissionList)
            } else {
                "true"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "false"
        }
    }

    private fun postStudentsAssessmentSubmissions(
        resultSubmissionList: List<AssessmentSubmission>
    ): String {
        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            return "false"
        }
        var isSuccess = true;
        val studentIds = mutableListOf<String>()
        resultSubmissionList.chunked(
            RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getString(RemoteConfigUtils.RESULTS_INSERTION_CHUNK_SIZE).toInt()
        ) {
            val dbIds = mutableListOf<Long>()
            val submissions = mutableListOf<com.data.models.submissions.SubmitResultsModel>()
            it.forEach { model ->
                dbIds.add(model.id)
                studentIds.add(model.studentId)
                submissions.add(model.studentSubmissions!!)
            }
            Timber.i("submission ids list to be deleted! $dbIds")
            val response =
                assessmentService.postSubmissionsForDeletedStudents(Constants.BEARER_ + getUserAuth(),STUDENT_DELETION_QUERY, submissions)
                    .execute()
            if (response.isSuccessful) {
                studentIds.add(0,"success")
                submissionsDao.delete(dbIds)
            } else {
                studentIds.add(0,"failure")
                submissionsDao.delete(dbIds) // TODO:  currently, error from server for deleted students, so deleting submissions
                // Currently not stopping loop if API fails
                isSuccess = false
                Timber.i("Submissions Response Failure - %s", response.message())
                Timber.i("Submissions Response Code - %s", response.code())
                Timber.i("Submissions Response Error - %s", response.errorBody()?.string())
            }
        }
        return studentIds.toString()
    }

    private fun getStudentsNotInServer(studentsFromServer: List<Student>, studentsFromDb: List<Student>): List<Student> {
        val studentsNotInServer = mutableListOf<Student>()

        for (studentDb in studentsFromDb) {
            var found = false
            for (studentServer in studentsFromServer) {
                if (studentDb.id == studentServer.id) {
                    found = true
                    break
                }
            }
            if (!found) {
                if (studentDb.id.toInt()>=0) { //dummy students not to be removed, hence not added to list
                    studentsNotInServer.add(studentDb)
                }
            }
        }

        return studentsNotInServer
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
        udise: Long,
        grade: Int,
        month: Int,
        year: Int
    ): Flow<MutableList<StudentWithAssessmentHistory>> {
        return studentsAssessmentHistoryDao.getStudentsByGradeMonthYear(udise, grade, month, year)
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