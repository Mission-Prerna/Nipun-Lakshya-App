package com.samagra.parent;

import com.samagra.commons.BuildConfig;

/**
 * These are constants required by the app module. All values declared should be public static and the constants should
 * preferably be final constants.
 *
 * @author Pranav Sharmas
 */
public class AppConstants {
    public static final String NIPUN_ABHYAS = "nipun_abhyas";
    public static final String SUCHI_ABHYAS = "suchi_abhyas";
    public static final String NIPUN_LAKSHYA = "nipun_lakshya";
    public static final String NIPUN_SUCHI = "nipun_suchi";
    /*
    * Created to get non state_led_assessments (lakshya and suchi for home screen)
    *
    * */
    public static final String NON_STATE_LED_ASSESSMENT = "non_state_led_assessment";
    public static final String INTENT_SCHOOL_DATA = "schoolData";
    public static final String INTENT_RA_PROPERTIES = "read_along_properties";
    public static final String INTENT_SELECTED_GRADE = "selectedGrade";
    public static final String INTENT_SELECTED_SUBJECT = "schoolSubject";
    public static final String INTENT_ODK_FORM_ID = "odkFormId";
    public static final String INTENT_COMPETENCY_NAME = "competencyName";
    public static final String PREF_FILE_NAME = "SAMAGRA_PREFS";
    public static final String SENDER_EMAIL_ID = "";
    static final String BASE_API_URL = BuildConfig.AUTH_API_BASE_URL;
    public static final String INTENT_ODK_PROPERTIES = "odk_properties";
    public static final String RECEIVER_EMAIL_ID = "";
    public static final int INTERMEDIATE_HINDI = 1100;
    public static final int INTERMEDIATE_MATH = 1102;
    public static final int INTERMEDIATE_MATH_RESULT = 1502;
    public static final int FINAL_HINDI_4_OR_5 = 1101;
    public static String JWTToken = BuildConfig.STAGING_HASURA_SERVER_AUTH_TOKEN;
    public static final String INTENT_FINAL_RESULT_LIST= "finalResultsList";
    public static final String INTENT_FINAL_RESULT_MAP= "finalResultsMap";
    public static final String INTENT_FINAL_STATE_LED_RESULT_LIST = "finalStateLedResultsMap";

    public static final String INTENT_STUDENT_COUNT = "students_count";

    public static final String INTENT_ODK_RESULT = "odkResult";
    public static final String TYPE_COMBINED = "combined";
    public static final String COMBINED_STUDENT = "combined_student";
    public static final String INTENT_BOLO_RESULT = "combinedResults";
    public static final String BOLO_START_TIME ="startTime";
    public static final String ODK_START_TIME= "odkStartTime";

    public static final String KEY_MENTORS ="mentors";
    public static final String KEY_METADATA ="metadata";
    public static final String KEY_SCHOOLS_AND_VISITS ="schools_and_visits";
    public static final String KEY_OVERVIEWS ="over_views";
    public static final String KEY_ODK_FORMS ="odk_forms";
    public static final String KEY_REMOTE_CONFIG ="remote_config";
    public static final String USER_PARENT ="parent";
    public static final String USER_TEACHER ="teacher";
    public static final String USER_MENTOR ="mentor";
    public static final String USER_EXAMINER ="examiner";
    public static final String KEY_COMPETENCIES = "competencies";
    public static String INTENT_COMPLETE_NIPUN_MAP = "Intent_Complete_Nipun_Map";
    public static final String DIET_MENTOR_SPOT_ASSESSMENT ="diet_mentor_spot_assessment";
    public static final String DIET_MENTOR_STATE_LED_ASSESSMENT ="diet_mentor_state_led_assessment";
    public static final String SHOW_FINAL_RESULTS = "showFinalResults";
    public static final String TYPE_UPS = "UPS";
    public static final String READ_ALONG_CRITERIA_KEY = "RA";
    public static final String ODK_CRITERIA_KEY = "ODK";

//    public static final String ASSESSMENT_WORKFLOW_CONFIG = "assessment_workflow_config";
}
