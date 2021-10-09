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
    public static String UPDATE_PASSWORD_URL;
    public static MainApplication mainApplication = null;
    public static String BASE_API_URL = "";
    public static String APPLICATION_ID = "";
    public static String API_KEY = "";
    public static String USER_ID = "";

    /**
     *
     * @param mainApplication MainApplication Instance
     */
    public static void init(@NonNull MainApplication mainApplication,String baseAPIURL) {
        AncillaryScreensDriver.mainApplication = mainApplication;
        AncillaryScreensDriver.BASE_API_URL=baseAPIURL;
    }


    /**
     * This function receives the User Object as {@link JSONObject} and removes the FCM Token from it.
     *
     * @param jsonObject - The user JSON Object from the backend APIs.
     * @return user object as {@link JSONObject} with empty FCM Token.
     */
    private static JSONObject removeFCMTokenFromObject(JSONObject jsonObject) {
        Grove.d("Removing FCM Token from %s", jsonObject.toString());
        try {
            JSONObject user = jsonObject.getJSONObject("user");
            JSONObject data = user.getJSONObject("data");
            data.put("FCM.token", "");
            user.put("data", data);
            jsonObject.put("user", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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
