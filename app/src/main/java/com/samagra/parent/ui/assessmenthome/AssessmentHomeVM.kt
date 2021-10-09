package com.samagra.parent.ui.assessmenthome

import android.app.Application
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.posthog.android.PostHog
import com.samagra.commons.models.Result
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.di.FormManagementCommunicator
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.basemvvm.SingleLiveEvent
import com.samagra.grove.logging.Grove
import com.samagra.parent.helper.*
import com.samagra.parent.ui.DataSyncRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.util.*

class AssessmentHomeVM(
    application: Application, private val dataSyncRepo: DataSyncRepository
) : BaseViewModel(application) {

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
    private val syncRepo = SyncRepository()

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
                    .isNotEmpty() || RealmStoreHelper.getSurveyResults().isNotEmpty()
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
        viewModelScope.launch(Dispatchers.IO) {
            progressBarVisibility.postValue(true)
            withContext(Dispatchers.Main) {
                FormManagementCommunicator.getContract().resetEverythingODK(
                    getApplication() as Context
                ) { failedResetActions ->
                    Grove.d("Failure to reset actions at Assessment Home screen $failedResetActions")
                    viewModelScope.launch(Dispatchers.IO) {
                        clearAllUserData(prefs)
                        delay(500)
                        progressBarVisibility.postValue(false)
                        gotoLogin.postValue(Unit)
                    }
                }
            }
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
                isSuccess = helper.syncSurveys(prefs) && isSuccess
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
                getMentorDetailsFromPrefs(prefs)
                getOverviewDataFormPrefs(prefs)
                progressBarVisibility.postValue(false)
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
}