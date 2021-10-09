package com.samagra.ancillaryscreens.data.network;

import com.samagra.ancillaryscreens.data.model.LoginRequest;
import com.samagra.ancillaryscreens.data.model.LoginResponse;

import org.json.JSONObject;

import io.reactivex.Single;

/**
 * Interface containing all the API Calls performed by this module.
 * All calls to be implemented in a single implementation of this interface.
 *
 * @author Pranav Sharma
 * @see BackendCallHelperImpl
 */
public interface BackendCallHelper {

    Single<JSONObject> refreshToken(String apiKey, String refreshToken);

    Single<LoginResponse> performLoginApiCall(LoginRequest loginRequest);

}
