package com.samagra.ancillaryscreens.data.model;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The response of the Login API is wrapped in this class prior to use anywhere in code.
 *
 * @author Pranav Sharma
 * @see com.samagra.ancillaryscreens.data.network.BackendCallHelperImpl#performLoginApiCall(LoginRequest)
 */
public class LoginResponse {

    public JsonPrimitive token;
    public JsonPrimitive refreshToken;
    public JsonObject user;

    public String getUserName() {
        return this.user.get("registrations")
                .getAsJsonArray().get(0).getAsJsonObject()
                .get("username").getAsString();
    }

    @NonNull
    @Override
    public String toString() {
        return "token : " + token + " user " + user.toString();
    }
}

