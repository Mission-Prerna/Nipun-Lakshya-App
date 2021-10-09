package com.assessment.studentselection

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.assessment.R
import com.data.network.Result
import com.data.repository.CycleDetailsRepository
import com.data.repository.SchoolsRepository
import com.data.repository.StudentsRepository
import com.samagra.commons.basemvvm.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExaminerStudentSelectionViewModel @Inject constructor(
    application: Application,
    private val studentsRepo: StudentsRepository,
    private val schoolRepository: SchoolsRepository,
    private val cycleDetailsRepository: CycleDetailsRepository
) : BaseViewModel(application = application) {

    private val uiMutableState: MutableStateFlow<StudentScreenStates> =
        MutableStateFlow(StudentScreenStates.Loading)
    val uiState: StateFlow<StudentScreenStates> = uiMutableState.asStateFlow()

    private val gradesListMutableState: MutableStateFlow<GradesStates> =
        MutableStateFlow(GradesStates.Loading)

    val gradesListState: StateFlow<GradesStates> = gradesListMutableState.asStateFlow()

    private val studentAssessmentHistoryCompleteInfoMutableState: MutableStateFlow<StudentAssessmentHistoryCompleteInfoStates> =
        MutableStateFlow(StudentAssessmentHistoryCompleteInfoStates.Loading)

    val studentAssessmentHistoryCompleteInfoState: StateFlow<StudentAssessmentHistoryCompleteInfoStates> =
        studentAssessmentHistoryCompleteInfoMutableState.asStateFlow()

    private val studentAssessmentHistoryMutableState: MutableStateFlow<StudentAssessmentHistoryStates> =
        MutableStateFlow(StudentAssessmentHistoryStates.Loading)

    val studentAssessmentHistoryState: StateFlow<StudentAssessmentHistoryStates> =
        studentAssessmentHistoryMutableState.asStateFlow()

    private val assessmentCountMutableState: MutableStateFlow<StudentAssessmentHistoryStates> =
        MutableStateFlow(StudentAssessmentHistoryStates.Loading)

    val assessmentCountState: StateFlow<StudentAssessmentHistoryStates> =
        assessmentCountMutableState.asStateFlow()

    private var cycleId: Int? = null

    init {
        viewModelScope.launch {
            initCycleId()
        }
    }

    private suspend fun initCycleId() {
        cycleId = cycleDetailsRepository.getCurrentCycleId()
    }

    private var gradeListJob: Job? = null
    private var dbFetchScope: Job? = null

    fun getGradesList() {
        gradeListJob?.cancel()
        gradeListJob = viewModelScope.launch {
            try {
                studentsRepo.getGradesList().collect {
                    gradesListMutableState.value = GradesStates.Success(it)
                }
            } catch (t: Throwable) {
                gradesListMutableState.value = GradesStates.Error(t)
            }
        }
    }

    fun fetchStudents(udise: Long) {
        viewModelScope.launch {
            try {
                gradesListMutableState.value = GradesStates.Loading
                if (this@ExaminerStudentSelectionViewModel.cycleId == null) {
                    cycleDetailsRepository.getCurrentCycleId()
                }
                when (studentsRepo.fetchStudents(udise, true)) {

                    // TODO :: hardcoded data
                    is Result.Success -> {
                        getGradesList()
                        withContext(Dispatchers.IO) {
                            schoolRepository.fetchStudentStatusHistories(
                                udise, listOf(1, 2, 3), cycleId!!
                            )
                        }
                    }

                    is Result.Error -> {
                        gradesListMutableState.value =
                            GradesStates.Error(Exception("yet to sync submission"))
                    }

                    else -> {
                    }
                }
            } catch (t: Throwable) {
                gradesListMutableState.value = GradesStates.Error(t)
            }
        }
    }

    fun fetchStudentsAssessmentHistoryInfo(
        udise: Long,
        grade: Int
    ) {

        viewModelScope.launch {
            if (this@ExaminerStudentSelectionViewModel.cycleId == null) {
                initCycleId()
            }
            getStudentsAssessmentHistory(udise, grade, cycleId!!)
            try {
                studentAssessmentHistoryCompleteInfoMutableState.value =
                    StudentAssessmentHistoryCompleteInfoStates.Loading
                withContext(Dispatchers.IO) {
                    val studentAssessmentHistoryInfoResult =
                        schoolRepository.fetchStudentStatusHistories(
                            udise,
                            listOf(grade),
                            cycleId!!
                        )
                    withContext(Dispatchers.Main) {
                        when (studentAssessmentHistoryInfoResult) {
                            is Result.Success -> {
                                studentAssessmentHistoryCompleteInfoMutableState.value =
                                    StudentAssessmentHistoryCompleteInfoStates.Success(
                                        studentAssessmentHistoryInfoResult.data
                                    )
                            }

                            else -> {
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                studentAssessmentHistoryCompleteInfoMutableState.value =
                    StudentAssessmentHistoryCompleteInfoStates.Error(t)
            }
        }
    }

    private fun getStudentsAssessmentHistory(udise: Long, grade: Int, cycleId: Int) {
        studentAssessmentHistoryMutableState.value = StudentAssessmentHistoryStates.Loading
        dbFetchScope?.cancel()
        dbFetchScope = viewModelScope.launch(Dispatchers.IO) {
            try {
                studentsRepo.getStudentsAssessmentHistory(udise, grade, cycleId).collect {
                    withContext(Dispatchers.Main) {
                        studentAssessmentHistoryMutableState.value =
                            StudentAssessmentHistoryStates.Success(it)
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    studentAssessmentHistoryMutableState.value =
                        StudentAssessmentHistoryStates.Error(t)
                }
            }
        }
    }

    fun loadData(udise: Long) {
        fetchStudents(udise)
    }

    fun calculateSchoolAssessmentCount(udise: Long) {
        viewModelScope.launch {
            if (cycleId == null) {
                initCycleId()
            }
            studentsRepo.getSchoolAssessmentCount(udise = udise, cycleId = cycleId!!).collect {
                val countTxt = "${it.totalNonPending}/${it.totalStudents}"
                uiMutableState.value = StudentScreenStates.LoadMetricsData(countText = countTxt)
            }
        }
    }

    fun proceedWithSchoolSubmission(udise: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (cycleId == null) {
                initCycleId()
            }
            val studentsWithStatus =
                studentsRepo.getPendingStudents(cycleId = cycleId!!, udise = udise)
            if (studentsWithStatus.isEmpty()) {
                uiMutableState.value = StudentScreenStates.StartSchoolSubmissionFlow
            } else {
                uiMutableState.value =
                    StudentScreenStates.OpenSchoolSubmissionDisclaimer(studentsWithStatus)
            }
        }
    }

    fun updateOfflineStats(udise: Long, ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            if (cycleId == null) {
                initCycleId()
            }
            val result =
                schoolRepository.postSchoolSubmission(
                    cycleId = cycleId!!,
                    udise = udise,
                    context = ctx
                )
            if (result is Result.Error) {
                uiMutableState.value = StudentScreenStates.ShowMessage(result.exception.message ?: ctx.getString(
                    R.string.please_try_again_later))
                return@launch
            }
            schoolRepository.updateHomeStats(cycleId = cycleId!!, udise = udise)
            uiMutableState.value = StudentScreenStates.OpenSchoolReport
        }
    }

}