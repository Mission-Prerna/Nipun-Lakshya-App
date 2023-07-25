package com.samagra.ancillaryscreens.data.prefs;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.commons.models.Result;
import com.samagra.ancillaryscreens.data.model.LoginResponse;
import com.samagra.ancillaryscreens.di.ApplicationContext;
import com.samagra.ancillaryscreens.di.PreferenceInfo;
import com.samagra.ancillaryscreens.utils.Constant;
import com.samagra.commons.PreferenceKeys;
import com.samagra.commons.constants.Constants;
import com.samagra.commons.constants.UserConstants;
import com.samagra.grove.logging.Grove;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Solid implementation of {@link CommonsPreferenceHelper}, performs the read/write operations on the {@link SharedPreferences}
 * used by the ancillary screens. The class is injected to all activities instead of manually creating an object.
 *
 * @author Pranav Sharma
 */
public class CommonsPrefsHelperImpl implements CommonsPreferenceHelper {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences defaultPreferences;
    private static final Gson sGson = new Gson();
    Context context;


    @Inject
    public CommonsPrefsHelperImpl(@ApplicationContext Context context, @PreferenceInfo String prefFileName) {
        this.sharedPreferences = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
        this.context = context;
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getCurrentUserName() {
        return defaultPreferences.getString("user.fullName", "");
    }

    @Override
    public String getCurrentUserId() {
        return defaultPreferences.getString("user.id", "");
    }

    @Override
    public String getPrefByKey(String param) {
        return defaultPreferences.getString(param, "");
    }

    @Override
    public void setCurrentUserLoginFlags() {
        defaultPreferences.edit().putBoolean("isLoggedIn", true).apply();
        defaultPreferences.edit().putBoolean("justLoggedIn", true).apply();

        boolean firstLogIn = sharedPreferences.getBoolean("firstLoginIn", false);
        if (firstLogIn) defaultPreferences.edit().putBoolean("firstLoginIn", false).apply();
        else defaultPreferences.edit().putBoolean("firstLoginIn", true).apply();

        boolean firstLogIn2 = sharedPreferences.getBoolean("firstLoginIn2", false);
        if (!firstLogIn2) defaultPreferences.edit().putBoolean("firstLoginIn2", true).apply();
        else defaultPreferences.edit().putBoolean("firstLoginIn2", false).apply();
    }

    /**
     * @param response
     */

    @Override
    public void setCurrentUserDetailsFromLogin(LoginResponse response) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        Grove.d("Refresh Token is " + response.refreshToken);
        if (response.refreshToken != null)
            editor.putString("refreshToken", response.refreshToken.getAsString());
        else editor.putString("refreshToken", "");
        editor.putString("token", response.token.getAsString());

        if (response.user.has("email"))
            editor.putString("user.email", response.user.get("email").getAsString());
        else editor.putString("user.email", "");

        if (response.user.has("username"))
            editor.putString("user.username", response.user.get("username").getAsString());
        else editor.putString("user.username", response.getUserName());

        if (response.token != null) editor.putString("user.token", response.token.getAsString());

        if (response.user.has("data")) {
            editor.putString("user.data", response.user.get("data").toString());
            JsonObject data = response.user.get("data").getAsJsonObject();

            if (data.has("category"))
                editor.putString("user.category", data.get("category").getAsJsonPrimitive().getAsString());
            else editor.putString("user.category", "");

            if (data.has("joiningDate"))
                editor.putString("user.joiningDate", data.get("joiningDate").getAsJsonPrimitive().getAsString());
            else editor.putString("user.joiningDate", "");

        }
        editor.apply();
    }

    @Override
    public void setCurrentUserAdditionalDetailsFromLogin(LoginResponse response) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        if (response.user.has("registrations")) {
            JsonArray registrations = response.user.get("registrations").getAsJsonArray();
            for (int i = 0; i < registrations.size(); i++) {
                if (registrations.get(i).getAsJsonObject().has("applicationId")) {
                    String applicationId = registrations.get(i).getAsJsonObject().get("applicationId").getAsString();
                    if (applicationId.equals(AncillaryScreensDriver.APPLICATION_ID)) {
                        // This is applicationId for Shiksha Saathi
                        editor.putString("user.role", registrations.get(i).getAsJsonObject().get("roles").getAsJsonArray().get(0).getAsJsonPrimitive().getAsString());
                    }
                }
            }
        }
        if (response.user.has("fullName"))
            editor.putString("user.fullName", response.user.get("fullName").getAsString());
        else if (response.user.has("data") && response.user.get("data") != null && response.user.get("data").getAsJsonObject().has("accountName")) {
            String fullName = response.user.get("data").getAsJsonObject().get("accountName").getAsString();
            editor.putString("user.fullName", fullName);
        } else {
            editor.putString("user.fullName", response.user.get("username").getAsString());
        }
        editor.putString(response.user.get("id").getAsString(), "user.id");
        if (response.user.has("mobilePhone"))
            editor.putString("user.mobilePhone", response.user.get("mobilePhone").getAsString());
        else editor.putString("user.mobilePhone", "");

        if (response.user.has("data") && response.user.get("data") != null) {
            JsonObject userData = response.user.get("data").getAsJsonObject();
            if (userData.has("roleData")) {
                if (userData.get("roleData").getAsJsonObject().has("designation")) {
                    editor.putString("user.designation", response.user.get("data").getAsJsonObject().get("roleData").getAsJsonObject().get("designation").getAsJsonPrimitive().getAsString());
                }
            }
        }
        editor.apply();
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
    public void updateProfileKeyValuePair(String key, String value) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public String getProfileContentValueForKey(String key) {
        return defaultPreferences.getString(key, "");
    }

    @Override
    public boolean isShowSplash() {
        return defaultPreferences.getBoolean(PreferenceKeys.KEY_SHOW_SPLASH, false);
    }

    @Override
    public Long getLastAppVersion() {
        return sharedPreferences.getLong(PreferenceKeys.KEY_LAST_VERSION, 0);
    }

    @Override
    public void updateLastAppVersion(long updatedVersion) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putLong(PreferenceKeys.KEY_LAST_VERSION, updatedVersion);
        editor.apply();
    }

    @Override
    public String fetchDesignation() {
        return defaultPreferences.getString("user.designation", "");
    }

    @Override
    public String getToken() {
        return defaultPreferences.getString(Constant.TOKEN, "");
    }


    @SuppressLint("ApplySharedPref")
    @Override
    public void updateFirstRunFlag(boolean value) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putBoolean(PreferenceKeys.KEY_FIRST_RUN, false);
        editor.commit();
    }

    @Override
    public boolean isLoggedIn() {
        String selectedUser = getSelectedUser();
        if (doesPinSharedPrefExist()) {
            String loginPin = getLoginPin();
            clearLoginPinPref();
            boolean isLoginOldVersion = !loginPin.equals("") && !selectedUser.equals(Constants.USER_PARENT);
            if (isLoginOldVersion)
                saveIsUserLoggedIn(true);
            return isLoginOldVersion;
        } else {
            return getIsUserLoggedIn() && !selectedUser.equals(Constants.USER_PARENT);
        }
    }

    public boolean doesPinSharedPrefExist() {
        return defaultPreferences.contains(UserConstants.LOGIN_PIN);
    }
    public void clearLoginPinPref() {
        SharedPreferences.Editor edit = defaultPreferences.edit();
        edit.remove(UserConstants.LOGIN_PIN);
        edit.apply();
    }

    @Override
    public int getPreviousVersion() {
        return context.getSharedPreferences("VersionPref", MODE_PRIVATE).getInt("appVersionCode", 0);
    }

    @Override
    public String getRefreshToken() {
        return defaultPreferences.getString(UserConstants.REFRESH_TOKEN, "");
    }

    @Override
    public void updateAppVersion(int currentVersion) {
        SharedPreferences.Editor editor = context.getSharedPreferences("VersionPref", MODE_PRIVATE).edit();
        editor.putInt("appVersionCode", currentVersion);
        editor.putBoolean("isAppJustUpdated", true);
        editor.apply();
    }

    @Override
    public void updateToken(String token) {
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getLoginPin() {
        return defaultPreferences.getString(UserConstants.LOGIN_PIN, "");
    }

    public void clearData() {
        SharedPreferences.Editor edit = defaultPreferences.edit();
        //todo Anyone to delete keys
        /*
         *  after force update or after 1.5.4, 2nd major app updates delete all unnecessary keys
         *  Requirement is to clear unused keys from the apps
         * */
        edit.remove(UserConstants.USER_DIET_MENTOR_DESIGNATION);
        edit.remove(Constant.PHONE_NO);
        //-------------------------------------
        edit.remove(UserConstants.LOGIN_PIN);
        edit.remove(UserConstants.LOGIN);
        edit.remove(UserConstants.MENTOR_DETAIL);
        edit.remove(UserConstants.MENTOR_OVERVIEW_DETAIL);
        edit.remove(UserConstants.ASSESSMENT_START_TIME);
        edit.remove(UserConstants.REFRESH_TOKEN);
        edit.remove(UserConstants.DIET_MENTOR);
        //clear matadata
        edit.remove(UserConstants.ZIP_HASH);
        edit.remove(UserConstants.SELECTED_USER);
        edit.remove(Constant.META_ACTORS);
        edit.remove(Constant.META_SUBJECTS);
        edit.remove(Constant.META_DESIGNATIONS);
        edit.remove(Constant.META_ASSESSMENT_TYPES);
        edit.remove(Constant.PREVIOUS_META_FETCH);
        edit.remove(Constants.FALLBACK_BASEMAP);
        edit.apply();
    }

    public void saveRefreshToken(@NotNull String refreshToken) {
        SharedPreferences.Editor edit = defaultPreferences.edit();
        edit.putString(UserConstants.REFRESH_TOKEN, refreshToken);
        edit.apply();

    }

    public void updateFormConfiguredListText(@NotNull String toJson) {
        defaultPreferences.edit().putString("assessment_form_list1", toJson).apply();
    }

    public void saveMentorDetails(String mentorDetails) {
        defaultPreferences.edit().putString(UserConstants.MENTOR_DETAIL, mentorDetails).apply();
    }

    public void saveMentorOverViewDetails(String startODKTime) {
        defaultPreferences.edit().putString(UserConstants.MENTOR_OVERVIEW_DETAIL, startODKTime).apply();
    }

    public String getMentorDetails() {
        return defaultPreferences.getString(UserConstants.MENTOR_DETAIL, "");
    }

    public String getMentorOverviewDetails() {
        return defaultPreferences.getString(UserConstants.MENTOR_OVERVIEW_DETAIL, "");
    }

    public void saveAssessmentStartTime(long startODKTime) {
        defaultPreferences.edit().putLong(UserConstants.ASSESSMENT_START_TIME, startODKTime).apply();
    }

    public long getAssessmentStartTime() {
        return defaultPreferences.getLong(UserConstants.ASSESSMENT_START_TIME, 0);
    }

    public void saveODKFormQuesLength(int quesLength) {
        defaultPreferences.edit().putInt(Constant.QUES_LENGTH, quesLength).apply();
    }

    public int getODKFormQuesLength() {
        return defaultPreferences.getInt(Constant.QUES_LENGTH, 0);
    }

    public void saveWorkerId(@NotNull UUID id) {
        defaultPreferences.edit().putString("uuid", id.toString()).apply();
    }

    public void saveSelectedUser(String userType) {
        defaultPreferences.edit().putString(UserConstants.SELECTED_USER, userType).apply();
    }

    public void saveSelectStateLedAssessment(String typeUser) {
        defaultPreferences.edit().putString(UserConstants.DIET_MENTOR, typeUser).apply();
    }

    public String getSaveSelectStateLedAssessment() {
        return defaultPreferences.getString(UserConstants.DIET_MENTOR, "");
    }

    public String getSelectedUser() {
        return defaultPreferences.getString(UserConstants.SELECTED_USER, "");
    }

    public void saveCompetencyData(String competencyString) {
        defaultPreferences.edit().putString(Constant.COMPETENCY_DATA, competencyString).apply();
    }

    public String getCompetencyData() {
        return defaultPreferences.getString(Constant.COMPETENCY_DATA, "");
    }

    public void saveOdkServerUrl(String odkServerUrl) {
        defaultPreferences.edit().putString(Constant.ODK_SERVER_URL, odkServerUrl).apply();
    }

    public void saveOdkServerSubmissionUrl(String odkSubmissionUrl) {
        defaultPreferences.edit().putString(Constant.ODK_SERVER_SUBMISSION_URL, odkSubmissionUrl).apply();
    }

    public String getOdkServerUrl(String odkServerUrl) {
        return defaultPreferences.getString(Constant.ODK_SERVER_URL, "");
    }

    public String getOdkServerSubmissionUrl(String odkServerUrl) {
        return defaultPreferences.getString(Constant.ODK_SERVER_SUBMISSION_URL, "");
    }

    public void saveAuthToken(String authToken) {
        defaultPreferences.edit().putString(Constant.AUTH_TOKEN_JWT, authToken).apply();
    }

    public String getAuthToken() {
        return defaultPreferences.getString(Constant.AUTH_TOKEN_JWT, "");
    }

    public void saveIsUserLoggedIn(Boolean isUserLoggedIn) {
        defaultPreferences.edit().putBoolean(UserConstants.LOGIN, isUserLoggedIn).apply();
    }

    public Boolean getIsUserLoggedIn() {
        return defaultPreferences.getBoolean(UserConstants.LOGIN, false);
    }

    /*
     * three assessment types 1. nipun_lakshya, 2. nipun_suchi 3. state_led_assessment
     * */
    public void saveAssessmentType(String assessmentType) {
        defaultPreferences.edit().putString(Constant.ASSESSMENT_TYPE, assessmentType).apply();
    }

    public String getAssessmentType() {
        return defaultPreferences.getString(Constant.ASSESSMENT_TYPE, "");
    }

    public void saveActorsList(String actorsListJson) {
        defaultPreferences.edit().putString(Constant.META_ACTORS, actorsListJson).apply();
    }

    public String getActorsListJson() {
        return defaultPreferences.getString(Constant.META_ACTORS, "");
    }

    public void saveSubjectsList(String subjectsListJson) {
        defaultPreferences.edit().putString(Constant.META_SUBJECTS, subjectsListJson).apply();
    }

    public String getSubjectsListJson() {
        return defaultPreferences.getString(Constant.META_SUBJECTS, "");
    }

    public void saveAssessmentTypesList(String assessmentTypesListJson) {
        defaultPreferences.edit().putString(Constant.META_ASSESSMENT_TYPES, assessmentTypesListJson).apply();
    }

    public String getAssessmentTypesListJson() {
        return defaultPreferences.getString(Constant.META_ASSESSMENT_TYPES, "");
    }

    public void saveDesignationsList(String actorsListJson) {
        defaultPreferences.edit().putString(Constant.META_DESIGNATIONS, actorsListJson).apply();
    }

    public String getDesignationsListJson() {
        return defaultPreferences.getString(Constant.META_DESIGNATIONS, "");
    }

    public String getString(@NonNull String key, @Nullable String defaultValue) {
        return defaultPreferences.getString(key, defaultValue);
    }

    public void putString(@NotNull String key, @NotNull String value) {
        defaultPreferences.edit().putString(key, value).apply();
    }

    public void removeKey(@NotNull String key) {
        defaultPreferences.edit().remove(key).apply();
    }

    @Override
    public long getPreviousMetadataFetch() {
        return defaultPreferences.getLong(Constant.PREVIOUS_META_FETCH, 0L);
    }

    @Override
    public void setPreviousMetadataFetch(Long timestamp) {
        defaultPreferences.edit().putLong(Constant.PREVIOUS_META_FETCH, timestamp).apply();
    }

    /*
     * Getting saved json to object data of mentor details
     * */
    @Override
    public Result getMentorDetailsData() {
        Result mentorDetailsFromPrefs = null;
        try {
            String mentorDetails = getMentorDetails();
            if (mentorDetails != null && !mentorDetails.equalsIgnoreCase("")) {
                mentorDetailsFromPrefs = sGson.fromJson(mentorDetails, Result.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mentorDetailsFromPrefs;
    }

    @Override
    public Date getLastSyncedAt() {
        String lastSyncedAt = defaultPreferences.getString(Constants.LAST_SYNCED_AT, null);
        if (lastSyncedAt != null) {
            try {
                return new SimpleDateFormat(Constants.SYNCED_DATE_FORMAT, Locale.getDefault()).parse(lastSyncedAt);
            } catch (ParseException e) {
                Timber.e(e);
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);
        return cal.getTime();
    }

    @Override
    public void markDataSynced() {
        String date = new SimpleDateFormat(Constants.SYNCED_DATE_FORMAT, Locale.getDefault()).format(new Date());
        defaultPreferences.edit().putString(Constants.LAST_SYNCED_AT, date).apply();
    }
}
