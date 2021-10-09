package com.samagra.grove.contracts;


import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.samagra.grove.logging.Grove;
import com.samagra.grove.logging.GroveUtils;
import com.samagra.grove.logging.LoggableApplication;
import com.samagra.grove.hyperlog.HyperLog;

import org.json.JSONObject;

import java.io.File;

public class GroveLoggingComponentLauncher implements IGroveLoggingComponent {

    @Override
    public void initializeLoggingComponent(
            Application application,
            LoggableApplication applicationInstance, Context context,
            ErrorActivityHandler errorActivityHandler,
            boolean isUCEHEnabled,
            boolean isHyperlogEnabled,
            String senderEmailID,
            String receiverEmailID
    ) {
        Grove.init(applicationInstance, context, isHyperlogEnabled);
    }


    @Override
    public void uploadLogFile(String apiURL, final Context context, String authToken, boolean isOverrideMethod, OverrideUploadFileCallback overrideUploadFileCallback) {
        File file = HyperLog.getDeviceLogsInFile(context);
        if (!isOverrideMethod) {
            if (!TextUtils.isEmpty(apiURL) && !TextUtils.isEmpty(authToken)) {
                Rx2AndroidNetworking.upload(apiURL)
                        .addHeaders("Authorization", "Bearer " + authToken)
                        .addMultipartFile("data", file)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                clearEarlierLogList(context);
                                Grove.d("Logs have been successfully pushed with API Response: " + response.toString());
                            }

                            @Override
                            public void onError(ANError error) {
                                Grove.e("Logs were not able to be sent to the Server with error received: " + error.getResponse().toString());
                            }
                        });
            } else {
                Grove.e("Please check the API URL or the auth token for sending the app logs is empty or null");
            }
        } else {
            overrideUploadFileCallback.sendAppLogsToServer(file);
            clearEarlierLogList(context);
        }
    }

    private void clearEarlierLogList(Context context) {
        if (context != null) {
            HyperLog.deleteLogList();
        }
    }

    @Override
    public void setAppUserName(String userName) {
        if (TextUtils.isEmpty(userName)) userName = "";
        GroveUtils.setUserName(userName);
    }

    @Override
    public void setAppUserData(String userData) {
        if (TextUtils.isEmpty(userData)) userData = "";
        GroveUtils.setUserData(userData);
    }
}