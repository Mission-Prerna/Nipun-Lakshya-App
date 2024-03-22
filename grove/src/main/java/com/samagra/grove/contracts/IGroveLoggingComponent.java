package com.samagra.grove.contracts;

import android.app.Application;
import android.content.Context;

import com.samagra.grove.logging.LoggableApplication;

public interface IGroveLoggingComponent {

    /**
     * @param application          App Instance - {@link Application}
     * @param applicationInstance  Loggable Application Interface object - {@link LoggableApplication}
     * @param context              Context Instance - {@link Context}
     * @param errorActivityHandler Interface object to communicate back to main app {@link ErrorActivityHandler}
     * @param isUCEHEnabled        Flag to control the initialisation of UncaughtExceptionHandler Object - {@link Boolean}
     * @param isHyperlogEnabled    Flag to control the initialisation of Hyperlog to store App logs in local device - {@link Boolean}
     * @param senderEmailID        User E-mail ID to send the logs - {@link String}
     * @param receiverEmailID      User E-mail ID to send the logs - {@link String}
     */
    void initializeLoggingComponent(
            Application application,
            LoggableApplication applicationInstance, Context context,
            ErrorActivityHandler errorActivityHandler,
            boolean isUCEHEnabled,
            boolean isHyperlogEnabled,
            String senderEmailID,
            String receiverEmailID
    );

    /**
     * This method sends the App Log Files to the Back-end Server
     * @param apiURL - Backend API URL - {@link String}
     * @param context - Context Instance - {@link Context}
     * @param token - API Authentication Token - {@link String}
     */
    void uploadLogFile(String apiURL, Context context, String token , boolean isOverrideMethod, OverrideUploadFileCallback  overrideUploadFileCallback);

    /**
     * Set App User name
     * @param userName {@link String}
     */
    void setAppUserName(String userName);

    /**
     * Set App user data.
     * @param userData {@link String}
     */
    void setAppUserData(String userData);
}