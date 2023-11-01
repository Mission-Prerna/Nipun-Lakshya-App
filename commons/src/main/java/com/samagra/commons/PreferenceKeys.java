package com.samagra.commons;

import java.util.HashMap;

public final class PreferenceKeys {

    // aggregate_preferences.xml
    public static final String KEY_USERNAME                 = "username";
    public static final String KEY_PASSWORD                 = "password";

    // form_management_preferences.xml
    public static final String KEY_AUTOSEND                 = "autosend";
    public static final String KEY_DELETE_AFTER_SEND        = "delete_send";
    public static final String KEY_COMPLETED_DEFAULT        = "default_completed";
    public static final String KEY_CONSTRAINT_BEHAVIOR      = "constraint_behavior";
    public static final String KEY_HIGH_RESOLUTION          = "high_resolution";
    public static final String KEY_IMAGE_SIZE               = "image_size";
    public static final String KEY_GUIDANCE_HINT            = "guidance_hint";
    public static final String KEY_INSTANCE_SYNC            = "instance_sync";
    public static final String KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check";
    public static final String KEY_AUTOMATIC_UPDATE         = "automatic_update";
    public static final String KEY_HIDE_OLD_FORM_VERSIONS   = "hide_old_form_versions";
    public static final String KEY_BACKGROUND_LOCATION      = "background_location";

    // form_metadata_preferences.xml
    public static final String KEY_METADATA_USERNAME        = "metadata_username";
    public static final String KEY_METADATA_PHONENUMBER     = "metadata_phonenumber";
    public static final String KEY_METADATA_EMAIL           = "metadata_email";

    // google_preferences.xml
    public static final String KEY_SELECTED_GOOGLE_ACCOUNT  = "selected_google_account";
    public static final String KEY_GOOGLE_SHEETS_URL        = "google_sheets_url";
    public static final String TITLE                        = "title";

    // identity_preferences.xml
    public static final String KEY_ANALYTICS                = "analytics";

    // other_preferences.xml
    public static final String KEY_FORMLIST_URL             = "formlist_url";
    public static final String KEY_SUBMISSION_URL           = "submission_url";

    // server_preferences.xml
    public static final String KEY_PROTOCOL                 = "protocol";
    public static final String KEY_SMS_GATEWAY              = "sms_gateway";
    public static final String KEY_SUBMISSION_TRANSPORT_TYPE = "submission_transport_type";
    public static final String KEY_TRANSPORT_PREFERENCE      = "submission_transport_preference";
    public static final String KEY_SMS_PREFERENCE            = "sms_preference";

    // user_interface_preferences.xml
    public static final String KEY_APP_THEME                = "appTheme";
    public static final String KEY_APP_LANGUAGE             = "app_language";
    public static final String KEY_FONT_SIZE                = "font_size";
    public static final String KEY_NAVIGATION               = "navigation";
    public static final String KEY_SHOW_SPLASH              = "showSplash";
    public static final String KEY_SPLASH_PATH              = "splashPath";
    public static final String KEY_MAP_SDK                  = "map_sdk_behavior";
    public static final String KEY_MAP_BASEMAP              = "map_basemap_behavior";

    // other keys
    public static final String KEY_LAST_VERSION             = "lastVersion";
    public static final String KEY_FIRST_RUN                = "firstRun";
    // values
    public static final String NAVIGATION_SWIPE             = "swipe";
    public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
    public static final String NAVIGATION_BUTTONS           = "buttons";
    private static final String AUTOSEND_OFF                = "off";
    private static final String GUIDANCE_HINT_OFF           = "no";

    // These values match those in map_sdk_selector_entry_values.
    public static final String GOOGLE_MAPS_BASEMAP_KEY      = "google_maps";
    public static final String OSM_BASEMAP_KEY              = "osmdroid";
    public static final String MAPBOX_BASEMAP_KEY           = "mapbox_maps";
    public static final String DEFAULT_BASEMAP_KEY = GOOGLE_MAPS_BASEMAP_KEY;

    public static final String GOOGLE_MAPS_BASEMAP_DEFAULT  = "streets";

    public static final String OSM_MAPS_BASEMAP_DEFAULT     = "openmap_streets";

    // These values match those in map_mapbox_basemap_selector_entry_values.
    public static final String MAPBOX_MAP_STREETS           = "mapbox_streets";
    public static final String MAPBOX_MAP_LIGHT             = "mapbox_light";
    public static final String MAPBOX_MAP_DARK              = "mapbox_dark";
    public static final String MAPBOX_MAP_SATELLITE         = "mapbox_satellite";
    public static final String MAPBOX_MAP_SATELLITE_STREETS = "mapbox_satellite_streets";
    public static final String MAPBOX_MAP_OUTDOORS          = "mapbox_outdoors";
    public static final String MAPBOX_BASEMAP_DEFAULT       = "mapbox_streets";

    private static HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        // aggregate_preferences.xml
        hashMap.put(KEY_USERNAME,                   "");
        hashMap.put(KEY_PASSWORD,                   "");
        // form_management_preferences.xml
        hashMap.put(KEY_AUTOSEND,                   AUTOSEND_OFF);
        hashMap.put(KEY_GUIDANCE_HINT,              GUIDANCE_HINT_OFF);
        hashMap.put(KEY_DELETE_AFTER_SEND,          false);
        hashMap.put(KEY_COMPLETED_DEFAULT,          true);
        hashMap.put(KEY_CONSTRAINT_BEHAVIOR,        CONSTRAINT_BEHAVIOR_ON_SWIPE);
        hashMap.put(KEY_HIGH_RESOLUTION,            true);
        hashMap.put(KEY_IMAGE_SIZE,                 "original_image_size");
        hashMap.put(KEY_INSTANCE_SYNC,              true);
        hashMap.put(KEY_PERIODIC_FORM_UPDATES_CHECK, "never");
        hashMap.put(KEY_AUTOMATIC_UPDATE,           false);
        hashMap.put(KEY_HIDE_OLD_FORM_VERSIONS,     true);
        hashMap.put(KEY_BACKGROUND_LOCATION,        true);
        // form_metadata_preferences.xml
        hashMap.put(KEY_METADATA_USERNAME,          "");
        hashMap.put(KEY_METADATA_PHONENUMBER,       "");
        hashMap.put(KEY_METADATA_EMAIL,             "");
        // google_preferences.xml
        hashMap.put(KEY_SELECTED_GOOGLE_ACCOUNT,    "");
        hashMap.put(KEY_GOOGLE_SHEETS_URL,          "");
        // identity_preferences.xml
        hashMap.put(KEY_ANALYTICS,                  true);
        // other_preferences.xml
        return hashMap;
    }

    public static final HashMap<String, Object> GENERAL_KEYS = getHashMap();

    private PreferenceKeys() {

    }

    public static final String CHAT_BOT_LIST = "CHAT_BOT_LIST";
    public static final String CHAT_BOT_HISTORY = "CHAT_BOT_HISTORY";
    public static final String CHAT_BOT_STARRED_MSGS = "CHAT_BOT_STARRED_MSGS";

    public static final String CHAT_BOT_DETAILS = "CHAT_BOT_DETAILS";

    public static final String LAST_FORCE_LOGOUT_VERSION = "FORCE_LOGOUT_VERSION";
    public static final String VERSION_CODE = "version_code";
    public static final String VERSION_NAME = "version_name";
}
