package com.samagra.ancillaryscreens.fcm

import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.ancillaryscreens.data.model.RetrofitService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.fcm.model.UpsertTokenRequest
import com.samagra.commons.constants.Constants
import com.samagra.commons.utils.CommonConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationRepository {

    fun postFCMToken(
        token: String,
        prefs: CommonsPrefsHelperImpl,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        Timber.d("postFCMToken: $token")
        CoroutineScope(Dispatchers.IO).launch {
            val client = Network.getClient(
                ClientType.RETROFIT,
                RetrofitService::class.java,
                CommonConstants.IDENTITY_APP_SERVICE
            ) ?: return@launch

            val bearerToken = Constants.BEARER_ + prefs.authToken
            try {
                val response = client.upsertMentorToken(bearerToken, UpsertTokenRequest(token))
                Timber.d("postFCMToken: Successfully posted: %s", response.id)
                onSuccess.invoke()
            } catch (e: Exception) {
                Timber.e(e, "postFCMToken: Failed: ${e.message}")
                onFailure.invoke(e)
            }
        }
    }

}