@file:Suppress("SENSELESS_COMPARISON")

package com.samagra.commons.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.samagra.commons.BuildConfig
import timber.log.Timber

object RemoteConfigUtils {
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private val firebaseCallbackList = ArrayList<RemoteConfigListener?>()
    var isFirebaseInitialized = false

    const val RESULTS_INSERTION_CHUNK_SIZE: String = "results_insertion_chunk_size"
    const val FUSION_AUTH_APPLICATION_ID = "fusion_auth_app_id"
    const val FUSION_AUTH_PASSWORD = "fusion_auth_password"
    const val FUSION_AUTH_API_KEY = "fusion_auth_api_key"
    const val LOGIN_SERVICE_BASE_URL = "user_service_base_url"
    const val HELP_FAQ_FORM_URL: String = "help_faq_form_url"
    const val ASSESSMENT_WORKFLOW_CONFIG = "assessment_workflow_config"
    const val ASSESSMENT_WORKFLOW_CONFIG_EXAMINER = "assessment_workflow_config_examiner"
    const val HELP_FAQ_JSON = "help_faq_json"
    const val SYNC_FALLBACK_CONFIG = "sync_fallback_config"
    const val ODK_FORM_QUES_LENGTH = "odk_form_ques_length"
    const val PRIVACY_POLICY_URL = "privacy_policy_url"
    const val POSTHOG_SERVER_URL = "POSTHOG_SERVER_URL"
    const val POSTHOG_SERVER_API_KEY = "POSTHOG_SERVER_API_KEY"
    const val ODK_SERVER_URL = "ODK_SERVER_URL"
    const val ODK_SERVER_SUBMISSION_URL = "ODK_SERVER_SUBMISSION_URL"
    const val HASURA_SERVER_AUTH_TOKEN = "HASURA_SERVER_AUTH_TOKEN"
    const val HASURA_SERVER_BASE_URL = "HASURA_SERVER_BASE_URL"
    const val MINIMUM_VIABLE_VERSION = "minimum_viable_version"
    const val FORMS_ZIP_URL = "forms_zip_url"
    const val CHATBOT_ENABLED = "chatbot_enabled"
    const val CHATBOT_URLS = "chatbot_urls"
    const val INFO_NOTES_LOGIN = "info_notes_login"
    const val INFO_NOTES_DETAIL_SELECTION = "info_notes_detail_selection"
    const val PROFILE_SELECTION_INFO = "profile_selection_info"
    const val FORCE_LOGOUT_VERSION = "force_logout_version"
    const val SYNC_WORKER_INTERVAL_IN_MINUTES = "sync_worker_interval_in_minutes"
    const val API_BASE_URL = "API_BASE_URL"
    const val METADATA_FETCH_DELTA_IN_HOURS = "metadata_fetch_delta_in_hours"
    const val ZIP_HASH = "ZIP_HASH"
    const val GATEKEEPER_BASE_URL = "GATEKEEPER_BASE_URL"
    const val GATEKEEPER_API_KEY = "GATEKEEPER_API_KEY"
    const val GEOFENCING_CONFIG = "geofencing_config"
    const val CHATBOT_ICON_VISIBILITY_TO_ACTOR = "chatbot_icon_visibility_to_actor"
    const val TOKEN_SYNC_REPEAT_IN_DAYS = "token_sync_repeat_in_days"

    fun addFirebaseCallback(remoteConfigListener: RemoteConfigListener) {
        firebaseCallbackList.add(remoteConfigListener)
    }

    private val DEFAULTS: HashMap<String, Any> = hashMapOf(
        POSTHOG_SERVER_URL to BuildConfig.DEFAULT_POSTHOG_SERVER_URL,
        POSTHOG_SERVER_API_KEY to BuildConfig.DEFAULT_POSTHOG_SERVER_API_KEY,
        ODK_SERVER_URL to BuildConfig.STAGING_ODK_SERVER_URL,
        HASURA_SERVER_AUTH_TOKEN to BuildConfig.STAGING_HASURA_SERVER_AUTH_TOKEN,
        HASURA_SERVER_BASE_URL to BuildConfig.STAGING_HASURA_URL,
        FORMS_ZIP_URL to BuildConfig.STAGING_FORM_ZIP_URL,
        CHATBOT_URLS to BuildConfig.DEFAULT_CHATBOT_URLS,
        FUSION_AUTH_API_KEY to BuildConfig.DEFAULT_FUSION_AUTH_API_KEY,
        FUSION_AUTH_PASSWORD to BuildConfig.DEFAULT_FUSION_AUTH_PASSWORD,
        FUSION_AUTH_APPLICATION_ID to BuildConfig.DEFAULT_FUSION_AUTH_APPLICATION_ID,
        ZIP_HASH to BuildConfig.STAGING_FORM_ZIP_HASH,
        LOGIN_SERVICE_BASE_URL to BuildConfig.STAGING_LOGINSERVICE_URL,
        API_BASE_URL to BuildConfig.DEFAULT_API_BASE_URL,
        GATEKEEPER_BASE_URL to BuildConfig.STAGING_GATEKEEPER_URL,
        GATEKEEPER_API_KEY to BuildConfig.STAGING_GATEKEEPER_API_KEY,
        MINIMUM_VIABLE_VERSION to 160010,
        ASSESSMENT_WORKFLOW_CONFIG to "{\"flowConfigs\":[{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":9},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":14},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":1},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":2},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":15},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":5},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":6},{\"gradeNumber\":2,\"states\":[2],\"subject\":\"Hindi\",\"competencyId\":10},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":11},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":3},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":4},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":16},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":17},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":7},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":8},{\"gradeNumber\":3,\"states\":[2],\"subject\":\"Hindi\",\"competencyId\":12},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":13},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":18},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":19},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":20},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":21},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":22},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":23},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":24},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":25},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":25},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":27},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":28},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":29},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":30},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":31},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":32},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":33},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":34},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":35},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":36}],\"actions\":[{\"count\":0,\"futureStateId\":0,\"id\":1,\"type\":\"POP\"},{\"count\":0,\"futureStateId\":0,\"id\":2,\"type\":\"CLEAR\"},{\"count\":2,\"futureStateId\":1,\"id\":3,\"type\":\"PUSH\"}],\"states\":[{\"decision\":{\"failureActions\":[1],\"id\":1,\"successActions\":[1],\"meta\":{\"testType\":\"Count\",\"wordCount\":0}},\"gradeNumber\":1,\"id\":1,\"maxFailureCount\":3,\"stateData\":{\"successCriteria\":1,\"formID\":\"\"},\"subject\":\"Math\",\"type\":\"odk\"},{\"decision\":{\"failureActions\":[1],\"id\":2,\"successActions\":[1],\"meta\":{\"testType\":\"Fluency\",\"wordCount\":8}},\"gradeNumber\":1,\"id\":2,\"maxFailureCount\":3,\"stateData\":{\"successCriteria\":5,\"formID\":\"\"},\"subject\":\"Hindi\",\"type\":\"bolo\"}],\"chapterMapping\":[{\"grade\":2,\"subject\":\"Math\",\"competencyId\":1,\"type\":\"odk\",\"refIds\":[\"g2m_w1\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":2,\"type\":\"odk\",\"refIds\":[\"g2m_w2\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":3,\"type\":\"odk\",\"refIds\":[\"g3m_w1\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":4,\"type\":\"odk\",\"refIds\":[\"g3m_w2\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":5,\"type\":\"odk\",\"refIds\":[\"g2h_w3\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":6,\"type\":\"odk\",\"refIds\":[\"g2h_w4\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":7,\"type\":\"odk\",\"refIds\":[\"g3h_w3\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":8,\"type\":\"odk\",\"refIds\":[\"g3h_w4\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":9,\"type\":\"odk\",\"refIds\":[\"g1h_npl_5\",\"g1h_npl_1_52\",\"g1h_npl_1_53\",\"g1h_npl_1_54\",\"g1h_npl_1_55\",\"g1h_npl_1_56\",\"g1h_npl_1_57\",\"g1h_npl_1_58\",\"g1h_npl_1_59\",\"g1h_npl_1_510\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":10,\"type\":\"odk\",\"refIds\":[\"g2h_npl_1\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":10,\"type\":\"bolo\",\"refIds\":[\"upp_g2h_s1\",\"g2h_s10_v1\",\"g2h_s9_v1\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":11,\"type\":\"odk\",\"refIds\":[\"g2h_npl_2\",\"g2h_npl_2_2\",\"g2h_npl_2_3\",\"g2h_npl_2_4\",\"g2h_npl_2_5\",\"g2h_npl_2_6\",\"g2h_npl_2_7\",\"g2h_npl_2_8\",\"g2h_npl_2_9\",\"g2h_npl_2_10\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":12,\"type\":\"bolo\",\"refIds\":[\"upp_g3h_s1\",\"g3h_s2_v1\",\"g3h_s3_v1\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":13,\"type\":\"odk\",\"refIds\":[\"g3h_npl_2\",\"g3h_npl_2_2\",\"g3h_npl_2_3\",\"g3h_npl_2_4\",\"g3h_npl_2_5\",\"g3h_npl_2_6\",\"g3h_npl_2_7\",\"g3h_npl_2_8\",\"g3h_npl_2_9\",\"g3h_npl_2_10\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":14,\"type\":\"odk\",\"refIds\":[\"g1m_npl_1\",\"g1m_npl_1_2\",\"g1m_npl_1_3\",\"g1m_npl_1_4\",\"g1m_npl_1_5\",\"g1m_npl_1_6\",\"g1m_npl_1_7\",\"g1m_npl_1_8\",\"g1m_npl_1_9\",\"g1m_npl_1_10\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":15,\"type\":\"odk\",\"refIds\":[\"g2m_npl_1\",\"g2m_npl_1_2\",\"g2m_npl_1_3\",\"g2m_npl_1_4\",\"g2m_npl_1_5\",\"g2m_npl_1_6\",\"g2m_npl_1_7\",\"g2m_npl_1_8\",\"g2m_npl_1_9\",\"g2m_npl_1_10\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":16,\"type\":\"odk\",\"refIds\":[\"g3m_npl_1\",\"g3m_npl_1_2\",\"g3m_npl_1_3\",\"g3m_npl_1_4\",\"g3m_npl_1_5\",\"g3m_npl_1_6\",\"g3m_npl_1_7\",\"g3m_npl_1_8\",\"g3m_npl_1_9\",\"g3m_npl_1_10\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":17,\"type\":\"odk\",\"refIds\":[\"g3m_npl_2\",\"g3m_npl_2_2\",\"g3m_npl_2_3\",\"g3m_npl_2_4\",\"g3m_npl_2_5\",\"g3m_npl_2_6\",\"g3m_npl_2_7\",\"g3m_npl_2_8\",\"g3m_npl_2_9\",\"g3m_npl_2_10\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":18,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w5_1\",\"g1m_npl_w5_2\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":19,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w6_1\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":20,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w7_1\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":21,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w8_1\",\"g1m_npl_w8_2\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":22,\"type\":\"odk\",\"refIds\":[\"g2m_npl_w6_1\",\"g2m_npl_w6_2\",\"g2m_npl_w6_3\",\"g2m_npl_w6_4\",\"g2m_npl_w6_5\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":23,\"type\":\"odk\",\"refIds\":[\"g2m_npl_w8_1\",\"g2m_npl_w8_2\",\"g2m_npl_w8_3\",\"g2m_npl_w8_4\",\"g2m_npl_w8_5\",\"g2m_npl_w8_6\",\"g2m_npl_w8_7\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":24,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w4_1\",\"g3m_npl_w4_2\",\"g3m_npl_w4_3\",\"g3m_npl_w4_4\",\"g3m_npl_w4_5\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":25,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w5_1\",\"g3m_npl_w5_2\",\"g3m_npl_w5_3\",\"g3m_npl_w5_4\",\"g3m_npl_w5_5\",\"g3m_npl_w5_6\",\"g3m_npl_w5_7\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":26,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w6_1\",\"g3m_npl_w6_2\",\"g3m_npl_w6_3\",\"g3m_npl_w6_4\",\"g3m_npl_w6_5\",\"g3m_npl_w6_6\",\"g3m_npl_w6_7\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":27,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w7_1\",\"g3m_npl_w7_2\",\"g3m_npl_w7_3\",\"g3m_npl_w7_4\",\"g3m_npl_w7_5\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":28,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w8_1\",\"g3m_npl_w8_2\",\"g3m_npl_w8_3\",\"g3m_npl_w8_4\",\"g3m_npl_w8_5\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":29,\"type\":\"odk\",\"refIds\":[\"g1h_npl_w5_1\",\"g1h_npl_w5_2\",\"g1h_npl_w5_3\",\"g1h_npl_w5_4\",\"g1h_npl_w5_5\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":30,\"type\":\"odk\",\"refIds\":[\"g1h_npl_w6_1\",\"g1h_npl_w6_2\",\"g1h_npl_w6_3\",\"g1h_npl_w6_4\",\"g1h_npl_w6_5\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":31,\"type\":\"odk\",\"refIds\":[\"g1h_npl_w7_1\",\"g1h_npl_w7_2\",\"g1h_npl_w7_3\",\"g1h_npl_w7_4\",\"g1h_npl_w7_5\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":32,\"type\":\"odk\",\"refIds\":[\"g2h_npl_w5_1\",\"g2h_npl_w5_2\",\"g2h_npl_w5_3\",\"g2h_npl_w5_4\",\"g2h_npl_w5_5\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":33,\"type\":\"odk\",\"refIds\":[\"g2h_npl_w6_1\",\"g2h_npl_w6_2\",\"g2h_npl_w6_3\",\"g2h_npl_w6_4\",\"g2h_npl_w6_5\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":34,\"type\":\"odk\",\"refIds\":[\"g2h_npl_w7_1\",\"g2h_npl_w7_2\",\"g2h_npl_w7_3\",\"g2h_npl_w7_4\",\"g2h_npl_w7_5\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":35,\"type\":\"odk\",\"refIds\":[\"g3h_npl_w5_1\",\"g3h_npl_w5_2\",\"g3h_npl_w5_3\",\"g3h_npl_w5_4\",\"g3h_npl_w5_5\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":36,\"type\":\"odk\",\"refIds\":[\"g3h_npl_w6_1\",\"g3h_npl_w6_2\",\"g3h_npl_w6_3\"]}]}",
        PRIVACY_POLICY_URL to "https://docs.google.com/document/d/1HXO-Y-_AoDoEmup-K0ANSEpM3Lt8ZS-WvZhuswbRxG4/edit#",
        RESULTS_INSERTION_CHUNK_SIZE to "50",
        ODK_SERVER_SUBMISSION_URL to "/submission",
        CHATBOT_ENABLED to false,
        INFO_NOTES_LOGIN to "1. Please use the same mobile number which you is registered on Prerna Portal 2. In case your number is not registered on the Prerna Portal, please get in touch with the DC in your District's BSA office 3. In case of any problem with the Nipun Lakshya app, contact the helpline number 0522-3538777.",
        ODK_FORM_QUES_LENGTH to 10,
        PROFILE_SELECTION_INFO to "1. All ARPs, SRGs, DIET mentors using the app for conducting assessments in the classroom, please select \"Assessment\" 2. Assessment data will only get recorded if the state mentor selects \"Assessment\" 3. All other users to select \"Practice\"",
        FORCE_LOGOUT_VERSION to 160000L,
        SYNC_WORKER_INTERVAL_IN_MINUTES to 10,
        METADATA_FETCH_DELTA_IN_HOURS to 23,
        SYNC_FALLBACK_CONFIG to "{\"thresholdPeriod\":0.03}",
        HELP_FAQ_FORM_URL to "https://bit.ly/NIPUNLakshyaAppIssues",
        HELP_FAQ_JSON to "[{\"ques\":\"रीड अलोंग ऐप सम्बंधित समस्या ?\",\"ans\":\"यदि आपकी 'Read Along' से सम्बंधित समस्या हैं, इस फॉर्म को भरने से पहले, सुनिश्चित करें की:\\n1.  Google PlayStore से आपने 'Read Along' ऐप को डाउनलोड कर लिया हैं\\n2. 'Read Along' ऐप को खोलकर, ऐप 'Audio' और 'Video' permissions मांगने पर 'Allow' कर दिया हैं\\n3. 'Read Along' ऐप की भाषा को अंग्रेजी से हिंदी में बदल दिया हैं (ऐप के होम स्क्रीन पर ऊपर बाईं ओर बटन पर दबाकर)\\n4. 'Read Along' ऐप पर आपने 'upprerna' पार्टनर कोड डाल दिया है (ऐप के होम स्क्रीन पर ऊपर बाईं ओर बटन पर दबाकर)\\n\"},{\"ques\":\"OTP सम्बंधित समस्या / OTP नहीं आ रहा ?\",\"ans\":\"यदि आपको OTP नहीं आ रहा है तोह कृपया ‘Helpline Form’ खोलें:\\n1. पहले पेज पर समस्या एवं सुझाव में से समस्या चुने \\n2. अगले पेज पर आयी सूची में OTP सम्बंधित समस्या चुने\\n3. आगे आने वाले सभी सवालों के उचित उत्तर फॉर्म में भरें \"},{\"ques\":\"ऐप पर सवालों के साथ चित्र बहुत छोटे आ रहे हैं\",\"ans\":\"अगर ऐप पर चित्र छोटे दिख रहे है तो कृपया ऐप डिलीट करके दोबारा playstore से डाउनलोड करें | नए version में यह समस्या सही कर दी गयी है |\"},{\"ques\":\"ऐप में स्क्रीन पर कुछ नहीं आ रहा | ऐप बार-बार बंद हो जाता है |\\n\",\"ans\":\"फ़ोन में कृपया Free Memory चेक करें | सुनिश्चित करें कि फ़ोन में Free Memory 30% से ज्यादा है | \"},{\"ques\":\"शिक्षक के फ़ोन में आंकलन किये गए छात्रों की संख्या बहुत ज्यादा आ रही है\",\"ans\":\"शिक्षक के फ़ोन में जो छात्रों की संख्या आ रही है, वह पूरे ब्लॉक के छात्रों की संख्या है | शिक्षक के ब्लॉक में कितने बच्चों का आंकलन हुआ है वह संख्या शिक्षक को प्रेषित हो रही है | मेंटर्स ने जितने विद्यार्थियों का आंकलन खुद से किया है, वही उनके ऐप पर प्रेषित किया जा रहा है |\"}]",
        INFO_NOTES_DETAIL_SELECTION to "1. अभ्यास निपुण लक्ष्य पर करना चाहते हैं तो निपुण अभ्यास चुनें।\n2. अभ्यास साप्ताहिक दक्षताओं पर करना चाहते हैं तो सूची अभ्यास चुनें।",
        ASSESSMENT_WORKFLOW_CONFIG_EXAMINER to "{\"flowConfigs\":[{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":9},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":14},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":1},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":2},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":15},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":5},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":6},{\"gradeNumber\":2,\"states\":[2],\"subject\":\"Hindi\",\"competencyId\":10},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":11},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":3},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":4},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":16},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":17},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":7},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":8},{\"gradeNumber\":3,\"states\":[2],\"subject\":\"Hindi\",\"competencyId\":12},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":13},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":18},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":19},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":20},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Math\",\"competencyId\":21},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":22},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Math\",\"competencyId\":23},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":24},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":25},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":25},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":27},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Math\",\"competencyId\":28},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":29},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":30},{\"gradeNumber\":1,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":31},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":32},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":33},{\"gradeNumber\":2,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":34},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":35},{\"gradeNumber\":3,\"states\":[1],\"subject\":\"Hindi\",\"competencyId\":36}],\"actions\":[{\"count\":0,\"futureStateId\":0,\"id\":1,\"type\":\"POP\"},{\"count\":0,\"futureStateId\":0,\"id\":2,\"type\":\"CLEAR\"},{\"count\":2,\"futureStateId\":1,\"id\":3,\"type\":\"PUSH\"}],\"states\":[{\"decision\":{\"failureActions\":[1],\"id\":1,\"successActions\":[1],\"meta\":{\"testType\":\"Count\",\"wordCount\":0}},\"gradeNumber\":1,\"id\":1,\"maxFailureCount\":3,\"stateData\":{\"successCriteria\":1,\"formID\":\"\"},\"subject\":\"Math\",\"type\":\"odk\"},{\"decision\":{\"failureActions\":[1],\"id\":2,\"successActions\":[1],\"meta\":{\"testType\":\"Fluency\",\"wordCount\":8}},\"gradeNumber\":1,\"id\":2,\"maxFailureCount\":3,\"stateData\":{\"successCriteria\":5,\"formID\":\"\"},\"subject\":\"Hindi\",\"type\":\"bolo\"}],\"chapterMapping\":[{\"grade\":2,\"subject\":\"Math\",\"competencyId\":1,\"type\":\"odk\",\"refIds\":[\"g2m_w1\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":2,\"type\":\"odk\",\"refIds\":[\"g2m_w2\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":3,\"type\":\"odk\",\"refIds\":[\"g3m_w1\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":4,\"type\":\"odk\",\"refIds\":[\"g3m_w2\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":5,\"type\":\"odk\",\"refIds\":[\"g2h_w3\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":6,\"type\":\"odk\",\"refIds\":[\"g2h_w4\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":7,\"type\":\"odk\",\"refIds\":[\"g3h_w3\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":8,\"type\":\"odk\",\"refIds\":[\"g3h_w4\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":9,\"type\":\"odk\",\"refIds\":[\"g1h_npl_5\",\"g1h_npl_1_52\",\"g1h_npl_1_53\",\"g1h_npl_1_54\",\"g1h_npl_1_55\",\"g1h_npl_1_56\",\"g1h_npl_1_57\",\"g1h_npl_1_58\",\"g1h_npl_1_59\",\"g1h_npl_1_510\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":10,\"type\":\"odk\",\"refIds\":[\"g2h_npl_1\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":10,\"type\":\"bolo\",\"refIds\":[\"upp_g2h_s1\",\"g2h_s10_v1\",\"g2h_s9_v1\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":11,\"type\":\"odk\",\"refIds\":[\"g2h_npl_2\",\"g2h_npl_2_2\",\"g2h_npl_2_3\",\"g2h_npl_2_4\",\"g2h_npl_2_5\",\"g2h_npl_2_6\",\"g2h_npl_2_7\",\"g2h_npl_2_8\",\"g2h_npl_2_9\",\"g2h_npl_2_10\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":12,\"type\":\"bolo\",\"refIds\":[\"upp_g3h_s1\",\"g3h_s2_v1\",\"g3h_s3_v1\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":13,\"type\":\"odk\",\"refIds\":[\"g3h_npl_2\",\"g3h_npl_2_2\",\"g3h_npl_2_3\",\"g3h_npl_2_4\",\"g3h_npl_2_5\",\"g3h_npl_2_6\",\"g3h_npl_2_7\",\"g3h_npl_2_8\",\"g3h_npl_2_9\",\"g3h_npl_2_10\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":14,\"type\":\"odk\",\"refIds\":[\"g1m_npl_1\",\"g1m_npl_1_2\",\"g1m_npl_1_3\",\"g1m_npl_1_4\",\"g1m_npl_1_5\",\"g1m_npl_1_6\",\"g1m_npl_1_7\",\"g1m_npl_1_8\",\"g1m_npl_1_9\",\"g1m_npl_1_10\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":15,\"type\":\"odk\",\"refIds\":[\"g2m_npl_1\",\"g2m_npl_1_2\",\"g2m_npl_1_3\",\"g2m_npl_1_4\",\"g2m_npl_1_5\",\"g2m_npl_1_6\",\"g2m_npl_1_7\",\"g2m_npl_1_8\",\"g2m_npl_1_9\",\"g2m_npl_1_10\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":16,\"type\":\"odk\",\"refIds\":[\"g3m_npl_1\",\"g3m_npl_1_2\",\"g3m_npl_1_3\",\"g3m_npl_1_4\",\"g3m_npl_1_5\",\"g3m_npl_1_6\",\"g3m_npl_1_7\",\"g3m_npl_1_8\",\"g3m_npl_1_9\",\"g3m_npl_1_10\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":17,\"type\":\"odk\",\"refIds\":[\"g3m_npl_2\",\"g3m_npl_2_2\",\"g3m_npl_2_3\",\"g3m_npl_2_4\",\"g3m_npl_2_5\",\"g3m_npl_2_6\",\"g3m_npl_2_7\",\"g3m_npl_2_8\",\"g3m_npl_2_9\",\"g3m_npl_2_10\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":18,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w5_1\",\"g1m_npl_w5_2\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":19,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w6_1\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":20,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w7_1\"]},{\"grade\":1,\"subject\":\"Math\",\"competencyId\":21,\"type\":\"odk\",\"refIds\":[\"g1m_npl_w8_1\",\"g1m_npl_w8_2\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":22,\"type\":\"odk\",\"refIds\":[\"g2m_npl_w6_1\",\"g2m_npl_w6_2\",\"g2m_npl_w6_3\",\"g2m_npl_w6_4\",\"g2m_npl_w6_5\"]},{\"grade\":2,\"subject\":\"Math\",\"competencyId\":23,\"type\":\"odk\",\"refIds\":[\"g2m_npl_w8_1\",\"g2m_npl_w8_2\",\"g2m_npl_w8_3\",\"g2m_npl_w8_4\",\"g2m_npl_w8_5\",\"g2m_npl_w8_6\",\"g2m_npl_w8_7\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":24,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w4_1\",\"g3m_npl_w4_2\",\"g3m_npl_w4_3\",\"g3m_npl_w4_4\",\"g3m_npl_w4_5\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":25,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w5_1\",\"g3m_npl_w5_2\",\"g3m_npl_w5_3\",\"g3m_npl_w5_4\",\"g3m_npl_w5_5\",\"g3m_npl_w5_6\",\"g3m_npl_w5_7\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":26,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w6_1\",\"g3m_npl_w6_2\",\"g3m_npl_w6_3\",\"g3m_npl_w6_4\",\"g3m_npl_w6_5\",\"g3m_npl_w6_6\",\"g3m_npl_w6_7\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":27,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w7_1\",\"g3m_npl_w7_2\",\"g3m_npl_w7_3\",\"g3m_npl_w7_4\",\"g3m_npl_w7_5\"]},{\"grade\":3,\"subject\":\"Math\",\"competencyId\":28,\"type\":\"odk\",\"refIds\":[\"g3m_npl_w8_1\",\"g3m_npl_w8_2\",\"g3m_npl_w8_3\",\"g3m_npl_w8_4\",\"g3m_npl_w8_5\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":29,\"type\":\"odk\",\"refIds\":[\"g1h_npl_w5_1\",\"g1h_npl_w5_2\",\"g1h_npl_w5_3\",\"g1h_npl_w5_4\",\"g1h_npl_w5_5\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":30,\"type\":\"odk\",\"refIds\":[\"g1h_npl_w6_1\",\"g1h_npl_w6_2\",\"g1h_npl_w6_3\",\"g1h_npl_w6_4\",\"g1h_npl_w6_5\"]},{\"grade\":1,\"subject\":\"Hindi\",\"competencyId\":31,\"type\":\"odk\",\"refIds\":[\"g1h_npl_w7_1\",\"g1h_npl_w7_2\",\"g1h_npl_w7_3\",\"g1h_npl_w7_4\",\"g1h_npl_w7_5\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":32,\"type\":\"odk\",\"refIds\":[\"g2h_npl_w5_1\",\"g2h_npl_w5_2\",\"g2h_npl_w5_3\",\"g2h_npl_w5_4\",\"g2h_npl_w5_5\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":33,\"type\":\"odk\",\"refIds\":[\"g2h_npl_w6_1\",\"g2h_npl_w6_2\",\"g2h_npl_w6_3\",\"g2h_npl_w6_4\",\"g2h_npl_w6_5\"]},{\"grade\":2,\"subject\":\"Hindi\",\"competencyId\":34,\"type\":\"odk\",\"refIds\":[\"g2h_npl_w7_1\",\"g2h_npl_w7_2\",\"g2h_npl_w7_3\",\"g2h_npl_w7_4\",\"g2h_npl_w7_5\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":35,\"type\":\"odk\",\"refIds\":[\"g3h_npl_w5_1\",\"g3h_npl_w5_2\",\"g3h_npl_w5_3\",\"g3h_npl_w5_4\",\"g3h_npl_w5_5\"]},{\"grade\":3,\"subject\":\"Hindi\",\"competencyId\":36,\"type\":\"odk\",\"refIds\":[\"g3h_npl_w6_1\",\"g3h_npl_w6_2\",\"g3h_npl_w6_3\"]}]}",
        GEOFENCING_CONFIG to "{\"enabled\":false,\"actors_disabled\":[1],\"geofencing_initials\":{\"fencing_radius\":500},\"location_mismatch_error_dialog\":{\"title\":\"You are far from the school\",\"description\":\"You are outside the predefined geofence of the school.\"}}",
        CHATBOT_ICON_VISIBILITY_TO_ACTOR to "{\"enabled_actors\": [1,3]}",
        TOKEN_SYNC_REPEAT_IN_DAYS to 10,
    )

    private fun initialiseFirebaseRemoteConfig(remoteConfigListener: RemoteConfigListener?): FirebaseRemoteConfig {
        val remoteConfig1 = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1)
            .build()

        remoteConfig1.setConfigSettingsAsync(configSettings)
        remoteConfig1.setDefaultsAsync(DEFAULTS)
        remoteConfig1.fetchAndActivate().addOnCompleteListener {
            Timber.d("addOnCompleteListener")
            remoteConfig1.activate()
            remoteConfigListener?.onSuccess()
            isFirebaseInitialized = true
            firebaseCallbackSuccess()
        }
        remoteConfig1.fetchAndActivate().isComplete
        remoteConfig = remoteConfig1
        return remoteConfig1
    }

    fun init(remoteConfigListener: RemoteConfigListener) {
        initialiseFirebaseRemoteConfig(remoteConfigListener)
    }

    @JvmStatic
    fun getFirebaseRemoteConfigInstance(): FirebaseRemoteConfig {
        if (remoteConfig == null) {
            synchronized(RemoteConfigUtils::class.java) {
                if (remoteConfig != null) return remoteConfig
                init(object : RemoteConfigListener {
                    override fun onFailure() {
                    }

                    override fun onSuccess() {
                    }
                })
            }
        }
        return remoteConfig
    }

    fun getPrivacyPolicyURL(): String = remoteConfig.getString(PRIVACY_POLICY_URL)


    fun firebaseCallbackSuccess() {
        for (item in firebaseCallbackList) {
            item?.onSuccess()
        }
    }
}

interface RemoteConfigListener {
    fun onSuccess()
    fun onFailure()

}

