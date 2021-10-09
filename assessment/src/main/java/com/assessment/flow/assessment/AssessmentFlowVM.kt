package com.assessment.flow.assessment

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import com.assessment.R
import com.assessment.flow.AssessmentConstants.EXAMINER_ASSESSMENT_QUERY
import com.assessment.flow.AssessmentConstants.EXAMINER_ASSESSMENT_TYPE
import com.assessment.flow.AssessmentConstants.NON_EXAMINER_ASSESSMENT_QUERY
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ACTIVITY_FOR_RESULT
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ASSESSMENT_RESULT
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ODK_INSTRUCTION_ACTIVITY
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.READ_ALONG_INSTRUCTION_ACTIVITY
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.flow.workflowengine.bolo.ReadAlongProperties
import com.assessment.flow.workflowengine.odk.OdkProperties
import com.data.FlowType
import com.data.db.models.entity.AssessmentState
import com.data.db.models.entity.ReferenceIds
import com.data.db.models.entity.School
import com.data.db.models.helper.AssessmentStateDetails
import com.data.db.models.helper.FlowStateStatus
import com.data.models.stateresult.AssessmentStateResult
import com.data.models.stateresult.ModuleResult
import com.data.models.ui.ScorecardData
import com.data.repository.AssessmentsRepository
import com.data.repository.CycleDetailsRepository
import com.data.repository.MetadataRepository
import com.data.repository.StudentsRepository
import com.google.gson.Gson
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.models.Result
import com.samagra.commons.utils.CommonConstants.BOLO
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.NetworkStateManager.Companion.instance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
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
    private lateinit var cachedAssessmentState: AssessmentStateDetails
    private var flowRunning = false
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
                Timber.i("Assessments callbacks [${it.size}] : %s", gson.toJson(it))
                if (it.isNotEmpty()) {       // we get empty states as well when assessment has not yet started or ended, to manage multiple scenarios correctly we have done this.
                    flowRunning = true
                    cachedAssessmentState = it[0]
                    stateFlow.value = AssessmentFlowState.Next(cachedAssessmentState)
                } else if (flowInterrupted) {
                    stateFlow.value = AssessmentFlowState.OnExit(
                        (getApplication() as Context).getString(
                            R.string.assessment_cancelled
                        )
                    )
                } else if (flowRunning) {
                    flowRunning = false
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
        state.result = gson.toJson(assessmentStateResult)
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
        state.result = gson.toJson(assessmentStateResult)
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
            val currentUser = prefs.getString(UserConstants.SELECTED_USER, "")
            if (currentUser!=Constants.USER_PARENT) {
                assessmentsRepository.convertStatesToSubmissions(
                    schoolData?.udise!!,
                    cycleId = cycleId,
                    mentorDetails?.actorId == Constants.ACTOR_ID_TEACHER
                )
            }
            withContext(Dispatchers.Main) {
                stateFlow.value = AssessmentFlowState.OnLaunchScoreboard(
                    schoolData,
                    studentName,
                    studentId,
                    grade,
                    scoreCards
                )
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

    fun processAssessmentResult(
        result: ActivityResult,
        odkProps: OdkProperties?,
        currentState: AssessmentStateDetails?,
        ctx: Context
    ) {
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
            val data = result.data
            var assessmentStateResult = data?.getSerializableExtra(ASSESSMENT_RESULT) as AssessmentStateResult?
            val activity = data?.getStringExtra(ACTIVITY_FOR_RESULT)

            if (assessmentStateResult != null) {
                onAssessmentResultNotNull(activity, assessmentStateResult, odkProps)
            } else {
                assessmentStateResult = AssessmentStateResult()
                assessmentStateResult.studentId = studentId
                onAssessmentResultNull(activity, assessmentStateResult, odkProps)
            }

            val currentState = currentState ?: cachedAssessmentStateDetails()
            ?: return
            if (result.resultCode == Activity.RESULT_OK) {
                markAssessmentComplete(
                    currentState.getAsAssessmentState(),
                    assessmentStateResult
                )
            } else if (result.resultCode == RESULT_CANCELED) {
                stateFlow.value = AssessmentFlowState.OnExit(ctx.getString(R.string.assessment_cancelled))
            }
        } else {
            return
        }
    }

    private fun onAssessmentResultNotNull(activity:String?, assessmentStateResult: AssessmentStateResult, odkProps: OdkProperties?){
        if (activity == ODK_INSTRUCTION_ACTIVITY) {   // setting result from odk props when not null
            assessmentStateResult.apply {
                grade = odkProps?.grade
                workflowRefId = odkProps?.formID
                subject = odkProps?.subject
            }
        } else if (activity == READ_ALONG_INSTRUCTION_ACTIVITY) {  // setting result from read along props when not null
            if (readAlongProps == null) {
                return
            }
            assessmentStateResult.apply {
                moduleResult.stateGrade = readAlongProps?.stateGrade
                grade = readAlongProps?.grade
                subject = readAlongProps?.subject
            }
        }
    }

    private fun onAssessmentResultNull(
        activity: String?,
        assessmentStateResult: AssessmentStateResult,
        odkProps: OdkProperties?
    ) {
        if (activity == ODK_INSTRUCTION_ACTIVITY) {
            setDefaultOdkProps(assessmentStateResult, odkProps) // set to default in case of null
        } else if (activity == READ_ALONG_INSTRUCTION_ACTIVITY) {
            setDefaultReadAlongProps(assessmentStateResult)     // set to default in case of null
        }
    }

    private fun setDefaultOdkProps(assessmentStateResult: AssessmentStateResult, odkProps: OdkProperties?){
        assessmentStateResult.apply {
            workflowRefId = odkProps?.formID
            grade = odkProps?.grade
            subject = odkProps?.subject
        }
        val moduleResult = ModuleResult()
        moduleResult.apply {
            isNetworkActive = instance?.networkConnectivityStatus == true
            module = ODK
            achievement = 0
            isPassed = false
            sessionCompleted = false
            appVersionCode = UtilityFunctions.getVersionCode()
            totalQuestions = 0
            successCriteria = 0
        }
        assessmentStateResult.moduleResult = moduleResult.apply {
            startTime = Date().time
            endTime = Date().time
        }
    }

    private fun setDefaultReadAlongProps(assessmentStateResult: AssessmentStateResult){
        assessmentStateResult.apply {
            workflowRefId = "0"
            grade = readAlongProps?.grade
            subject = readAlongProps?.subject
        }
        val moduleResult = ModuleResult()
        moduleResult.apply {
            module = BOLO
            isNetworkActive = instance?.networkConnectivityStatus == true
            achievement = 0
            isPassed = false
            sessionCompleted = false
            appVersionCode = UtilityFunctions.getVersionCode()
            totalQuestions = 0
            successCriteria = 0
        }
        assessmentStateResult.moduleResult = moduleResult.apply {
            startTime = Date().time
            endTime = Date().time
        }
    }

}
sealed class AssessmentFlowState {
    class Loading(val enabled : Boolean) : AssessmentFlowState()

    class Error(val t: Throwable) : AssessmentFlowState()

    object Completed : AssessmentFlowState()

    class Next(val state: AssessmentStateDetails) : AssessmentFlowState()

    class OnExit(val reason : String) : AssessmentFlowState()

    class OnLaunchScoreboard(val schoolData: School?, val studentName: String, val studentId: String, val grade: Int, val scorecards: List<ScorecardData>): AssessmentFlowState()
}