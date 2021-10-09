package com.samagra.parent.data.prefs;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.samagra.commons.LocaleManager;
import com.samagra.commons.PreferenceKeys;
import com.samagra.commons.constants.UserConstants;
import com.samagra.parent.di.ApplicationContext;
import com.samagra.parent.di.PreferenceInfo;

import org.odk.collect.android.preferences.GeneralKeys;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppPreferenceHelper implements PreferenceHelper {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences defaultPreferences;
    private final Context context;

    @Inject
    public AppPreferenceHelper(@ApplicationContext Context context, @PreferenceInfo String prefFileName) {
        this.sharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    @Override
    public String getCurrentUserName() {
        return defaultPreferences.getString("user.username", "");
    }

    @Override
    public String getCurrentUserFullName() {
        if (defaultPreferences.getString("user.fullName", "").equals(""))
            return defaultPreferences.getString("user.username", "");
        else
            return defaultPreferences.getString("user.fullName", "");
    }

    @Override
    public boolean hasDownloadedStudentData() {
        return defaultPreferences.getBoolean("downloadedStudentData", false);
    }

    @Override
    public String getUserContactDetails() {
        return defaultPreferences.getString("user.mobilePhone", "");
    }

    @Override
    public void updateProfile(String student) {
        defaultPreferences.edit().putString("profile", student).apply();
    }

    @Override
    public void updateProfileShown(boolean b) {
        defaultPreferences.edit().putBoolean("isUpdateProfileShown", b).apply();
    }

    @Override
    public boolean isUpdateProfileShown() {
        return defaultPreferences.getBoolean("isUpdateProfileShown", true);
    }

    //Main Form List
    @Override
    public void updateFormConfiguredListText(String toJson) {
        defaultPreferences.edit().putString("assessment_form_list", toJson).apply();

    }

    @Override
    public void formsUpdated(boolean flag) {
        defaultPreferences.edit().putBoolean("assessment_sample_updated", flag).apply();
        if (flag) {
            defaultPreferences.edit().putBoolean("hindi_assessment_sample_updated", flag).apply();
            defaultPreferences.edit().putBoolean("math_assessment_sample_updated", flag).apply();
        }
    }

    @Override
    public void removeAssessmentEventList() {
        defaultPreferences.edit().remove("ASSESSMENT_EVENT_LIST").apply();
    }

    public static Object getObjectFromJson(String jsonString, Class resultObjectClass) {
        if (resultObjectClass == null) {
            return jsonString;
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, resultObjectClass);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getPreviousVersion() {
        return context.getSharedPreferences("VersionPref", MODE_PRIVATE).getInt("appVersionCode", 0);
    }

    @Override
    public String getToken() {
        return defaultPreferences.getString("token", "");
    }

    @Override
    public boolean isFirstLogin() {
        return defaultPreferences.getBoolean("firstLoginIn", false);
    }

    @Override
    public boolean isFirstRun() {
        return defaultPreferences.getBoolean(PreferenceKeys.KEY_FIRST_RUN, true);
    }

    @Override
    public boolean isShowSplash() {
        return defaultPreferences.getBoolean(GeneralKeys.KEY_SHOW_SPLASH, false);
    }

    @Override
    public void updateAppVersion(int currentVersion) {
        SharedPreferences.Editor editor = context.getSharedPreferences("VersionPref", MODE_PRIVATE).edit();
        editor.putInt("appVersionCode", currentVersion);
        editor.putBoolean("isAppJustUpdated", true);
        editor.commit();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void updateFirstRunFlag(boolean value) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putBoolean(GeneralKeys.KEY_FIRST_RUN, false);
        editor.commit();
    }

    @Override
    public Long getLastAppVersion() {
        return sharedPreferences.getLong(GeneralKeys.KEY_LAST_VERSION, 0);
    }

    @Override
    public void updateLastAppVersion(long updatedVersion) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putLong(GeneralKeys.KEY_LAST_VERSION, updatedVersion);
        editor.apply();
    }

    @Override
    public void updateFormVersion(String version) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putString("formVersion", version);
        editor.apply();
    }

    @Override
    public String fetchCurrentSystemLanguage() {
        if (defaultPreferences.getString("currentLanguage", "").isEmpty()) {
            defaultPreferences.edit().putString("currentLanguage", LocaleManager.ENGLISH).apply();
            return LocaleManager.HINDI;
        } else {
            return defaultPreferences.getString("currentLanguage", "");
        }
    }

    private String toJson(Object inputObject) {
        try {
            Gson gson = new Gson();
            return gson.toJson(inputObject);
        } catch (Exception e) {
            return "";
        }
    }


    @Override
    public String getDevice() {
        return defaultPreferences.getString("device", "");
    }

    @Override
    public void updateDevice(String deviceIdentifier) {
        defaultPreferences.edit().putString("device", deviceIdentifier).apply();
    }

    @Override
    public boolean isPrivacyPolicyAccepted() {
        return defaultPreferences.getBoolean("isPrivacyPolicyRead", false);
    }

    @Override
    public void updatePrivacyPolicyReadStatus() {
        defaultPreferences.edit().putBoolean("isPrivacyPolicyRead", true).apply();
    }

    @Override
    public void updateAssessmentRangeConfigListJSON(String formAssessmentsConfigText) {
        defaultPreferences.edit().putString("formAssessmentsConfigText", formAssessmentsConfigText).apply();
    }

    @Override
    public String assessmentRangeConfigListJSON() {
        return defaultPreferences.getString("formAssessmentsConfigText", "");
    }

    @Override
    public boolean isLoggedIn() {
        return defaultPreferences.getBoolean("isLoggedIn", false);
    }

    @Override
    public String getRefreshToken() {
        return defaultPreferences.getString(UserConstants.REFRESH_TOKEN, "");
    }

}
