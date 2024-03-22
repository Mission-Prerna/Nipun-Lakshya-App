package com.samagra.parent.ui.userselection

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.basemvvm.SingleLiveEvent
import com.samagra.parent.authentication.AuthenticationActivity
import com.samagra.parent.helper.MetaDataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class UserSelectionVM(
    application: Application,
    private val userSelectionRepo: UserSelectionRepository
) : BaseViewModel(application) {
    val flowDecisionLiveData =
        SingleLiveEvent<Pair<AuthenticationActivity.RedirectionFlow, String>>()

    fun fetchData(
        prefs: CommonsPrefsHelperImpl,
        flow: Pair<AuthenticationActivity.RedirectionFlow, String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            downLoadWorkflowConfig()
            downloadOdkFormLength(prefs)
            fetchMetaData(prefs, flow)
        }
    }

    private suspend fun fetchMetaData(
        prefs: CommonsPrefsHelperImpl,
        flow: Pair<AuthenticationActivity.RedirectionFlow, String>
    ) {
        MetaDataHelper.fetchMetaData(prefs).collect {
            Timber.d("fetchMetaData: $it")
            flowDecisionLiveData.postValue(flow)
        }
    }

    private fun downloadOdkFormLength(prefs: CommonsPrefsHelperImpl) {
        userSelectionRepo.downloadFormsLength(prefs)
    }

    private fun downLoadWorkflowConfig() {
        userSelectionRepo.downloadWorkFlowConfigFromRemoteConfig()
    }
}
