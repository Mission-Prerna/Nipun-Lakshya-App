package com.samagra.parent.helper

import com.data.db.models.AppAction
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.ancillaryscreens.data.model.RetrofitService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.parent.ui.getBearerAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

object AppActionsHelper {

    private val apiService by lazy { generateApiService() }

    sealed class AppActionsState {
        object OnFailure: AppActionsState()
        class OnDataReceived(val response : List<AppAction>?) : AppActionsState()
    }

    suspend fun fetchAppActions(prefs: CommonsPrefsHelperImpl, timestamp: Long): StateFlow<AppActionsState?> {
        val remoteResponseStatus: MutableStateFlow<AppActionsState?> = MutableStateFlow(null)

        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            Timber.d("fetchAppActions: no network")
            remoteResponseStatus.emit(AppActionsState.OnFailure)
        }

        try {
            val responseAppActions = CoroutineScope(Dispatchers.IO).async {
                apiService?.fetchAppActions(apiKey = prefs.getBearerAuthToken(), timestamp = timestamp)
            }
            val appActionsData: List<AppAction>? = responseAppActions.await()
            appActionsData?.let {
                Timber.d("fetchAppActionsData: response got")
                remoteResponseStatus.emit(AppActionsState.OnDataReceived(it))
            }
        } catch (t: Exception) {
            Timber.e(t, "fetchAppActionsData error: %s", t.message)
            remoteResponseStatus.emit(AppActionsState.OnFailure)
        }
        return remoteResponseStatus
    }

    private fun generateApiService(): RetrofitService? {
        return Network.getClient(
            ClientType.RETROFIT,
            RetrofitService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )
    }

}