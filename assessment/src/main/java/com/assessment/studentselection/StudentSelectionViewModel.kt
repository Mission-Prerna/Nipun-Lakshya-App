package com.assessment.studentselection

import android.app.Application
import android.preference.PreferenceManager
import androidx.lifecycle.viewModelScope
import com.data.network.Result
import com.data.repository.StudentsRepository
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.DASHBOARD_SCREEN
import com.samagra.commons.posthog.EID_INTERACT
import com.samagra.commons.posthog.EVENT_TYPE_SYSTEM
import com.samagra.commons.posthog.NL_APP_DASHBOARD
import com.samagra.commons.posthog.NL_DASHBOARD
import com.samagra.commons.posthog.OBJ_TYPE_UI_ELEMENT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.TYPE_SUMMARY
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StudentSelectionViewModel @Inject constructor(
    application: Application,
    private val studentsRepo: StudentsRepository
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

    private var gradeListJob: Job? = null
    private var dbFetchScope: Job? = null
    private val DELETE_SUBMISSION_FAILURE_EVENT = "delete_submission_failure"

    fun addDummyStudents(udise: Long){
        CoroutineScope(Dispatchers.IO).launch {
            studentsRepo.addDummyStudents(udise)
        }
    }
    fun getStudents(grade: Int) {
        viewModelScope.launch {
            try {
                studentsRepo.getStudents(grade).collect {
                    uiMutableState.value = StudentScreenStates.Success(it)
                }
            } catch (t: Throwable){
                uiMutableState.value = StudentScreenStates.Error(t)
            }
        }
    }

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
                when (val submissionStudentIds = studentsRepo.fetchStudents(udise, true)) {
                    is Result.Success -> {
                        if (submissionStudentIds.data!!.startsWith("failure")){
                            sendSubmissionFailureTelemetry(DELETE_SUBMISSION_FAILURE_EVENT,submissionStudentIds.data!!.substring("failure".length+2, submissionStudentIds.data!!.length - 1))
                        }
                        getGradesList()
                    }

                    is Result.Error -> {
                        gradesListMutableState.value = GradesStates.Error(Exception("yet to sync submission"))
                    }

                    else -> {
                    }
                }
            } catch (t: Throwable){
                gradesListMutableState.value = GradesStates.Error(t)
            }
        }
    }

    private fun sendSubmissionFailureTelemetry(event: String, studentIds: String) {
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val cData = ArrayList<Cdata>()
        cData.add(Cdata("studentIds", studentIds))
        val properties = PostHogManager.createProperties(
            DASHBOARD_SCREEN,
            EVENT_TYPE_SYSTEM,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_DASHBOARD, cData),
            Edata(NL_DASHBOARD, TYPE_SUMMARY),
            Object.Builder().id("Sync Submissions").type(OBJ_TYPE_UI_ELEMENT).build(),
            defaultSharedPreferences
        )
        PostHogManager.capture(getApplication(), event, properties)
    }

    fun fetchStudentsAssessmentHistoryInfo(udise: Long, grade: Int, month: Int, year: Int){
        getStudentsAssessmentHistory(udise, grade, month, year)
        viewModelScope.launch {
            try {
                studentAssessmentHistoryCompleteInfoMutableState.value = StudentAssessmentHistoryCompleteInfoStates.Loading
                when (val studentAssessmentHistoryInfoResult = studentsRepo.fetchStudentsAssessmentHistory(udise, grade.toString(), month, year)) {
                    is Result.Success -> {
                        studentAssessmentHistoryCompleteInfoMutableState.value = StudentAssessmentHistoryCompleteInfoStates.Success(studentAssessmentHistoryInfoResult.data)
                    }
                    else -> {
                    }
                }
            } catch (t: Throwable){
                studentAssessmentHistoryCompleteInfoMutableState.value = StudentAssessmentHistoryCompleteInfoStates.Error(t)
            }
        }
    }

    private fun getStudentsAssessmentHistory(udise: Long, grade: Int, month: Int, year: Int) {
        studentAssessmentHistoryMutableState.value = StudentAssessmentHistoryStates.Loading
        dbFetchScope?.cancel()
        dbFetchScope = viewModelScope.launch(Dispatchers.IO) {
            try {
                studentsRepo.getStudentsAssessmentHistory(udise,grade,month,year).collect {
                    withContext(Dispatchers.Main){
                        studentAssessmentHistoryMutableState.value = StudentAssessmentHistoryStates.Success(it)
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main){
                    studentAssessmentHistoryMutableState.value = StudentAssessmentHistoryStates.Error(t)
                }
            }
        }
    }

}