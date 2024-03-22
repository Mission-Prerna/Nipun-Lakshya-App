package com.samagra.commons.utils

import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.samagra.commons.CommonUtilities
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.utils.NetworkStateManager.Companion.instance

object CustomEventCrashUtil {

    @JvmStatic
    fun setCrashPropsLoggedInUser(
        selectedUser: String, mentorId: String, designationId: Int, actorId: Int
    ) {
        val keysAndValues = instance?.networkConnectivityStatus?.let {
            CustomKeysAndValues.Builder().putString(UserConstants.SELECTED_USER, selectedUser)
                .putString(UserConstants.USER_ID, mentorId)
                .putInt(UserConstants.DESIGNATION_ID, designationId)
                .putInt(UserConstants.ACTOR_ID, actorId)
                .putBoolean(UserConstants.NETWORK_STATUS, it).build()
        }
        keysAndValues?.let { FirebaseCrashlytics.getInstance().setCustomKeys(it) }
    }

    @JvmStatic
    fun setCrashPropsAsGuest(selectedUser: String) {
        val keysAndValues = instance?.networkConnectivityStatus?.let {
            CustomKeysAndValues.Builder().putString(UserConstants.SELECTED_USER, selectedUser)
                .putBoolean(UserConstants.NETWORK_STATUS, it).build()
        }
        keysAndValues?.let { FirebaseCrashlytics.getInstance().setCustomKeys(it) }
    }

    @JvmStatic
    fun setSelectedUserProperty(selectedUser: String) {
        if (selectedUser != "") {
            CommonUtilities.setCrashlyticsProperty(selectedUser)
            setCrashPropsAsGuest(selectedUser)
        } else {
            CommonUtilities.setCrashlyticsProperty(UserConstants.USER_GUEST)
            setCrashPropsAsGuest(UserConstants.USER_GUEST)
        }
    }

}