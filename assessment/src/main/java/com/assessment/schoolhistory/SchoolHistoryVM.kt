package com.assessment.schoolhistory

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.assessment.studentselection.GradesStates
import com.data.db.models.entity.AssessmentSchoolHistory
import com.data.db.models.helper.AssessmentSchool
import com.data.models.history.AssessmentSchoolPlaceHolder
import com.data.repository.AssessmentsRepository
import com.data.repository.StudentsRepository
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.EID_INTERACT
import com.samagra.commons.posthog.EVENT_TYPE_USER_ACTION
import com.samagra.commons.posthog.NL_APP_SCHOOL_HISTORY
import com.samagra.commons.posthog.NL_APP_USER_SELECTION
import com.samagra.commons.posthog.NL_SCHOOL_HISTORY
import com.samagra.commons.posthog.NL_USERSELECTION
import com.samagra.commons.posthog.OBJ_TYPE_UI_ELEMENT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.SCHOOL_HISTORY_SCREEN
import com.samagra.commons.posthog.TYPE_CLICK
import com.samagra.commons.posthog.USERSELECTION_SCREEN
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchoolHistoryVM @Inject constructor(
    application: Application,
    private val studentsRepo: StudentsRepository,
    private val assessmentsRepository: AssessmentsRepository
) : BaseViewModel(application = application) {

    private var dbFetchScope: Job? = null
    private val gradesListMutableState: MutableStateFlow<GradesStates> =
        MutableStateFlow(GradesStates.Loading)

    val gradesListState: StateFlow<GradesStates> = gradesListMutableState.asStateFlow()

    private val schoolHistoryMutableState: MutableStateFlow<SchoolHistoryStates> =
        MutableStateFlow(SchoolHistoryStates.Loading)

    val schoolHistoryState: StateFlow<SchoolHistoryStates> =
        schoolHistoryMutableState.asStateFlow()

    fun getGradesList() {
        viewModelScope.launch {
            try {
                studentsRepo.getGradesList().collect {
                    gradesListMutableState.value = GradesStates.Success(it)
                }
            } catch (t: Throwable) {
                gradesListMutableState.value = GradesStates.Error(t)
            }
        }
    }

    fun getStudentsAssessmentHistory(udise: Long, grades: List<Int>) {
        schoolHistoryMutableState.value = SchoolHistoryStates.Loading
        dbFetchScope?.cancel()
        dbFetchScope = viewModelScope.launch {
            try {
                val finalList = mutableListOf<AssessmentSchool>()
                // fetch data from db
                assessmentsRepository.getSchoolAssessmentHistory(grades).collect {
                    finalList.clear()
                    if (it.size > 1) {
                        finalList.addAll(mergeGradeResults(it))
                    }else {
                        finalList.addAll(it)
                    }
                    finalList.add(
                        0,
                        AssessmentSchoolPlaceHolder(
                            total = "कुल\nविद्यार्थी",
                            assessed = "विद्यार्थी\nआकलन किए",
                            successful = "विद्यार्थी\nनिपुण",
                            period = "माह"
                        )
                    )
                    schoolHistoryMutableState.value = SchoolHistoryStates.Success(finalList)
                }
            } catch (t: Throwable) {
                schoolHistoryMutableState.value = SchoolHistoryStates.Error(t)
            }
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            assessmentsRepository.fetchSchoolAssessmentHistory(udise, grades)
        }
    }

    private fun mergeGradeResults(it: MutableList<AssessmentSchoolHistory>): MutableList<AssessmentSchoolHistory> {
        val resultsMap = linkedMapOf<Int, AssessmentSchoolHistory>()
        it.forEach {
            val assessment = resultsMap[it.month]
            if (assessment == null) {
                resultsMap[it.month] = it
            } else {
                assessment.total = assessment.total + it.total
                assessment.assessed = assessment.assessed + it.assessed
                assessment.successful = assessment.successful + it.successful
            }
        }
        return resultsMap.values.toMutableList()
    }

    fun sendTelemetry(event: String, grade: Int, schoolsData: SchoolsData, context: Context) {
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val cData = ArrayList<Cdata>()
        cData.add(Cdata("grade", grade.toString()))
        cData.add(Cdata("block", schoolsData.block))
        cData.add(Cdata("block_id", schoolsData.blockId.toString()))
        cData.add(Cdata("district", schoolsData.block))
        cData.add(Cdata("district_id", schoolsData.districtId.toString()))
        val properties = PostHogManager.createProperties(
            SCHOOL_HISTORY_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_SCHOOL_HISTORY, cData),
            Edata(NL_SCHOOL_HISTORY, TYPE_CLICK),
            Object.Builder().id("Grade Button").type(OBJ_TYPE_UI_ELEMENT).build(), defaultSharedPreferences
        )
        PostHogManager.capture(context, event, properties)
    }

}