package com.samagra.parent.ui.assessmenthome

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.data.db.DbHelper
import com.data.db.NLDatabase
import com.data.db.models.AppAction
import com.data.db.models.MentorPerformanceInsightsItem
import com.data.db.models.entity.CycleDetails
import com.data.repository.AppActionsRepository
import com.data.repository.CycleDetailsRepository
import com.data.repository.MetadataRepository
import com.data.repository.SchoolsRepository
import com.data.repository.StudentsRepository
import com.posthog.android.PostHog
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.basemvvm.SingleLiveEvent
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.Result
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.DASHBOARD_SCREEN
import com.samagra.commons.posthog.EVENT_HOME_SCREEN_APP_ACTION_COMPLETE
import com.samagra.commons.posthog.EVENT_HOME_SCREEN_APP_ACTION_FAILED
import com.samagra.commons.posthog.NL_APP_DASHBOARD
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.helper.AppActionsHelper
import com.samagra.parent.helper.MentorDataHelper
import com.samagra.parent.helper.MetaDataHelper
import com.samagra.parent.helper.RealmStoreHelper
import com.samagra.parent.helper.SyncRepository
import com.samagra.parent.helper.SyncingHelper
import com.samagra.parent.repository.ExaminerPerformanceInsightsRepository
import com.samagra.parent.repository.MentorPerformanceInsightsRepository
import com.samagra.parent.repository.TeacherPerformanceInsightsRepository
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.assessmenthome.states.ExaminerInsightsStates
import com.samagra.parent.ui.assessmenthome.states.MentorInsightsStates
import com.samagra.parent.ui.assessmenthome.states.TeacherInsightsStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import javax.inject.Inject


private const val USER_EXAMINER = 2
private const val ACTION_DOMAIN_DB = "db"
private const val ACTION_DOMAIN_SYSTEM = "system"

@HiltViewModel
class AssessmentHomeVM @Inject constructor(
    application: Application,
    private val dataSyncRepo: DataSyncRepository,
    private val teacherPerformanceInsightsRepository: TeacherPerformanceInsightsRepository,
    private val examinerPerformanceInsightsRepository: ExaminerPerformanceInsightsRepository,
    private val mentorPerformanceInsightsRepository: MentorPerformanceInsightsRepository,
    private val nlDatabase: NLDatabase?
) : BaseViewModel(application) {

    private val firebaseInstance by lazy { RemoteConfigUtils.getFirebaseRemoteConfigInstance() }

    private val prefs: CommonsPrefsHelperImpl by lazy { initPreferences() }

    @Inject
    lateinit var metaRepo: MetadataRepository

    @Inject
    lateinit var cycleDetailsRepository: CycleDetailsRepository

    @Inject
    lateinit var studentsRepository: StudentsRepository

    @Inject
    lateinit var schoolsRepository: SchoolsRepository

    @Inject
    lateinit var appActionsRepository: AppActionsRepository

    val gotoLogin = MutableLiveData<Unit>()
    val logoutUserLiveData = MutableLiveData<Unit>()
    val showSyncBeforeLogout = MutableLiveData<Unit>()
    val nameValue = ObservableField("")
    val designation = ObservableField("")
    val udise = MutableLiveData("")
    val updateSync = MutableLiveData<Int>()
    val phoneNumberValue = ObservableField("")
    val mentorDetailsSuccess = MutableLiveData<Result>()
    val mentorOverViewData = MutableLiveData<HomeOverviewData>()
    val setupNewAssessmentClicked = SingleLiveEvent<Unit>()
    val helpFaqList = SingleLiveEvent<String>()
    val helpFaqFormUrl = SingleLiveEvent<String>()
    val syncRequiredLiveData = MutableLiveData<Boolean>()
    private val syncRepo = SyncRepository()

    private val teacherInsightsMutableState: MutableStateFlow<TeacherInsightsStates> =
        MutableStateFlow(TeacherInsightsStates.Loading)

    val teacherInsightsState: StateFlow<TeacherInsightsStates> =
        teacherInsightsMutableState.asStateFlow()

    private val examinerInsightsMutableState: MutableStateFlow<ExaminerInsightsStates> =
        MutableStateFlow(ExaminerInsightsStates.Loading)

    val examinerInsightsState: StateFlow<ExaminerInsightsStates> =
        examinerInsightsMutableState.asStateFlow()

    private val mentorInsightsMutableState: MutableStateFlow<MentorInsightsStates> =
        MutableStateFlow(MentorInsightsStates.Loading)

    val mentorInsightsState: StateFlow<MentorInsightsStates> =
        mentorInsightsMutableState.asStateFlow()

    private fun initPreferences() = CommonsPrefsHelperImpl(getApplication(), "prefs")

    fun fetchTeacherPerformanceInsights(udise: String) {
        viewModelScope.launch {
            try {
                teacherInsightsMutableState.value = TeacherInsightsStates.Loading
                val teacherPerformanceInsightsResult =
                    teacherPerformanceInsightsRepository.fetchTeacherPerformanceInsights(udise)
                when (teacherPerformanceInsightsResult) {
                    is com.data.network.Result.Success -> {
                        teacherInsightsMutableState.value =
                            TeacherInsightsStates.Success(teacherPerformanceInsightsResult.data!!)
                    }

                    else -> {
                        teacherInsightsMutableState.value =
                            TeacherInsightsStates.Error(Exception("An error occurred"))
                    }
                }
            } catch (t: Throwable) {
                teacherInsightsMutableState.value = TeacherInsightsStates.Error(t)
            }
        }
    }

    fun getTeacherPerformanceInsights() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                teacherPerformanceInsightsRepository.getTeacherPerformanceInsights()
                    .collect { insights ->
                        if (insights.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                teacherInsightsMutableState.value =
                                    TeacherInsightsStates.Success(insights)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                teacherInsightsMutableState.value =
                                    TeacherInsightsStates.Error(Exception(""))
                                Timber.i("No data in db")
                            }
                        }
                    }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    teacherInsightsMutableState.value = TeacherInsightsStates.Error(t)
                }
            }
        }
    }

    fun fetchExaminerPerformanceInsights() {
        viewModelScope.launch {
            try {
                examinerInsightsMutableState.value = ExaminerInsightsStates.Loading
                val examinerPerformanceInsightsResult =
                    examinerPerformanceInsightsRepository.fetchExaminerPerformanceInsights()
                when (examinerPerformanceInsightsResult) {
                    is com.data.network.Result.Success -> {
                        examinerInsightsMutableState.value =
                            ExaminerInsightsStates.Success(examinerPerformanceInsightsResult.data!!)
                    }

                    else -> {
                        examinerInsightsMutableState.value =
                            ExaminerInsightsStates.Error(Exception("An error occurred"))
                    }
                }
            } catch (t: Throwable) {
                examinerInsightsMutableState.value = ExaminerInsightsStates.Error(t)
            }
        }
    }

    fun getExaminerPerformanceInsights() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                examinerPerformanceInsightsRepository.getExaminerPerformanceInsights()
                    .collect { insights ->
                        if (insights.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                examinerInsightsMutableState.value =
                                    ExaminerInsightsStates.Success(insights)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                examinerInsightsMutableState.value =
                                    ExaminerInsightsStates.Error(Exception(""))
                                Timber.i("No data in db")
                            }
                        }
                    }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    examinerInsightsMutableState.value = ExaminerInsightsStates.Error(t)
                }
            }
        }
    }

    fun fetchMentorPerformanceInsights() {
        viewModelScope.launch {
            try {
                mentorInsightsMutableState.value = MentorInsightsStates.Loading
                val mentorPerformanceInsightsResult =
                    mentorPerformanceInsightsRepository.fetchMentorPerformanceInsights()
                when (mentorPerformanceInsightsResult) {
                    is com.data.network.Result.Success -> {
                        mentorInsightsMutableState.value =
                            MentorInsightsStates.Success(mentorPerformanceInsightsResult.data!!)
                    }

                    else -> {
                        mentorInsightsMutableState.value =
                            MentorInsightsStates.Error(Exception("An error occurred"))
                    }
                }
            } catch (t: Throwable) {
                mentorInsightsMutableState.value = MentorInsightsStates.Error(t)
            }
        }
    }

    fun getMentorPerformanceInsights() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mentorPerformanceInsightsRepository.getMentorPerformanceInsights()
                    .collect { insights ->
                        if (insights!=null) {
                            withContext(Dispatchers.Main) {
                                mentorInsightsMutableState.value =
                                    MentorInsightsStates.Success(insights)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                mentorInsightsMutableState.value =
                                    MentorInsightsStates.Error(Exception(""))
                                Timber.i("No data in db")
                            }
                        }
                    }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    mentorInsightsMutableState.value = MentorInsightsStates.Error(t)
                }
            }
        }
    }

    fun onSetupNewAssessmentClicked() {
        setupNewAssessmentClicked.call()
    }

    private fun downLoadWorkflowConfig() {
        dataSyncRepo.downloadWorkFlowConfigFromRemoteConfig()
    }

    private fun downloadOdkFormLength(prefs: CommonsPrefsHelperImpl) {
        dataSyncRepo.downloadFormsLength(prefs)
    }

    fun downloadDataFromRemoteConfig(prefs: CommonsPrefsHelperImpl, internetAvailable: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            downLoadWorkflowConfig()
            downloadOdkFormLength(prefs)
        }
    }

    fun onLogoutClicked() {
        CoroutineScope(Dispatchers.IO).launch {

            if (RealmStoreHelper.getFinalResults()
                    .isNotEmpty() || RealmStoreHelper.getSurveyResults()
                    .isNotEmpty() || DbHelper.isSyncingRequired()
            ) {
                showSyncBeforeLogout.postValue(Unit)
            } else {
                logoutUserLiveData.postValue(Unit)
            }
        }
    }

    fun onLogoutUserData(
        prefs: CommonsPrefsHelperImpl
    ) {
        progressBarVisibility.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            clearAllUserData(prefs)
            delay(500)
            progressBarVisibility.postValue(false)
            gotoLogin.postValue(Unit)
        }
    }

    private suspend fun clearRealmTables() {
        val isSuccess = withContext(Dispatchers.IO) {
            RealmStoreHelper.clearAllTables()
        }
        Timber.d("clearRealmTables: ")
    }

    private suspend fun clearAllUserData(prefs: CommonsPrefsHelperImpl) {
        prefs.clearData()
        clearRealmTables()
        nlDatabase?.clearAllTables()
        PostHog.with(getApplication()).reset()
    }

    fun getHelpFaqList() {
        helpFaqList.value = dataSyncRepo.getHelpFaqListFromFirebase()
    }

    fun getHelpFaqFormUrl() {
        helpFaqFormUrl.value = dataSyncRepo.getHelpFaqFormUrlFromFirebase()
    }

    fun syncDataToServer(
        prefs: CommonsPrefsHelperImpl, success: () -> Unit, failure: () -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                progressBarVisibility.postValue(true)
                Timber.i("In Progress @ " + Date())
                val helper = SyncingHelper()
                var isSuccess = helper.syncAssessments(prefs)
                isSuccess = helper.syncSubmissions(prefs) && isSuccess
                isSuccess = helper.syncSurveys(prefs) && isSuccess
                isSuccess = helper.syncSchoolSubmission(prefs) && isSuccess
                Timber.i("IsSuccess : $isSuccess")
                withContext(Dispatchers.Main) {
                    if (isSuccess) success() else failure()
                    progressBarVisibility.postValue(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun syncDataFromServer(prefs: CommonsPrefsHelperImpl, enforce: Boolean = false) {
        Timber.d("syncDataFromServer: ")
        viewModelScope.launch(Dispatchers.IO) {
            progressBarVisibility.postValue(true)
            MentorDataHelper.fetchMentorData(enforce, prefs).collect {
                Timber.d("syncDataFromServer: collect $it")
                if (it == null) return@collect
                if (it is MentorDataHelper.MetaDataState.OnDataReceived) {
                    it.response.mentor?.let { mentor ->
                        if (mentor.actorId == USER_EXAMINER) {
                            storeExaminerCycleDetailsInDb(it.response.cycleDetails)
                        }
                        schoolsRepository.insertSchools(it.response.schoolList)
                    }
                }
                getMentorDetailsFromPrefs(prefs)
                if (prefs.selectedUser.equals(Constants.USER_TEACHER).not()) {
                    getOverviewDataFormPrefs(prefs)
                }
                progressBarVisibility.postValue(false)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = (appActionsRepository.getLatestTime()?.plus(1L)) ?: prefs.prefCreationTime

            AppActionsHelper.fetchAppActions(prefs, timestamp).collect{
                if (it == null) return@collect
                if (it is AppActionsHelper.AppActionsState.OnDataReceived) {
                    it.response?.let {actions ->
                        parseAppActionsData(actions)
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            MetaDataHelper.fetchMetaData(
                prefs = prefs,
                enforce = enforce
            ).collect {
                Timber.d("syncDataFromServer metadata collect: $it")
            }
        }
    }

    private suspend fun parseAppActionsData(appActions: List<AppAction>) {
         appActionsRepository.insertAction(appActions)
         checkPermissionAndExecuteQuery()
    }

    private suspend fun checkPermissionAndExecuteQuery() {
        val actionsToPerform = appActionsRepository.getAllActions()

        for (action in actionsToPerform) {
            if (action.domain == ACTION_DOMAIN_DB) {
                if (isActionAllowed(action)) {
                    try {
                        nlDatabase?.runInTransaction {
                            nlDatabase.openHelper.writableDatabase.execSQL(action.action)
                        }
                        appActionsRepository.markActionCompleted(action.id)
                        sendAppActionEvent(isComplete = true)
                    } catch (e: Exception) {
                        Timber.e(e, "Exception during SQL query")
                        sendAppActionEvent(isComplete = false)
                    }
                }
        } else if (action.domain == ACTION_DOMAIN_SYSTEM){
            if (isActionAllowed(action)){
                try {
                    sendAppActionEvent(isComplete = true)
                    clearAppData()
                } catch (e: Exception){
                    sendAppActionEvent(isComplete = false)
                    Timber.e(e,"Exception during clearing data")
                }
            }
          }
        }
    }

    fun insertDummyStats(dummyStats: MentorPerformanceInsightsItem){
        CoroutineScope(Dispatchers.IO).launch {
            mentorPerformanceInsightsRepository.insert(dummyStats)
        }
    }

    private fun clearAppData() {
        (getApplication<Application>().getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
    }
    private fun sendAppActionEvent(isComplete: Boolean) {
        val list = ArrayList<Cdata>()
        val mentorDetailsFromPrefs = prefs.mentorDetailsData
        mentorDetailsFromPrefs?.let {
            list.add(Cdata("userId", "" + it.id))
            list.add(Cdata("userType", "" + it.actorId))
        }
        val properties = PostHogManager.createProperties(
            page = DASHBOARD_SCREEN,
            eventType = null,
            eid = null,
            context = PostHogManager.createContext(APP_ID, NL_APP_DASHBOARD, list),
            eData = null,
            objectData = null,
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
        )

        if (isComplete)
            PostHogManager.capture(getApplication(), EVENT_HOME_SCREEN_APP_ACTION_COMPLETE, properties)
        else
            PostHogManager.capture(getApplication(), EVENT_HOME_SCREEN_APP_ACTION_FAILED, properties)
    }

    private suspend fun isActionAllowed(action: AppAction): Boolean{
        val allowedActionsTypes = firebaseInstance.getString(RemoteConfigUtils.APP_ACTIONS_ALLOWED)
        val listOfAllowedActionTypes = getListFromString(allowedActionsTypes)

        for (allowedActionType in listOfAllowedActionTypes){
            val queryAction = action.action.split("\\s+".toRegex())[0].lowercase()
            if (queryAction == allowedActionType.lowercase() && action.isCompleted.not()) {
                if (action.requested_at > getPrefCreationTime())
                    return true
                else
                    appActionsRepository.markActionCompleted(action.id)
            }
        }
        return false
    }

    private fun getListFromString(actions: String): List<String> {
        return actions.removeSurrounding("[", "]").split(",").map { it.trim() }
    }

    private fun getPrefCreationTime() = prefs.prefCreationTime
    fun examinerPerformanceInsights(context: Context) {
        viewModelScope.launch {
            if (checkIfCycleDetailsValid().not()) {
                examinerInsightsMutableState.emit(
                    ExaminerInsightsStates.Error(
                        error = IllegalStateException(
                            context.getString(R.string.invalid_cycle)
                        ),
                        ctaEnable = false
                    )
                )
                return@launch
            }
            if (UtilityFunctions.isNetworkConnected(context)) {
                getExaminerPerformanceInsights()
                fetchExaminerPerformanceInsights()
            } else {
                getExaminerPerformanceInsights()
            }
        }
    }

    private suspend fun storeExaminerCycleDetailsInDb(cycleDetails: CycleDetails?) {
        if (cycleDetails != null) {
            CoroutineScope(Dispatchers.IO).launch {
                cycleDetailsRepository.insertExaminerCycleDetails(cycleDetails)
            }
        }
    }

    private suspend fun checkIfCycleDetailsValid() =
        cycleDetailsRepository.getCurrentCycleDetails() != null

    private fun getMentorDetailsFromPrefs(prefs: CommonsPrefsHelperImpl) {
        val mentorDetails = prefs.mentorDetailsData
        mentorDetails?.let {
            mentorDetailsSuccess.postValue(it)
            nameValue.set(it.officer_name)
            phoneNumberValue.set(it.phone_no)
            val designation = MetaDataExtensions.getDesignationFromId(
                it.designation_id, prefs.designationsListJson
            )
            this.designation.set(designation)
            this.udise.postValue(it.udise.toString())
        }
    }

    private suspend fun getOverviewDataFormPrefs(prefs: CommonsPrefsHelperImpl) {
        val overviewDataFromPrefs =
            MentorDataHelper.getOverviewDataFromPrefs(prefs.mentorOverviewDetails)
        overviewDataFromPrefs?.let { overview ->
            val finalResultsRealm = RealmStoreHelper.getFinalResults()
            val homeOverviewData = if (finalResultsRealm.isNotEmpty()) {
                MentorDataHelper.setOverviewCalculations(finalResultsRealm, overview)
            } else {
                overview
            }
            mentorOverViewData.postValue(homeOverviewData)
        }
    }

    fun checkForFallback(prefs: CommonsPrefsHelperImpl) {
        syncRepo.syncToServer(prefs) {
            // Handle Loader if required
        }
    }

    fun updateOfflineData(schoolUdise: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (schoolUdise == null) return@launch
            val areCompetenciesAvailable = metaRepo.areCompetenciesAvailable()
            syncRequiredLiveData.postValue(areCompetenciesAvailable.not())
            studentsRepository.addDummyStudents(schoolUdise = schoolUdise)
        }
    }
}