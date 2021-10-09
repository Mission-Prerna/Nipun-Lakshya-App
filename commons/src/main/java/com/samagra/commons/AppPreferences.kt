package com.samagra.commons

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.models.Result

object AppPreferences {

    private lateinit var prefs: SharedPreferences

    private val gson by lazy { Gson() }

    private var savedMentor: Result? = null

    fun init(ctx: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    var chatBotList: String
        get() = prefs.getString(PreferenceKeys.CHAT_BOT_LIST, "[]") ?: "[]"
        set(botListStr) {
            prefs.edit().putString(PreferenceKeys.CHAT_BOT_LIST, botListStr).apply()
        }

    fun getStarredMsgs(): String {
        return prefs.getString(PreferenceKeys.CHAT_BOT_STARRED_MSGS, "{}")!!
    }

    fun saveStarredMessages(savedMsg: String) {
        prefs.edit()
                .putString(PreferenceKeys.CHAT_BOT_STARRED_MSGS, savedMsg)
                .apply()
    }

    fun getUser(): Result? {
        val savedMentorString = prefs.getString("mentorDetail", "")
        return try {
            savedMentor = gson.fromJson(savedMentorString, Result::class.java)
            savedMentor
        } catch (t: Throwable) {
            null
        }
    }

    fun getUserAuth() = prefs.getString("auth_token_jwt", "") ?: ""
    fun getUserMobile() = getUser()?.phone_no ?: ""

    fun getUserId() = try {
        getUser()?.id ?: ""
    } catch (e: Exception) {
        ""
    }

    fun clearLocal() {
        prefs.edit().apply {
            remove(UserConstants.PHONE_NO)
            remove(UserConstants.LOGIN_PIN)
            remove(UserConstants.LOGIN)
            remove(UserConstants.MENTOR_DETAIL)
            remove(UserConstants.MENTOR_OVERVIEW_DETAIL)
            remove(UserConstants.ASSESSMENT_START_TIME)
            remove(UserConstants.REFRESH_TOKEN)
            remove(UserConstants.USER_DIET_MENTOR_DESIGNATION)
            remove(UserConstants.DIET_MENTOR)
            remove(UserConstants.ZIP_HASH)
            remove(UserConstants.SELECTED_USER)
            apply()
        }
    }

    fun getSelectedUserType() = prefs.getString(UserConstants.SELECTED_USER, "")

    fun getUserBearer() = prefs.getString("auth_token_jwt", "")?.let {
        Constants.BEARER_ + it
    } ?: ""
}