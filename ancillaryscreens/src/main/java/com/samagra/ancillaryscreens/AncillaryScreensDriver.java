package com.samagra.ancillaryscreens;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.samagra.commons.CommonUtilities;
import com.samagra.commons.MainApplication;
import com.samagra.grove.logging.Grove;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The driver class for this module, any screen that needs to be launched from outside this module, should be
 * launched using this class.
 * Note: It is essential that you call the {@link AncillaryScreensDriver#init(MainApplication,String)} to initialise
 * the class prior to using it else an {@link InvalidConfigurationException} will be thrown.
 *
 * @author Pranav Sharma
 */
public class AncillaryScreensDriver {
    public static MainApplication mainApplication = null;
    public static String BASE_API_URL = "";
    public static String APPLICATION_ID = "";
    public static String API_KEY = "";

    /**
     *
     * @param mainApplication MainApplication Instance
     */
    public static void init(@NonNull MainApplication mainApplication,String baseAPIURL) {
        AncillaryScreensDriver.mainApplication = mainApplication;
        AncillaryScreensDriver.BASE_API_URL=baseAPIURL;
    }


    /**
     * Function to check if the mainApplication is initialised indicating if {@link AncillaryScreensDriver#init(MainApplication,String)} is called or not.
     * If not, it throws {@link InvalidConfigurationException}
     *
     * @throws InvalidConfigurationException - This Exception means that the module is not configured by the user properly. The exception generates
     *                                       detailed message depending on the class that throws it.
     */
    private static void checkValidConfig() {
        if (mainApplication == null)
            throw new InvalidConfigurationException(AncillaryScreensDriver.class);
    }


    public static void launchErrorScreen(Context context, Intent intent) {
        checkValidConfig();
        CommonUtilities.startActivityAsNewTask(intent, context);
    }
}
