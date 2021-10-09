package com.samagra.ancillaryscreens.data.model;

import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.grove.logging.Grove;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * The request object that is used while making a Login attempt. This object is required by {@link com.samagra.ancillaryscreens.data.network.BackendCallHelper#performLoginApiCall(LoginRequest)}
 * and contains the username and password fields which are the only 2 <b>required</b> fields by the fusionAuth API.
 *
 * @author Pranav Sharma
 * @see com.samagra.ancillaryscreens.data.network.BackendCallHelper#performLoginApiCall(LoginRequest)
 */
public class LoginRequest {

    private String username;
    private String password;
    private String applicationId;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.applicationId = applicationId;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public JSONObject getLoginRequestJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("loginId", username);
            jsonObject.put("applicationId", AncillaryScreensDriver.APPLICATION_ID);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Grove.d("Sending Login request for the user");
        return jsonObject;
    }
}
