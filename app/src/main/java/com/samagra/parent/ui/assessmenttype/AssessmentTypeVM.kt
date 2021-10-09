package com.samagra.parent.ui.assessmenttype

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.di.FormManagementCommunicator
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.grove.logging.Grove
import com.samagra.parent.helper.*
import com.samagra.parent.ui.DataSyncRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber

class AssessmentTypeVM(application: Application, private val dataSyncRepo: DataSyncRepository) :
    BaseViewModel(application) {
    val showSyncBeforeLogout = MutableLiveData<Unit>()
    val logoutUserLiveData = MutableLiveData<Unit>()
    val gotoLogin = MutableLiveData<Unit>()
    val showDialogLogoutWithNoInternet = MutableLiveData<Int>()
    private val syncRepo = SyncRepository()

    fun onLogoutClicked() {
        CoroutineScope(Dispatchers.IO).launch {
            if (RealmStoreHelper.getFinalResults()
                    .isNotEmpty() || RealmStoreHelper.getSurveyResults()
                    .isNotEmpty()
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
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            progressBarVisibility.postValue(true)
            withContext(Dispatchers.Main) {
                FormManagementCommunicator.getContract()
                    .resetEverythingODK(
                        getApplication() as Context
                    ) { failedResetActions ->
                        Grove.d("Failure to reset actions at Assessment Home screen $failedResetActions")
                        CoroutineScope(Job()).launch {
                            clearAllUserData(prefs)
                            delay(1000)
                            progressBarVisibility.postValue(false)
                            gotoLogin.postValue(Unit)
                        }
                    }
            }
        }
    }

    fun syncDataToServer(
        prefs: CommonsPrefsHelperImpl, success: () -> Unit, failure: () -> Unit
    ) {
        progressBarVisibility.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.d("syncPendingData: ")
                val helper = SyncingHelper()
                var isSuccess = helper.syncAssessments(prefs)
                Timber.d("syncPendingData: synced assessments: $isSuccess")
                isSuccess = helper.syncSurveys(prefs) && isSuccess
                Timber.d("syncPendingData: syncSurveys : $isSuccess")
                withContext(Dispatchers.Main) {
                    if (isSuccess) success() else failure()
                    progressBarVisibility.postValue(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                failure()
            }
        }
    }

    private suspend fun clearAllUserData(prefs: CommonsPrefsHelperImpl) {
        prefs.clearData()
        clearRealmTables()
    }

    private suspend fun clearRealmTables() {
        val isSuccess = withContext(Dispatchers.IO) {
            RealmStoreHelper.clearAllTables()
        }
        Timber.i("clearRealmTables isSuccess : $isSuccess")
    }

    fun syncDataFromServer(prefs: CommonsPrefsHelperImpl) {
        Timber.d("syncAppData: ")
        viewModelScope.launch(Dispatchers.IO) {
            progressBarVisibility.postValue(true)
            //todo remove viewModel and related code
            /*MentorDataHelper.fetchMentorData(false,prefs).collect {
                Timber.d("syncAppData: collect $it")
                if (it == null) return@collect
                progressBarVisibility.postValue(false)
            }*/
        }
        viewModelScope.launch(Dispatchers.IO) {
            /*MetaDataHelper.fetchMetaData(prefs).collect {
                Timber.d("syncAppData metadata collect: it")
            }*/
        }
    }

    fun checkForFallback(prefs: CommonsPrefsHelperImpl) {
        syncRepo.syncToServer(prefs, {
            // Handle Loader if required
        })
    }
}