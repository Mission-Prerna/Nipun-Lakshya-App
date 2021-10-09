package com.samagra.commons.constants;

import org.jetbrains.annotations.NotNull;

/**
 * Constants used throughout the app.
 * All variables declared here must be public static final.
 *
 * @author Pranav Sharma
 */
public class Constants {

    public static final String KEY_CUSTOMIZE_TOOLBAR = "customize_toolbar";
    public static final String CUSTOM_TOOLBAR_SHOW_NAVICON = "show_nav";
    public static final String CUSTOM_TOOLBAR_BACK_NAVICON_CLICK = "back_on_nav_click";
    public static final String CUSTOM_TOOLBAR_TITLE = "title";

    public static final String COMMON_SHARED_PREFS_NAME = "shared_prefs_odk_ancillary_screen";

    public static final String INTENT_LAUNCH_HOME_ACTIVITY = "org.samagra.prerna.LAUNCH_HOME";
    public static final String INTENT_LAUNCH_ASSESSMENT_HOME_ACTIVITY = "org.samagra.prerna.LAUNCH_ASSESSMENT_HOME";
    public static final String INTENT_LAUNCH_PARENT_HOME = "org.samagra.prerna.LAUNCH_PARENT_HOME";
    public static final String INTENT_LAUNCH_DIET_ASSESSMENT_TYPE_ACTIVITY = "org.samagra.prerna.LAUNCH_DIET_ASSESSMENT_TYPE";


    public static final String INTENT_LAUNCH_PROFILE_SELECTION_ACTIVITY = "LAUNCH_PROFILE_SELECTION";
    public static final String INTENT_LAUNCH_USER_SELECTION = "LAUNCH_USER_SELECTION";
    public static final String LOGOUT_CALLS = "logout_calls";

    public static final String WORK_MANAGER_SHARED_PREFS_NAME = "com.taskscheduler.sharedprefname";

    public static final String APP_LANGUAGE_KEY = "applicationLanguage";

    public static final String ZERO = "0";
    public static final String OPEN_URL = "url";
    public static final String OPEN_URL_TITLE = "open_url_title";
    public static final String USER_DESIGNATION_SRG = "S.R. G";
    public static final String BEARER_ = "Bearer ";
    public static final String NL_NOTIFICATION_CHANNEL = "nl_default_channel";

    public static final String NL_CHATBOT_NOTIFICATION_CHANNEL = "nl_chatbot_notification_channel";
    public static final String NL_SYNCING_NOTIFICATION_CHANNEL = "nl_syncing_notification_channel";

    public static final String USER_PARENT = "parent";
    public static final String USER_TEACHER = "teacher";
    public static final String USER_MENTOR = "mentor";
    public static final String USER_DIET_MENTOR = "Diet Mentor";
    public static final String USER_EXAMINER = "examiner";
    public static final String STATE_LED_ASSESSMENT = "state_led_assessment";

    @NotNull
    public static final String LAST_SYNCED_AT = "LAST_SYNCED_AT";

    public static final String SYNCED_DATE_FORMAT = "yyyy-MM-dd HH-mm-ss";
    public static final String FALLBACK_BASEMAP = "fallback_basemap";

    public static String CHATBOT_SERVICES_BASE_URL = "CHATBOT_SERVICES_BASE_URL";
}
