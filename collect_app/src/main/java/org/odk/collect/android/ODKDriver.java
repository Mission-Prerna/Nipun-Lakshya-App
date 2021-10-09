package org.odk.collect.android;

import android.content.Context;

import com.samagra.commons.MainApplication;

import java.util.HashMap;

public class ODKDriver {
    public static final String LAUNCH_INTENT_ACTION = "org.samagra.missionprerna.START_ODK_APP";
    private static int splashScreenImageRes = R.drawable.notes;
    private static boolean isUsingCustomTheme = false;
    private static int customThemeId = -1;
    private static int customThemeId_FormEntry = -1;
    private static int customThemeId_Settings = -1;
    private static long toolbarIconResId = -1;
    private static boolean modifyToolbarIcon = false;
    public static MainApplication applicationInstance = null;

    public static void init(MainApplication applicationInstance, int splashScreenImageRes) {
        ODKDriver.applicationInstance = applicationInstance;
        ODKDriver.splashScreenImageRes = splashScreenImageRes;
    }

    public static void init(MainApplication applicationInstance, int splashScreenImageRes, int customThemeId, int customThemeId_FormEntry, int customThemeId_Settings) {
        isUsingCustomTheme = true;
        ODKDriver.customThemeId = customThemeId;
        ODKDriver.customThemeId_FormEntry = customThemeId_FormEntry;
        ODKDriver.customThemeId_Settings = customThemeId_Settings;
        init(applicationInstance, splashScreenImageRes);
    }

    public static void init(MainApplication applicationInstance, int splashScreenImageRes,
                            int customThemeId, int customThemeId_FormEntry, int customThemeId_Settings, long toolbarIconResId) {
        init(applicationInstance, splashScreenImageRes, customThemeId, customThemeId_FormEntry, customThemeId_Settings);
        modifyToolbarIcon = true;
        ODKDriver.toolbarIconResId = toolbarIconResId;
    }

    public static void launchInstanceUploaderListActivity(Context context, HashMap<String, Object> extras) {
//        Intent intent = new Intent(context, InstanceUploaderListActivity.class);
//        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
//        intent.putExtra(Constants.KEY_CUSTOMIZE_TOOLBAR, extras);
//        intent.putIntegerArrayListExtra(Constants.CUSTOM_TOOLBAR_ARRAYLIST_HIDE_IDS, null);
//        context.startActivity(intent);
    }

    public static int getSplashScreenImageRes() {
        return ODKDriver.splashScreenImageRes;
    }

    public static boolean isIsUsingCustomTheme() {
        return isUsingCustomTheme;
    }

    public static int getCustomThemeId() {
        return customThemeId;
    }

    public static int getCustomThemeId_FormEntry() {
        return customThemeId_FormEntry;
    }

    public static int getCustomThemeId_Settings() {
        return customThemeId_Settings;
    }

    public static long getToolbarIconResId() {
        return toolbarIconResId;
    }

    public static boolean isModifyToolbarIcon() {
        return modifyToolbarIcon;
    }

}
