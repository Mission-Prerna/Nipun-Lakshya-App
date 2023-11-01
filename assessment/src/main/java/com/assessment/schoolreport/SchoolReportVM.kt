package com.assessment.schoolreport

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.assessment.R
import com.assessment.common.AssessmentHeaderModel
import com.assessment.schoolreport.data.AssessmentStatus
import com.assessment.schoolreport.data.SchoolStudentReport
import com.data.db.models.helper.StudentWithAssessmentHistory
import com.data.network.Result
import com.data.repository.CycleDetailsRepository
import com.data.repository.SchoolsRepository
import com.data.repository.StudentsRepository
import com.samagra.commons.AppPreferences
import com.samagra.commons.basemvvm.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SchoolReportVM @Inject constructor(
    private val application: Application,
    private val studentsRepository: StudentsRepository,
    private val schoolsRepository: SchoolsRepository,
    private val cycleDetailsRepository: CycleDetailsRepository,
) : BaseViewModel(application = application) {

    private val reportMutableState: MutableStateFlow<SchoolReportState> =
        MutableStateFlow(SchoolReportState.Loading)

    val reportState: StateFlow<SchoolReportState> by lazy {
        reportMutableState.asStateFlow()
    }

    private var isFetching = false

    fun getReportForSchool(udise: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            delay(1200)
            Timber.d("getReportForSchool: $udise")
            try {
                val headerModel: AssessmentHeaderModel?
                val schoolStatus: AssessmentStatus
                val studentUIModel = mutableListOf<SchoolStudentReport>()

                //School report
                schoolsRepository.getSchoolsAssessmentHistory(udise).let { history ->
                    Timber.d("getReportForSchool school history: $history")
                    schoolStatus = history.status.toAssessmentStatus()
                    headerModel = AssessmentHeaderModel(
                        name = history.schoolname ?: "",
                        identifier = history.udise,
                        date = SimpleDateFormat.getDateInstance()
                            .format(Date(history.assessmentDate)),
                        mentorType = AppPreferences.getSelectedUserType() ?: "",
                        mentorName = AppPreferences.getUser()?.officer_name ?: ""
                    )
                }
                Timber.d("getReportForSchool: header: $headerModel")

                //Students report
                studentsRepository.getSchoolsStudentsAssessmentHistory(udise)
                    .collect { studentReport ->
                        if (studentReport.isEmpty()) {
                            Timber.d("getReportForSchool: studentReport Empty & not fetching")
                            handleSchoolReportEmpty(udise = udise)
                        } else {
                            Timber.d("getReportForSchool student report: ${studentReport.size}")
                            handleSchoolReport(
                                studentReport = studentReport,
                                studentUIModel = studentUIModel,
                                headerModel = headerModel,
                                schoolStatus = schoolStatus
                            )
                        }
                    }
            } catch (t: Throwable) {
                Timber.e(t, "getReportForSchool: ")
                reportMutableState.emit(SchoolReportState.Error(t))
            }
        }
    }

    private suspend fun handleSchoolReportEmpty(udise: Long) {
        withContext(Dispatchers.IO) {
            if (isFetching) return@withContext
            isFetching = true
            Timber.d("handleSchoolReportEmpty: ")
            when (studentsRepository.fetchStudents(udise, false)) {
                is Result.Success -> {
                    Timber.d("getReportForSchool: fetch Students Successful")
                    val cycleId = cycleDetailsRepository.getCurrentCycleId()
                    Timber.d("getReportForSchool: current cycle: $cycleId")
                    when (schoolsRepository.fetchStudentStatusHistories(
                        udise = udise,
                        grades = listOf(1, 2, 3),
                        cycleId = cycleId
                    )) {
                        is Result.Error -> {
                            Timber.d("getReportForSchool: Error in fetchStudentStatusHistories")
                            emitError()
                        }

                        else -> {
                            //IGNORE
                        }
                    }
                }

                else -> {
                    emitError()
                }
            }
        }
    }

    private suspend fun handleSchoolReport(
        studentReport: List<StudentWithAssessmentHistory>,
        studentUIModel: MutableList<SchoolStudentReport>,
        headerModel: AssessmentHeaderModel?,
        schoolStatus: AssessmentStatus
    ) {
        var totalStudents = 0
        var successfulStudents = 0
        studentReport.forEach { student ->
            //Timber.d("getReportForSchool: for each student: $student")
            totalStudents++
            val studentAssessmentStatus =
                student.status.toAssessmentStatus()
            if (studentAssessmentStatus == AssessmentStatus.SUCCESSFUL) {
                Timber.d("getReportForSchool: ${student.name} is successful")
                successfulStudents++
            }
            studentUIModel.add(
                SchoolStudentReport(
                    name = student.name,
                    grade = application.getString(
                        R.string.grade_is,
                        student.grade
                    ),
                    rollNumber = application.getString(
                        R.string.roll_is,
                        student.rollNo
                    ),
                    status = studentAssessmentStatus
                )
            )
        }

        val reportState = SchoolReportState.SchoolReportSuccess(
            headerInfo = headerModel,
            schoolStatus = schoolStatus,
            students = studentUIModel,
            bottomText = application.getString(
                R.string.successful_count,
                successfulStudents,
                totalStudents
            )
        )
        Timber.d("getReportForSchool: reportState: sent to UI")
        reportMutableState.emit(reportState)
    }

    private suspend fun emitError() {
        reportMutableState.emit(
            SchoolReportState.Error(
                IllegalStateException(
                    application.getString(R.string.school_report_not_found)
                )
            )
        )
    }

}

private fun String?.toAssessmentStatus() = when (this) {
    "pass" -> AssessmentStatus.SUCCESSFUL
    "fail" -> AssessmentStatus.UNSUCCESSFUL
    else -> AssessmentStatus.PENDING
}

sealed class SchoolReportState {

    object Loading : SchoolReportState()
    data class SchoolReportSuccess(
        val headerInfo: AssessmentHeaderModel?,
        val schoolStatus: AssessmentStatus,
        val students: List<SchoolStudentReport>,
        val bottomText: String,
    ) : SchoolReportState()

    class Error(val exception: Throwable) : SchoolReportState()

}