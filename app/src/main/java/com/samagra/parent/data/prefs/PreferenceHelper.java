package com.samagra.parent.data.prefs;


import java.util.ArrayList;

import javax.inject.Singleton;

/**
 * Interface defining the access point to {@link android.content.SharedPreferences} used by the app module.
 * All access functions to be implemented by a solid implementation of this interface. This implementation should be
 * a {@link Singleton}.
 *
 * @author Pranav Sharma
 * @see AppPreferenceHelper
 */
@Singleton
public interface PreferenceHelper {
    String getCurrentUserName();

    String getToken();

    boolean isFirstLogin();

    String fetchCurrentSystemLanguage();

    int getPreviousVersion();

    boolean isFirstRun();

    boolean isShowSplash();

    void updateAppVersion(int currentVersion);

    void updateFirstRunFlag(boolean value);

    void updateLastAppVersion(long updatedVersion);

    Long getLastAppVersion();

    void updateFormVersion(String version);

    String getCurrentUserFullName();

    boolean isLoggedIn();

    boolean hasDownloadedStudentData();

    String getUserContactDetails();
    String getDevice();

    void updateDevice(String deviceIdentifier);

    boolean isPrivacyPolicyAccepted();

    void updatePrivacyPolicyReadStatus();

    void updateAssessmentRangeConfigListJSON(String formAssessmentsConfigText);

    String assessmentRangeConfigListJSON();

    void updateProfile(String student);
    void removeAssessmentEventList();
    void updateProfileShown(boolean b);
    boolean isUpdateProfileShown();

    void updateFormConfiguredListText(String toJson);

    void formsUpdated(boolean flag);

    String getRefreshToken();
}