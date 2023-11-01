package com.samagra.commons.prefs

import android.content.Context
import android.content.SharedPreferences
import com.samagra.commons.PreferenceKeys

object SystemPreferences {

    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences("sys_nl_prefs", Context.MODE_PRIVATE)
    }

    fun setForceLogoutVersion(ver: Long) {
        prefs.edit().putLong(PreferenceKeys.LAST_FORCE_LOGOUT_VERSION, ver).apply()
    }

    fun getForceLogoutVersion(): Long {
        return prefs.getLong(PreferenceKeys.LAST_FORCE_LOGOUT_VERSION, 0)
    }




    /**
     * Method to clear all preferences in @seeSystemPreferences
     * Do not clear these preferences on app logout. Only to be used for testing purpose
     **/
    fun clearLocal() {
        prefs.edit().apply {
            remove(PreferenceKeys.LAST_FORCE_LOGOUT_VERSION)
            apply()
        }
    }

}