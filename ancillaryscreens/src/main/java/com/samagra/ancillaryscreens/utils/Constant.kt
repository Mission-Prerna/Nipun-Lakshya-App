package com.samagra.ancillaryscreens.utils

object Constant {
    //METADATA_types
    const val META_ASSESSMENT_TYPES = "meta_data_assessment_types"
    const val META_ACTORS = "meta_data_actors"
    const val META_DESIGNATIONS = "meta_data_designations"
    const val META_SUBJECTS = "meta_data_subjects"
    const val PHONE_NO: String = "phone_no"
    const val ASSESSMENT_TYPE = "assessment_type"
    const val AUTH_TOKEN_JWT: String = "auth_token_jwt"
    const val ODK_SERVER_URL: String = "odk_server_url"
    const val ODK_SERVER_SUBMISSION_URL: String = "odk_server_submission_url"
    const val COMPETENCY_DATA: String = "competency_data"
    const val QUES_LENGTH: String = "quesLength"
    const val TOKEN: String = "token"
    const val INFO_NOTES_TYPE_SELECTION = "info_notes_type_selection"
    const val INFO_NOTES_NEW_PIN = "info_notes_new_pin"
    const val PREVIOUS_META_FETCH = "previous_meta_fetch"
}

object NumberConstants {
    const val ONE_THOUSAND_LONG: Long = 1000
    const val SIXTY_THOUSAND_LONG: Long = 60000
    const val TWO: Int = 2
    const val ZERO: Int = 0
    const val ZERO_DOUBLE: Double = 0.0
    const val ONE: Int = 1
    const val FOUR: Int = 4
    const val SIX: Int = 6
}

object KeyConstants {
    const val OK: String = "OK"
    const val SUCCESS: String = "success"
    const val PIN_BOTTOM_SHEET_DATA: String = "pinBottomSheetData"
    const val PHONE_NUMBER: String = "phoneNumber"
    const val BOOK_ID_NOT_FOUND: Int = 1006
}

object TagConstants {
    const val OTP_VIEW_FRAGMENT: String = "OTPViewFragment"
    const val PIN_FRAGMENT: String = "PinFragment"
}

object ServerStatusConstants {
    const val USER_NOT_REGISTERED: String = "422"
    const val TRYING_EARLY: String = "308"
    const val OTP_INCORRECT: String = "310"
    const val OTP_NOT_EXIST: String = "311"
    const val NOT_FOUND: String = "404"
    const val SUCCESS: Int = 200
}