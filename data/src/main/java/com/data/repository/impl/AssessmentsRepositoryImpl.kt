package com.data.repository.impl

import android.content.SharedPreferences
import com.data.FlowType
import com.data.db.dao.AssessmentSchoolHistoryDao
import com.data.db.dao.AssessmentStateDao
import com.data.db.dao.AssessmentSubmissionsDao
import com.data.db.dao.ExaminerPerformanceInsightsDao
import com.data.db.dao.StudentsAssessmentHistoryDao
import com.data.db.dao.StudentsDao
import com.data.db.dao.TeacherPerformanceInsightsDao
import com.data.db.models.ExaminerInsight
import com.data.db.models.Insight
import com.data.db.models.TeacherPerformanceInsightsItem
import com.data.db.models.entity.AssessmentSchoolHistory
import com.data.db.models.entity.AssessmentState
import com.data.db.models.entity.AssessmentSubmission
import com.data.db.models.entity.StudentAssessmentHistory
import com.data.db.models.helper.AssessmentStateDetails
import com.data.db.models.helper.FlowStateStatus
import com.data.helper.AssessmentFlowUtils
import com.data.models.stateresult.AssessmentStateResult
import com.data.models.submissions.StudentNipunStates
import com.data.models.submissions.StudentResults
import com.data.models.submissions.SubmitResultsModel
import com.data.models.ui.ScorecardData
import com.data.network.AssessmentService
import com.data.network.Result
import com.data.repository.AssessmentsRepository
import com.google.gson.Gson
import com.samagra.commons.AppPreferences
import com.samagra.commons.AppProperties
import com.samagra.commons.CommonUtilities
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.utils.CommonConstants.ODK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

private const val EXAMINER = 2
private const val TEACHER = 3
private const val PENDING = "pending"
private const val STUDENT = "student"
private const val GRADE_1 = "grade_1"
private const val GRADE_2 = "grade_2"
private const val GRADE_3 = "grade_3"

class AssessmentsRepositoryImpl @Inject constructor(
    private val service: AssessmentService,
    private val assessmentStateDao: AssessmentStateDao,
    private val assessmentSubmissionsDao: AssessmentSubmissionsDao,
    private val studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao,
    private val teacherPerformanceInsightsDao: TeacherPerformanceInsightsDao,
    private val examinerPerformanceInsightsDao: ExaminerPerformanceInsightsDao,
    private val schoolHistoryDao: AssessmentSchoolHistoryDao,
    private val studentsDao: StudentsDao,
    private val gson: Gson,
    private val prefs: SharedPreferences
) : AssessmentsRepository() {
    override fun getStates(): MutableList<AssessmentState> {
        return assessmentStateDao.getStates()
    }

    override fun observerStates(): Flow<List<AssessmentState>> {
        return assessmentStateDao.observeStates()
    }

    override fun observerIncompleteStates(): Flow<List<AssessmentStateDetails>> {
        return assessmentStateDao.observeIncompleteStates()
    }

    override fun createStates(grade: Int): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun clearStatesAsync() {
        assessmentStateDao.deleteAllAsync()
    }

    override fun clearStates() {
        assessmentStateDao.deleteAll()
    }

    override suspend fun getResultsForScoreCard(): List<ScorecardData> {
        val states = assessmentStateDao.getDetailedStates()
        val scoreCards = mutableListOf<ScorecardData>()
        states.forEach { state ->
            val assessmentStateResult =
                gson.fromJson(state.result, AssessmentStateResult::class.java)
            var isPassed = false
            var competencyScore = "नहीं हुआ"
            var competencyScoreDesc = ""
            if (state.stateStatus == FlowStateStatus.COMPLETED) {
                val moduleResult = assessmentStateResult.moduleResult
                if (moduleResult.module.equals(ODK, true)) {
                    competencyScore = "${moduleResult.achievement}/${moduleResult.totalQuestions}"
                    competencyScoreDesc = "सही"
                } else {
                    competencyScore = "${moduleResult.achievement}"
                    competencyScoreDesc = "शब्द/मिनट"
                }
                isPassed = moduleResult.isPassed
            }
            scoreCards.add(
                ScorecardData(
                    competencyDescription = state.learningOutcome,
                    competencyScore = competencyScore,
                    competencyScoreDescription = competencyScoreDesc,
                    isPassed = isPassed
                )
            )
        }
        return scoreCards
    }

    override suspend fun convertStatesToSubmissions(
        udise: Long,
        cycleId: Int?,
        updateSchoolHistory: Boolean
    ): Result<Unit> {
        val states = assessmentStateDao.getDetailedStates()
        val mentorDetailsStr = prefs.getString(UserConstants.MENTOR_DETAIL, "");
        val map = mutableMapOf<String, MutableList<StudentResults>>()
        val submissionsMap = mutableMapOf<String, SubmitResultsModel>()
        val mentorDetails = gson.fromJson(
            mentorDetailsStr,
            com.samagra.commons.models.Result::class.java
        )

        var totalTime: Long = 0
        states.forEach { state ->
            val result = state.result
            val assessmentStateResult = if (result.isNullOrEmpty()) {
                AssessmentFlowUtils.getDummyAssessmentResult(
                    state.getAsAssessmentState(),
                    state.studentGrade,
                    state.subjectName
                )
            } else {
                gson.fromJson(result, AssessmentStateResult::class.java)
            }
            val studentResult = StudentResults(studentId = state.studentId)
            studentResult.achievement = assessmentStateResult.moduleResult.achievement
            studentResult.competencyId = state.competencyId
            studentResult.endTime = assessmentStateResult.moduleResult.endTime
            studentResult.startTime = assessmentStateResult.moduleResult.startTime
            studentResult.isNetworkActive = assessmentStateResult.moduleResult.isNetworkActive
            studentResult.isPassed = assessmentStateResult.moduleResult.isPassed
            studentResult.module = assessmentStateResult.moduleResult.module
            studentResult.sessionCompleted = assessmentStateResult.moduleResult.sessionCompleted
            studentResult.statement = assessmentStateResult.moduleResult.statement
            studentResult.studentName = state.studentName
            studentResult.successCriteria = assessmentStateResult.moduleResult.successCriteria
            studentResult.totalQuestions = assessmentStateResult.moduleResult.totalQuestions
            studentResult.totalTimeTaken = 0  // TODO : Check if required
            studentResult.workflowRefId = assessmentStateResult.workflowRefId
            if (state.flowType == FlowType.ODK) {
                studentResult.odkResults =
                    assessmentStateResult.odkResultsData?.results ?: ArrayList()
            }
            val timeTaken = CommonUtilities.getTimeDifferenceMilis(
                assessmentStateResult.moduleResult.startTime,
                assessmentStateResult.moduleResult.endTime
            )
            totalTime += timeTaken
            val studentsResults = map[state.studentId]
            if (studentsResults.isNullOrEmpty()) {
                map[state.studentId] = mutableListOf(studentResult)
            } else {
                studentsResults.add(studentResult)
            }

            if (submissionsMap.containsKey(state.studentId).not()) {
                val submitResultsModel = SubmitResultsModel()
                submitResultsModel.submissionDate = Date().time
                submitResultsModel.actorId = mentorDetails.actorId
                submitResultsModel.appVersionCode = AppProperties.versionCode
                submitResultsModel.assessmentTypeId = 1 //
                submitResultsModel.blockId = mentorDetails.block_id
                submitResultsModel.grade = state.studentGrade
                submitResultsModel.noOfStudent = 1
                submitResultsModel.subjectId = state.subjectId
                submitResultsModel.udise = udise
                submitResultsModel.studentResults = map[state.studentId]!!
                submissionsMap[state.studentId] = submitResultsModel
            }
        }

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val submissions = mutableListOf<AssessmentSubmission>()
        val assessmentHistories = mutableListOf<StudentAssessmentHistory>()
        submissionsMap.forEach {
            val submission = AssessmentSubmission()
            submission.studentId = it.key
            submission.studentSubmissions = it.value
            submissions.add(submission)
            var studentNipunState = StudentNipunStates.pending
            inner@ for (result in it.value.studentResults) {
                if (result.isPassed == false) {
                    studentNipunState = StudentNipunStates.fail
                    break@inner
                } else if (result.isPassed == true) {
                    studentNipunState = StudentNipunStates.pass
                }
            }
            assessmentHistories.add(
                StudentAssessmentHistory(
                    id = it.key,
                    status = studentNipunState,
                    lastAssessmentDate = System.currentTimeMillis(),
                    month = if(cycleId == null) month else 0,
                    year = if(cycleId == null) year else 0,
                    udise = udise,
                    cycleId = cycleId ?: 0
                )
            )
        }
        if (updateSchoolHistory) {
            modifySchoolHistoryOffline(assessmentHistories, month, year)
        }
        studentsAssessmentHistoryDao.insert(assessmentHistories)
        val submissionCount = insertAssessmentSubmission(submissions)
        if (submissionCount.isNotEmpty()) {
            clearStates()
            if (mentorDetails.actorId == TEACHER) {
                updateHomeScreenStatsForTeacher(assessmentHistories)
            } else if (mentorDetails.actorId == EXAMINER) {
                updateHomeScreenStatsForExaminer(assessmentHistories)
            }
        }
        return Result.Success(Unit)
    }

    private suspend fun updateHomeScreenStatsForTeacher(assessmentHistories: List<StudentAssessmentHistory>) {
        val teacherInsights = teacherPerformanceInsightsDao.getTeacherPerformanceInsights().first()
        val newTeacherInsights: MutableList<TeacherPerformanceInsightsItem> = mutableListOf()
        teacherInsights.forEach {
            if (it.month == Calendar.getInstance()[Calendar.MONTH] + 1) {
                val newInsights: MutableList<Insight> = mutableListOf()
                val assessmentStudentIdList: List<String> = assessmentHistories.map { it.id }
                var existingStudentIdList: List<String> = mutableListOf()
                it.insights.forEach {
                    if (it.identifier == StudentNipunStates.assessed) {
                        // calculation of assessed
                        existingStudentIdList = it.student_ids
                        val studentIdsSet =
                            (assessmentStudentIdList + existingStudentIdList).toSet()
                        val uniqueStudentAccessedIdsList = studentIdsSet.toList() // insert in db
                        val countOfAssessed = uniqueStudentAccessedIdsList.size // insert in db
                        newInsights.add(
                            Insight(
                                countOfAssessed,
                                it.label,
                                uniqueStudentAccessedIdsList,
                                it.identifier
                            )
                        )
                    } else if (it.identifier == StudentNipunStates.pass) {
                        // calculation of nipun
                        existingStudentIdList = it.student_ids
                        val retakeAssessmentList =
                            assessmentHistories.filter { it.id in existingStudentIdList }
                        val failAssessmentList =
                            retakeAssessmentList.filter { it.status == StudentNipunStates.fail }
                        val failedIdList =
                            failAssessmentList.map { it.id } // ids to be removed from student_ids in insights
                        val failCount = failAssessmentList.size // decrease this count from existing

                        val newAssessmentTakenList = assessmentHistories - retakeAssessmentList
                        val passAssessmentList =
                            newAssessmentTakenList.filter { it.status == StudentNipunStates.pass }
                        val passedIdList =
                            passAssessmentList.map { it.id } // ids to be added in student_ids
                        val passCount = passAssessmentList.size // add this count to existing

                        val countOfNipun = it.count - failCount + passCount
                        val mutableStudentIdList = existingStudentIdList.toMutableList()
                        mutableStudentIdList.removeAll(failedIdList)
                        mutableStudentIdList.addAll(passedIdList)
                        newInsights.add(
                            Insight(
                                countOfNipun,
                                it.label,
                                mutableStudentIdList,
                                it.identifier
                            )
                        )
                    }
                }
                val newUpdatedTime = System.currentTimeMillis()
                val newTeacherPerformanceInsightsItem = TeacherPerformanceInsightsItem(
                    newInsights,
                    it.period,
                    it.type,
                    it.month,
                    it.year,
                    newUpdatedTime
                )
                newTeacherInsights.add(newTeacherPerformanceInsightsItem)
            }
        }
        teacherPerformanceInsightsDao.insert(newTeacherInsights)
    }

    private suspend fun updateHomeScreenStatsForExaminer(assessmentHistories: List<StudentAssessmentHistory>) {
        val examinerInsights = examinerPerformanceInsightsDao.getExaminerPerformanceInsights().first()

        val gradeAndAssessedMap = mutableMapOf<Int, Int>().withDefault { 0 }
        var totalStudentsAccessed = 0

        assessmentHistories.forEach {
            if (it.status != PENDING) {
                ++totalStudentsAccessed
                val gradeForCountIncrease = studentsDao.getStudentGradeById(it.id)
                gradeAndAssessedMap[gradeForCountIncrease] = gradeAndAssessedMap.getValue(gradeForCountIncrease) + 1
            }
        }

        val newExaminerInsights = examinerInsights.map { item ->
            val modifiedInsights = item.insights.map { insight ->
                when (insight.type) {
                    STUDENT -> {
                        val modifiedCount = totalStudentsAccessed
                        ExaminerInsight(if (modifiedCount!=0) modifiedCount + insight.count else insight.count, insight.label, insight.type)
                    }
                    GRADE_1 -> {
                        val modifiedCount = gradeAndAssessedMap[1] ?: 0
                        ExaminerInsight(if (modifiedCount!=0) modifiedCount + insight.count else insight.count , insight.label, insight.type)
                    }
                    GRADE_2 -> {
                        val modifiedCount = gradeAndAssessedMap[2] ?: 0
                        ExaminerInsight(if (modifiedCount!=0) modifiedCount + insight.count else insight.count , insight.label, insight.type)
                    }
                    GRADE_3 -> {
                        val modifiedCount = gradeAndAssessedMap[3] ?: 0
                        ExaminerInsight(if (modifiedCount!=0) modifiedCount + insight.count else insight.count , insight.label, insight.type)
                    }
                    else -> {
                        insight
                    }
                }
            }
            val newUpdatedTime = System.currentTimeMillis()
            item.copy(insights = modifiedInsights, updated_at = newUpdatedTime)
        }
        examinerPerformanceInsightsDao.insert(newExaminerInsights)
    }

    override fun insertAssessmentSubmission(submissions: MutableList<AssessmentSubmission>): List<Long> {
        return assessmentSubmissionsDao.insert(submissions)
    }

    override fun insertAssessmentStates(state: MutableList<AssessmentState>): List<Long> {
        return assessmentStateDao.insert(state)
    }

    override suspend fun updateState(state: AssessmentState) {
        assessmentStateDao.update(state)
    }

    override suspend fun abandonFlow(state: AssessmentStateDetails) {
        val incompleteStates = assessmentStateDao.getIncompleteStates()
        incompleteStates.forEach {
            if (state.id == it.id) {
                it.stateStatus = state.stateStatus
                it.result = state.result
            } else {
                it.stateStatus = FlowStateStatus.SKIPPED
            }
        }
        assessmentStateDao.insert(incompleteStates)
    }


    override suspend fun getSchoolAssessmentHistory(grade: List<Int>): Flow<MutableList<AssessmentSchoolHistory>> {
        return schoolHistoryDao.getHistoriesAsync(grade)
    }

    override fun fetchSchoolAssessmentHistory(
        udise: Long,
        grades: List<Int>
    ): Result<Unit> {
        try {
            val response = service.getAssessmentHistories(
                udise.toString(),
                grades.joinToString(","),
                "hi",
                Constants.BEARER_ + AppPreferences.getUserAuth()
            ).execute()
            if (response.isSuccessful) {
                var histories = mutableListOf<AssessmentSchoolHistory>()
                val gradeSummaries = response.body()
                gradeSummaries?.forEach { gradeSummary ->
                    gradeSummary.summary.forEach {
                        histories.add(
                            AssessmentSchoolHistory(
                                grade = getGrade(gradeSummary.grade),
                                total = it.total,
                                assessed = it.assessed,
                                successful = it.successful,
                                period = it.period,
                                year = it.year,
                                month = it.month,
                                updatedAt = it.updatedAt
                            )
                        )
                    }
                }
                modifyEntriesWithOfflineData(histories, grades)
                schoolHistoryDao.insert(histories)
                return Result.Success(Unit)
            }
            return Result.Error(Exception(response.errorBody()?.string() ?: response.message()))
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    private fun modifyEntriesWithOfflineData(
        histories: MutableList<AssessmentSchoolHistory>,
        grades: List<Int>
    ) {
        val localEntriesMap = schoolHistoryDao.getHistories(grades)
            .associateBy { generateCombinedKey(it) }

        for (index in histories.indices) {
            val serverEntry = histories[index]
            val localEntry = localEntriesMap[generateCombinedKey(serverEntry)]

            if (localEntry != null && localEntry.updatedAt > serverEntry.updatedAt) {
                histories[index] = localEntry
            }
        }
    }

    private suspend fun modifySchoolHistoryOffline(
        assessmentHistories: MutableList<StudentAssessmentHistory>,
        month: Int,
        year: Int
    ) {
        assessmentHistories.forEach { newStudentAssessmentHistory ->
            try {
                // School History will not get affected in anonymous student
                // Anonymous student ids are -1,-2 & -3 for grade 1, 2, 3 respectively
                if (newStudentAssessmentHistory.id.toLong() < 0) {
                    return@forEach
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
            val existingStudentAssessmentHistory =
                studentsAssessmentHistoryDao.getHistoryByAssessmentInfo(
                    newStudentAssessmentHistory.id,
                    month,
                    year
                )
            var schoolHistory =
                schoolHistoryDao.getHistoryByAssessmentInfo(
                    existingStudentAssessmentHistory.grade,
                    month,
                    year
                )

            // if no school assessment history is present
            if (schoolHistory == null) {
                val studentCount =
                    studentsDao.getStudentsCountByGrade(existingStudentAssessmentHistory.grade)
                schoolHistory = AssessmentSchoolHistory(
                    grade = existingStudentAssessmentHistory.grade,
                    total = studentCount,
                    assessed = 1,
                    successful = if (newStudentAssessmentHistory.status == StudentNipunStates.pass) 1 else 0,
                    period = AssessmentFlowUtils.getCurrentMonthNameInHindi(),
                    year = year,
                    month = month,
                    updatedAt = System.currentTimeMillis()
                )
                schoolHistoryDao.insert(schoolHistory)
                return
            }


            if (existingStudentAssessmentHistory.status != newStudentAssessmentHistory.status) {
                val currentTimeMillis = System.currentTimeMillis()
                if (existingStudentAssessmentHistory.status == StudentNipunStates.pending) {
                    schoolHistory.assessed++
                    if (newStudentAssessmentHistory.status == StudentNipunStates.pass) {
                        schoolHistory.successful++
                    }
                } else if (newStudentAssessmentHistory.status == StudentNipunStates.fail) {
                    schoolHistory.successful--
                } else if (newStudentAssessmentHistory.status == StudentNipunStates.pass) {
                    schoolHistory.successful++
                }
                schoolHistory.updatedAt = currentTimeMillis
                schoolHistoryDao.insert(schoolHistory)
            }

        }
    }

    private fun generateCombinedKey(it: AssessmentSchoolHistory): String {
        return it.grade.toString().plus("_").plus(it.month).plus("_").plus(it.year)
    }

    private fun getGrade(gradeText: String): Int {
        if (gradeText.contains("1")) {
            return 1
        } else if (gradeText.contains("2")) {
            return 2
        } else if (gradeText.contains("3")) {
            return 3
        }
        return 1
    }

}