package com.samagra.ancillaryscreens.data.network;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.ancillaryscreens.data.model.LoginRequest;
import com.samagra.ancillaryscreens.data.model.LoginResponse;
import com.samagra.ancillaryscreens.utils.BackendApiUrls;
import com.samagra.commons.constants.Constants;
import com.samagra.grove.logging.Grove;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Single;

/**
 * Solid implementation  of {@link BackendCallHelper} interface, constructs and executes the API calls
 * and returns an Observable for most functions so that the status of the calls can be observed.
 * The class maintains a singleton pattern allowing only a single instance of the class to exist at any given time.
 * This is done basically so that the class may be used outside the module without having to re-create an object.
 *
 * @author Pranav Sharma
 */
public class BackendCallHelperImpl implements BackendCallHelper {

    private static BackendCallHelperImpl backendCallHelper = null;

    private BackendCallHelperImpl() {
        // This class Cannot be initialized directly
    }

    /**
     * The method providing the singleton instance of this class. This methods automatically initiates the class
     * if it is null.
     */
    @NonNull
    public static BackendCallHelperImpl getInstance() {
        if (backendCallHelper == null)
            backendCallHelper = new BackendCallHelperImpl();
        return backendCallHelper;
    }

    @Override
    public Single<JSONObject> refreshToken(String apiKey, String refreshToken) {
        JSONObject body = new JSONObject();
        try {
            body.put("refreshToken", refreshToken);
        } catch (Throwable t) {
            Grove.e("Could not parse malformed JSON");
        }
        return Rx2AndroidNetworking.post(BackendApiUrls.REFRESH_JWT_ENDPOINT)
                .addHeaders("Authorization", apiKey)
                .addHeaders("Content-Type", "application/json")
                .setTag(Constants.LOGOUT_CALLS)
                .addJSONObjectBody(body)
                .build()
                .getJSONObjectSingle();

    }

    /**
     * This function executes the login api call using a {@link LoginRequest}. The API returns a {@link JSONObject}
     * which is first converted to a {@link LoginResponse} object and then used. Using the {@link JSONObject} directly
     * will cause an error.
     *
     * @param loginRequest - The {@link LoginRequest} object which contains relevant info required by the API. The info
     *                     from this object is first converted in {@link JSONObject} and then passed in the post request.
     * @return a {@link Single} object which receives the result of the API response and can be observed.
     * @see com.samagra.ancillaryscreens.screens.login.LoginPresenter#startAuthenticationTask(LoginRequest)
     * @see {https://fusionauth.io/docs/v1/tech/apis/login#authenticate-a-user}
     */
    @Override
    public Single<LoginResponse> performLoginApiCall(LoginRequest loginRequest) {
        return Rx2AndroidNetworking.post(BackendApiUrls.AUTH_LOGIN_ENDPOINT)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", AncillaryScreensDriver.API_KEY)
                .addJSONObjectBody(loginRequest.getLoginRequestJSONObject())
                .build()
                .getJSONObjectSingle()
                .map(jsonObject -> {
                    LoginResponse loginResponse;
                    loginResponse = new Gson().fromJson(jsonObject.toString(), LoginResponse.class);
                    return loginResponse;
                });
    }

}
