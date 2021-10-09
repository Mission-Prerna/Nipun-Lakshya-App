package com.samagra.parent.helper

import com.google.gson.Gson
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import com.samagra.parent.data.models.SyncFallbackConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class SyncRepository {

    fun syncToServer(
        prefs: CommonsPrefsHelperImpl,
        showProgress: (Boolean) -> Unit
    ) {
        val lastSyncedAt = prefs.lastSyncedAt
        val syncFallbackConfig = Gson().fromJson(
            getFirebaseRemoteConfigInstance()
                .getString(RemoteConfigUtils.SYNC_FALLBACK_CONFIG), SyncFallbackConfig::class.java
        )
        val threshHoldDate =
            Date(Date().time - (syncFallbackConfig.thresholdPeriod * 60 * 60 * 1000).toLong())
        Timber
            .i("lastSyncedAt : $lastSyncedAt -- threshHoldDate : $threshHoldDate")
        if (lastSyncedAt > threshHoldDate) {
            return
        }
        try {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    showProgress(true)
                }
                Timber.i("In Progress @ " + Date())
                val helper = SyncingHelper()
                var isSuccess = helper.syncAssessments(prefs)
                isSuccess = helper.syncSurveys(prefs) && isSuccess
                Timber.i("IsSuccess : $isSuccess")
                prefs.markDataSynced()
                withContext(Dispatchers.Main) {
                    showProgress(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}