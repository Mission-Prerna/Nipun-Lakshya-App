package com.samagra.parent.ui.splash;

import org.json.JSONObject;

interface JwtResponseListener {
    void onSuccess(JSONObject updatedToken);
    void onFailure();
}
