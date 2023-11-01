package com.assessment.studentselection

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.data.network.Result
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
                when (studentsRepo.fetchStudents(udise, true)) {
                    is Result.Success -> {
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

    fun fetchStudentsAssessmentHistoryInfo(udise: Long, grade: Int, month: Int, year: Int){
        getStudentsAssessmentHistory(grade, month, year)
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

    private fun getStudentsAssessmentHistory(grade: Int, month: Int, year: Int) {
        studentAssessmentHistoryMutableState.value = StudentAssessmentHistoryStates.Loading
        dbFetchScope?.cancel()
        dbFetchScope = viewModelScope.launch(Dispatchers.IO) {
            try {
                studentsRepo.getStudentsAssessmentHistory(grade,month,year).collect {
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