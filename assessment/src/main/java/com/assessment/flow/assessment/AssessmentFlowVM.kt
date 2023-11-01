package com.assessment.flow.assessment

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.assessment.R
import com.assessment.flow.AssessmentConstants
import com.assessment.flow.AssessmentConstants.EXAMINER_ASSESSMENT_QUERY
import com.assessment.flow.AssessmentConstants.EXAMINER_ASSESSMENT_TYPE
import com.assessment.flow.AssessmentConstants.NON_EXAMINER_ASSESSMENT_QUERY
import com.assessment.flow.scoreboard.StudentScoreboardActivity
import com.assessment.flow.workflowengine.bolo.ReadAlongProperties
import com.data.FlowType
import com.data.db.models.entity.AssessmentState
import com.data.db.models.entity.ReferenceIds
import com.data.db.models.entity.School
import com.data.db.models.helper.AssessmentStateDetails
import com.data.db.models.helper.FlowStateStatus
import com.data.models.stateresult.AssessmentStateResult
import com.data.repository.AssessmentsRepository
import com.data.repository.CycleDetailsRepository
import com.data.repository.MetadataRepository
import com.data.repository.StudentsRepository
import com.google.gson.Gson
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.models.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AssessmentFlowVM
@Inject constructor(
    application: Application,
    private val assessmentsRepository: AssessmentsRepository,
    private val metadataRepository: MetadataRepository,
    private val cycleDetailsRepository: CycleDetailsRepository,
    private val studentsRepository: StudentsRepository
) : BaseViewModel(application = application) {

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var gson: Gson

    private var cycleId: Int? = null

    var schoolData: School? = null
    var studentId: String = ""
    var grade: Int = -1
    val backgroundWorkCompleted = MutableLiveData<Boolean>()
    private lateinit var cachedAssessmentState: AssessmentStateDetails
    private var flowStarted = false
    private var flowInterrupted = false

    init {
        viewModelScope.launch {
            cycleId = cycleDetailsRepository.getCurrentCycleId()
        }
    }

    private val mentorDetails: Result? by lazy {
        val str = prefs.getString(UserConstants.MENTOR_DETAIL, "")
        try {
            if (str.isNullOrEmpty().not()) {
                gson.fromJson(str, Result::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val stateFlow: MutableStateFlow<AssessmentFlowState> =
        MutableStateFlow(AssessmentFlowState.Loading(true))
    val eventsState = stateFlow.asStateFlow()

    var readAlongProps: ReadAlongProperties? = null

    fun initFlow() {
        // Check if got the valid parameters
        if (grade == -1 || studentId.isBlank()) {
            stateFlow.value =
                AssessmentFlowState.OnExit(
                    (getApplication() as Context).getString(
                        R.string.invalid_assessment
                    )
                )
            return
        }
        refreshFlow()
        createStates()
    }

    private fun createStates() {
        viewModelScope.launch(Dispatchers.IO) {
            val refList = getRefIdsForUser()
            if (refList.isEmpty()) {
                stateFlow.value =
                    AssessmentFlowState.OnExit(
                        (getApplication() as Context).getString(
                            R.string.no_assessments_configured
                        )
                    )
                return@launch
            }
            val assessmentStates: MutableList<AssessmentState> = mutableListOf()
            for (i in refList) {
                assessmentStates.add(
                    AssessmentState(
                        studentId = studentId,
                        competencyId = i.competencyId,
                        refIds = i.refIds,
                        flowType = if (i.type.equals("odk", true)) FlowType.ODK else FlowType.BOLO,
                        stateStatus = FlowStateStatus.PENDING
                    )
                )
            }
            assessmentsRepository.clearStates()
            Timber.i(
                "Assessments Registered : %s",
                assessmentsRepository.insertAssessmentStates(assessmentStates)
            )
        }

    }

    private fun getRefIdsForUser(): List<ReferenceIds> {
        //TODO Tech Debt Karan -> Fix this to not use hardcoded strings.
        return when (mentorDetails?.actorId) {
            //Get specific assessment types for examiners
            Constants.ACTOR_ID_EXAMINER -> {
                val competencyList = metadataRepository.getCompetencies(
                    grade = grade,
                    searchTerm = EXAMINER_ASSESSMENT_QUERY
                ).map { it.id }
                metadataRepository.getRefIdsFromCompetencyIdsWithType(
                    competencyIds = competencyList,
                    type = EXAMINER_ASSESSMENT_TYPE
                )
            }

            else -> {
                val competencyList = metadataRepository.getCompetencies(
                    grade = grade,
                    searchTerm = NON_EXAMINER_ASSESSMENT_QUERY
                ).map { it.id }
                metadataRepository.getRefIdsFromCompetencyIds(competencyList)
            }
        }
    }

    private fun observeStates() {
        // Start observing assessment states
        viewModelScope.launch {
            assessmentsRepository.observerIncompleteStates().collect {
                Timber.i("Assessments callbacks [${it.size}] : %s", Gson().toJson(it))
                if (it.isNotEmpty()) {
                    flowStarted = true
                    cachedAssessmentState = it[0]
                    stateFlow.value = AssessmentFlowState.Next(cachedAssessmentState)
                } else if (flowInterrupted) {
                    stateFlow.value = AssessmentFlowState.OnExit(
                        (getApplication() as Context).getString(
                            R.string.assessment_cancelled
                        )
                    )
                } else if (flowStarted) {
                    stateFlow.value = AssessmentFlowState.Completed
                }
            }
        }
    }

    private fun refreshFlow(isCancelledByUser: Boolean = false) {
        flowInterrupted = isCancelledByUser
        viewModelScope.launch {
            assessmentsRepository.clearStatesAsync()
            observeStates()
        }
    }

    fun markAssessmentComplete(
        state: AssessmentState,
        assessmentStateResult: AssessmentStateResult
    ) {
        state.stateStatus = FlowStateStatus.COMPLETED
        state.result = Gson().toJson(assessmentStateResult)
        viewModelScope.launch {
            assessmentsRepository.updateState(state)
        }
    }

    fun abandonFlow(
        state: AssessmentStateDetails,
        assessmentStateResult: AssessmentStateResult,
        context: Context
    ) {
        flowInterrupted = true
        state.stateStatus = FlowStateStatus.CANCELLED
        state.result = Gson().toJson(assessmentStateResult)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                assessmentsRepository.abandonFlow(state)
            }
            moveToResults(context)
        }
    }

    fun moveToResults(ctx: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val scoreCards = assessmentsRepository.getResultsForScoreCard()
            val studentName = studentsRepository.getStudentNameById(studentId)
            //TODO : Hardcoded  actor id
            assessmentsRepository.convertStatesToSubmissions(
                schoolData?.udise!!,
                cycleId = cycleId,
                mentorDetails?.actorId == Constants.ACTOR_ID_TEACHER
            )
            withContext(Dispatchers.Main) {
                val i = Intent(ctx, StudentScoreboardActivity::class.java)
                val json = Gson().toJson(scoreCards)
                i.putExtra(StudentScoreboardActivity.SCORECARD_LIST, json)
                i.putExtra(AssessmentConstants.KEY_SCHOOL_DATA, schoolData)
                i.putExtra(AssessmentConstants.KEY_STUDENT_ID, studentId)
                i.putExtra(AssessmentConstants.KEY_STUDENT_NAME, studentName)
                i.putExtra(AssessmentConstants.KEY_GRADE, grade)
                backgroundWorkCompleted.postValue(true)
                ctx.startActivity(i)
            }
        }
    }

    //FIX FOR: https://console.firebase.google.com/project/mission-prerna/crashlytics/app/android:org.samagra.missionPrerna/issues/f7ebd247b1fba7238617fd9372da330a
    fun cachedAssessmentStateDetails(): AssessmentStateDetails? {
        Timber.d("cachedAssessmentStateDetails: pull from viewmodel")
        return if (this::cachedAssessmentState.isInitialized) {
            cachedAssessmentState
        } else {
            Timber.e("cachedAssessmentStateDetails cachedAssessment is null in vm")
            null
        }
    }
}