package com.samagra.commons.helper

import androidx.appcompat.app.AppCompatActivity
import com.samagra.commons.BuildConfig
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.gatekeeper.Gatekeeper

object GatekeeperHelper {

    fun assess(
        context: AppCompatActivity,
        actor: String = "",
        skipWarning: Boolean = false,
        singleError: Boolean = true
    ) {
        val firebaseRemoteConfig = RemoteConfigUtils.getFirebaseRemoteConfigInstance()
        val baseUrl = if (BuildConfig.DEBUG)
            "http://128.199.28.17:8065/"
        else
            firebaseRemoteConfig.getString(RemoteConfigUtils.GATEKEEPER_BASE_URL)
        val apiKey = if (BuildConfig.DEBUG)
            "Asdf@1234"
        else
            firebaseRemoteConfig.getString(RemoteConfigUtils.GATEKEEPER_API_KEY)
        Gatekeeper.assess(
            fragmentManager = context.supportFragmentManager,
            baseUrl = baseUrl,
            applicationId = context.packageName,
            apiKey = apiKey,
            actor = actor,
            skipWarning = skipWarning,
            singleError = singleError
        )
    }
}