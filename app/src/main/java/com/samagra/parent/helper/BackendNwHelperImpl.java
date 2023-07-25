package com.samagra.parent.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.androidnetworking.AndroidNetworking;
import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.commons.constants.Constants;
import com.samagra.grove.logging.Grove;
import com.samagra.parent.BuildConfig;
import com.samagra.parent.MyApplication;

import org.json.JSONObject;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

public class BackendNwHelperImpl implements BackendNwHelper {

    private static BackendNwHelperImpl backendCallHelper = null;

    private BackendNwHelperImpl() {
        // This class Cannot be initialized directly
        Context context = MyApplication.getInstance();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(new ChuckerInterceptor(context))
                .build();
        AndroidNetworking.initialize(context, okHttpClient);
    }

    /**
     * The method providing the singleton instance of this class. This methods automatically initiates the class
     * if it is null.
     */
    @NonNull
    public static BackendNwHelperImpl getInstance() {
        if (backendCallHelper == null)
            backendCallHelper = new BackendNwHelperImpl();
        return backendCallHelper;
    }

    static final String REFRESH_JWT_ENDPOINT = BuildConfig.REFRESH_JWT_BASE + "/api/jwt/refresh";
    static final String VALIDATE_ENDPOINT = AncillaryScreensDriver.BASE_API_URL + "/api/jwt/validate";

    @Override
    public Single<JSONObject> refreshToken(String apiKey, CommonsPrefsHelperImpl prefs) {
        JSONObject body = new JSONObject();
        try {
            body.put("token", prefs.getAuthToken());
            body.put("refreshToken", prefs.getRefreshToken());
        } catch (Throwable t) {
            Grove.e("Could not parse malformed JSON");
        }
        return Rx2AndroidNetworking.post(REFRESH_JWT_ENDPOINT)
//                .addHeaders("Authorization", apiKey)
                .addHeaders("Content-Type", "application/json")
//                .setTag(Constants.LOGOUT_CALLS)
                .addJSONObjectBody(body)
                .build()
                .getJSONObjectSingle();

    }

    @Override
    public Single<JSONObject> validateToken(String jwt) {
        return Rx2AndroidNetworking.get(VALIDATE_ENDPOINT)
                .addHeaders("Authorization", "JWT " + jwt)
                .addHeaders("Content-Type", "application/json")
                .setTag(Constants.LOGOUT_CALLS)
                .build()
                .getJSONObjectSingle();

    }

}