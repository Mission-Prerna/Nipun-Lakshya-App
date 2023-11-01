package com.samagra.commons.posthog

/***EVENTS***/
//app name-purpose (screen name (if view)+ action (if action then mention button) + click or view or summary)
const val EVENT_CREATE_PIN = "nl-loginscreen-createpin-click"
const val EVENT_LOGIN_SUCCESS = "nl-loginscreen-login-success"
const val EVENT_ODK_FORM_DOWNLOAD_LOCAL_STORAGE_FAILURE =
    "nl-user-selection-screen-download-local-storage-failure"
const val EVENT_SYNC_SERVER = "nl-dashboardscreen-syncserver-click"
const val EVENT_NEXT_STUDENT = "nl-start-assessment-nextstudent"
const val EVENT_BOLO_RESULT_SUCCESS_CALLBACK = "nl-bolo-result-success-callback"
const val EVENT_SETUP_ASSESSMENT = "nl-dashboardscreen-setupassessment-click"
const val EVENT_SCHOOL_SELECTION = "nl-schoolselectionscreen-selectschool-click"
const val EVENT_GRADE_SUBJECT_SELECTION = "nl-detailsselectionscreen-nextbutton-click"
const val EVENT_COMPETENCY_SELECTION = "nl-competencyselectionscreen-nextbutton-click"
const val EVENT_PIN_CREATION = "nl-authenticationscreen-pincreationdialog-nextbutton-click"
const val EVENT_SUBMIT_FINAL_RESULT = "nl-finalresultscreen-submit-button-click"
const val EVENT_JWT_REFRESH_TOKEN_FAILURE = "nl-splashscreen-jwtrefreshtokenapi-failure"
const val EVENT_START_WORKFLOW = "nl-start-workflow"
const val EVENT_APP_PROCESS_KILLED = "nl-app-process-killed"
const val EVENT_RA_COMPETENCY_SELECTION = "nl-GoogleRAinstructionscreen-startassessment-click"
const val EVENT_RA_FAILED = "nl-readalong-failed"
const val EVENT_STUDENT_RESULT = "nl-student-resultscreen-generated"
const val EVENT_ODK_COMPETENCY_SELECTION = "nl-odkinstructionscreen-startassessmentbutton-click"
const val EVENT_SUBMIT_RESULT = "nl-assessmentscreen-submitresult-click"
const val EVENT_FINAL_SCORE_CARD_VIEW = "nl-finalscorecardscreen-timespent-view"
const val EVENT_SELECT_PARENT = "nl-userselectionscreen-parent-click"
const val EVENT_SELECT_MENTOR = "nl-userselectionscreen-mentor-click"
const val EVENT_SELECT_TEACHER = "nl-userselectionscreen-teacher-click"
const val EVENT_SELECT_EXAMINER = "nl-userselectionscreen-examiner-click"
const val EVENT_CHATBOT_VIEW = "nl-chatbotscreen-view"
const val EVENT_CHATBOT_NOTIFICATION_RECEIVED = "nl-chatbotscreen-notification-received"
const val EVENT_CHATBOT_NOTIFICATION_OPENED = "nl-chatbotscreen-notification-opened"
const val EVENT_CHATBOT_INITIATE = "nl-assessmenttypeselection-chatbot-initiate"
const val EVENT_MIGRATION_FAILED = "nl-migration-failed"
const val EVENT_FORCE_LOGOUT = "nl-force-logout"
const val EVENT_WORKER_PROCESSING = "nl-worker-processing"
const val EVENT_DATA_MIS_MATCH = "nl-data-mis-match"
const val EVENT_CHATBOT_MEDIA_ACCESSED = "nl-chatbotscreen-chatbot_media_accessed"
const val EVENT_CHATBOT_MEDIA_VIEWED = "nl-chatbotscreen-chatbot_media_viewed"
const val EVENT_CHATBOT_MEDIA_DOWNLOADED = "nl-chatbotscreen-chatbot_media_downloaded"
const val EVENT_NOTIFICATION_RECEIVED = "nl-notification-received"
const val EVENT_STUDENT_SCREEN_MONTH_CHANGED = "nl-class-student-screen-month-changed"
const val EVENT_STUDENT_SCREEN_ASSESSMENT_STARTED = "nl-class-studentscreen-assessment-started"
const val EVENT_STUDENT_SCREEN_ANONYMOUS_ASSESSMENT_STARTED = "nl-class-student-screen-ghost-assessment-started"
const val EVENT_STUDENT_SCREEN_BACK_CLICKED = "nl-class-studentscreen-back-clicked"
const val EVENT_HOME_SCREEN_START_ASSESSMENT = "nl-homescreen-startassessment"
const val EVENT_HOME_SCREEN_SCHOOL_HISTORY = "nl-homescreen-schoolhistory-click"
const val EVENT_STUDENT_SCREEN_GRADE_SELECTED = "grade_selected"
const val EVENT_LOCATION_NOT_MATCHED = "nl-startassessment-locationnotmatched"
const val EVENT_LOCATION_MATCHED = "nl-startassessment-locationmatched"

/***EVENT TYPES***/
const val EVENT_TYPE_USER_ACTION = "User Action"
const val EVENT_TYPE_SCREEN_VIEW = "Screen View"
const val EVENT_TYPE_SUMMARY = "Summary"
const val EVENT_TYPE_SYSTEM = "System"

/***Event Id***/
const val EID_INTERACT = "INTERACT"
const val EID_IMPRESSION = "IMPRESSION"
const val EID_SYSTEM = "SYSTEM"

/*Page or Screens*/
const val DASHBOARD_SCREEN = "nipunlakshyaapp-dashboard-screen"
const val SCHOOLS_SELECTION_SCREEN = "nipunlakshyaapp-school-selection-screen"
const val DETAILS_SELECTION_SCREEN = "nipunlakshyaapp-details-selection-screen"
const val COMPETENCY_SELECTION_SCREEN = "nipunlakshyaapp-competency-selection-screen"
const val READ_ONLY_COMPETENCY_SCREEN = "nipunlakshyaapp-read-only-competency-screen"
const val RA_INSTRUCTION_SCREEN = "nipunlakshyaapp-GoogleRA-instruction-screen"
const val ODK_INSTRUCTION_SCREEN = "nipunlakshyaapp-ODK-form-screen"
const val ODK_INDIVIDUAL_RESULT_SCREEN = "nipunlakshyaapp-ODK-individual-result-screen"
const val INDIVIDUAL_NL_RESULT_SCREEN = "nipunlakshyaapp-individual-nl-result-screen"
const val ASSESSMENT_COMPLETE_SCREEN = "nipunlakshyaapp-assessment-screen"
const val FINAL_SCORECARD_SCREEN = "nipunlakshyaapp-final-scorecard-screen"
const val STUDENT_SCORECARD_SCREEN = "nipunlakshyaapp-student-scorecard-screen"
const val USERSELECTION_SCREEN = "nipunlakshyaapp-userselection-screen"
const val SCHOOL_HISTORY_SCREEN = "nipunlakshyaapp-schoolhistory-screen"
const val ASSESSMENT_FLOW_SCREEN = "nipunlakshyaapp-assessmentflow-screen"
const val LOGIN_SCREEN = "nipunlakshyaapp-login-screen"
const val PIN_CREATION_DIALOG = "nipunlakshyaapp-pin-creation-dialog"
const val CHATBOT_SCREEN = "nipunlakshyaapp-chatbot-screen"
const val ASSESSMENT_TYPE_SELECTION_SCREEN = "nipunlakshyaapp-assessment-type-selection-screen"
const val SPLASH_SCREEN = "splash-screen"
const val SYNC_WORKER = "sync-worker"
const val STUDENT_SELECTION_SCREEN = "nipunlakshyaapp-student-selection-screen"
const val EXAMINER_SELECTION_SCREEN = "nipunlakshyaapp-examiner-selection-screen"

/*CONTEXT CHANNEL (ANDROID)*/
const val CONTEXT_CHANNEL = "nipunlakshaya-android-app"

/*CONTEXT P-DATA ID (Application id)*/
const val APP_ID = "org.samagra.missionPrerna"

/*CONTEXT P-DATA P-ID (app + screen)*/
const val NL_APP_LOGIN = "nlapp-login"
const val NL_APP_DASHBOARD = "nlapp-dashboard"
const val NL_APP_SCHOOL_SELECTION = "nlapp-schoolselection"
const val NL_APP_DETAIL_SELECTION = "nlapp-detailsselection"
const val NL_APP_COMPETENCY_SELECTION = "nlapp-competencyselection"
const val NL_APP_PIN_CREATION = "nlapp-pincreation"
const val NL_APP_READ_ONLY_COMPETENCY = "nlapp-readonlycompetency"
const val NL_APP_INDIVIDUAL_RESULT = "nlapp-individualresult"
const val NL_APP_RA_INSTRUCTION = "nlapp-rainstruction"
const val NL_APP_STUDENT_SCORECARD = "nlapp-student-scorecard"
const val NL_APP_ODK_INSTRUCTION = "nlapp-odkinstruction"
const val NL_APP_COMPLETE_ASSESSMENT = "nlapp-endassessment"
const val NL_APP_FINAL_RESULT = "nlapp-finalresult"
const val NL_APP_SPLASH_SCREEN = "nlapp-splashscreen"
const val NL_APP_USER_SELECTION = "nlapp-userselection"
const val NL_APP_SCHOOL_HISTORY = "nlapp-schoolhistory"
const val NL_APP_ASSESSMENT_TYPE_SELECTION = "nlapp-assessmenttypeselection"
const val NL_APP_CHATBOT = "nlapp-chatbot"
const val NL_APP_DB_MIGRATION = "nlapp-db-migration"
const val NL_APP_FORCE_LOGOUT = "nlapp-force-logout"
const val NL_APP_SYNC_WORKER = "nlapp-sync-worker "
const val DATA_MIS_MATCH = "data-mis-match"
const val NL_APP_STUDENT_SELECTION = "nlapp-studentselection"

/*E-data type*/
const val TYPE_CLICK = "click"
const val TYPE_VIEW = "view"
const val TYPE_SUMMARY = "summary"
const val TYPE_SYSTEM_EXCEPTION = "exception"

/*E-data Page ID (NL + module)*/
const val NL_LOGIN = "nl-login"
const val NL_PIN_CREATION = "nl-pincreation"
const val NL_DASHBOARD = "nl-dashboard"
const val NL_SCHOOL_SELECTION = "nl-schoolselection"
const val NL_DETAIL_SELECTION = "nl-detailsselection"
const val NL_COMPETENCY_SELECTION = "nl-competencyselection"
const val NL_READ_ONLY_COMPETENCY = "nl-readonlycompetency"
const val NL_FINAL_RESULT = "nl-finalresult"
const val NL_SPLASH_SCREEN = "nl-splashscreen"
const val NL_SPOT_ASSESSMENT = "nl-spotassessment"
const val NL_USERSELECTION = "nl-userselection"
const val NL_SCHOOL_HISTORY = "nl-schoolhistory"
const val NL_CHATBOT = "nl-chatbot"
const val NL_ASSESSMENT_TYPE_SELECTION = "nl-assessmenttypeselection"

/*Object id*/
const val SUBMIT_FINAL_RESULT_BUTTON = "submit-final-button"
const val SYNC_SERVER_BUTTON = "syncserver-button"
const val SETUP_ASSESSMENT_BUTTON = "setupassessment-button"
const val SELECT_SCHOOL_BUTTON = "selectschool-button"
const val SELECT_DETAILS_BUTTON = "selectdetails-next-button"
const val START_ASSESSMENT_NEXT_BUTTON = "start-assessment-next-button"
const val CREATE_PIN_NEXT_BUTTON = "create-pin-next-button"
const val WORKFLOW_START = "workflow-start"
const val STORE_RESULT_ON_PROCESS_KILL = "store-result-on-app-process-kill"
const val NEXT_STUDENT_ASSESSMENT_BUTTON = "next-student-assessment-button"
const val BOLO_RESULT_CALLBACK = "bolo-result-callback"
const val BOLO_START_ASSESSMENT_BUTTON = "bolo-startassessment-button"
const val BOLO_CANCELLED_BUTTON = "bolo-cancelled-button"
const val ODK_START_ASSESSMENT_BUTTON = "odk-startassessment-button"
const val SUBMIT_RESULT_BUTTON = "submitresult-button"
const val TEACHER_BUTTON = "teacher-button"
const val PARENT_BUTTON = "parent-button"
const val MENTOR_BUTTON = "mentor-button"
const val EXAMINER_BUTTON = "examiner-button"
const val BOT_INITIATION_BUTTON = "botinitiation-button"


/*Object type*/
const val OBJ_TYPE_UI_ELEMENT = "ui-element"

const val PRODUCT = "Nipun Lakshya App"

const val POST_HOG_LOG_TAG = "--posthog"













