package com.samagra.commons

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.samagra.commons.constants.UserConstants
import org.json.JSONObject

object AppPreferences {

    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    var chatBotList: String
        get() = prefs.getString(PreferenceKeys.CHAT_BOT_LIST, "[]") ?: "[]"
        set(botListStr) {
            prefs.edit().putString(PreferenceKeys.CHAT_BOT_LIST, botListStr).apply()
        }

    fun submitChat(chatId: String, chat: String) {
        //TODO FIX
        //org.json.JSONException: Value [{"savedMsg":"undefined","msgId":"undefined","botId":"d655cf03-1f6f-4510-acf6-d3f51b488a5e"}] of type org.json.JSONArray cannot be converted to JSONObject
        /*val objString = prefs.getString(PreferenceKeys.CHAT_BOT_HISTORY, "{}")
        val historyListObj = JSONArray(objString)
        historyListObj.put(chatId, chat)
        prefs.edit().putString(PreferenceKeys.CHAT_BOT_HISTORY, historyListObj.toString()).apply()*/
    }

    fun getChatHistory(): String {
        return prefs.getString(PreferenceKeys.CHAT_BOT_HISTORY, "{}") ?: "{}"
    }

    fun getStarredMsgs(): String {
        return prefs.getString(PreferenceKeys.CHAT_BOT_STARRED_MSGS, "{}")!!
    }

    fun saveStarredMessages(savedMsg: String) {
        prefs.edit()
            .putString(PreferenceKeys.CHAT_BOT_STARRED_MSGS, savedMsg)
            .apply()
    }

    fun saveChatBotDetails(botListJson: String) {
        prefs.edit().putString(PreferenceKeys.CHAT_BOT_DETAILS, botListJson).apply()
    }

    fun getChatBotDetails(): String {
        return prefs.getString(PreferenceKeys.CHAT_BOT_DETAILS, "[]") ?: "[]"
    }

    fun getUserAuth() = prefs.getString("auth_token_jwt", "") ?: ""
    fun getUserMobile() = prefs.getString("mentorDetail", "{}")?.let { JSONObject(it) }
        ?.getString("phone_no") ?: ""

    fun getUserId() =  try {
        prefs.getString("mentorDetail", "{}")?.let { JSONObject(it) }
            ?.getString("id") ?: ""
    } catch (e: Exception) {
        ""
    }

    fun clearLocal() {
        prefs.edit().apply {
            remove(UserConstants.PHONE_NO)
            remove(UserConstants.LOGIN_PIN)
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

    fun getSelectedUserType() = prefs.getString(UserConstants.SELECTED_USER, "");
}