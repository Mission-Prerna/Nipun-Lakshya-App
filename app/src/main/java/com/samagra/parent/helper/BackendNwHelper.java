package com.samagra.parent.helper;

import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;

import org.json.JSONObject;

import io.reactivex.Single;

public interface BackendNwHelper {

    /*
    * IN use to refresh token getting from the callback REST api.
    * */
    Single<JSONObject> refreshToken(String apiKey, CommonsPrefsHelperImpl prefs);

    /*
    * not in use
    * */
    Single<JSONObject> validateToken(String jwt);
}
