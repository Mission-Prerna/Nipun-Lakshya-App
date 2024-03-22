package com.morziz.network.network;

import android.content.Context;

import com.morziz.network.models.ApiResult;
import com.morziz.network.models.ErrorData;
import com.morziz.network.utils.ErrorMessagesN;
import com.morziz.network.utils.NetworkUtils;

import java.io.IOException;
import java.net.SocketException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rohitsingh on 27/05/16.
 */

/*
* Use ApiCallback for network call
     onApiSuccess - if statusCode is between 200 && 400 and isSuccess true from server api call
     onApiFailure - if isSuccess false from server api call
      onError - if onFailure if retrofit onFailure is Called
* */
public abstract class ApiCallback<T extends ApiResult> implements Callback<T> {

    public static final String STATUS_SUCCESS = "Success",
            STATUS_FAILURE = "Failure",
            STATUS_ERROR = "Error";

    public static final int logOnError = 0x1;
    public static final int logOnSuccess = 0x2;
    public static final int logOnFailure = 0x4;
    public static final int logAll = logOnError | logOnSuccess | logOnFailure;
    public static final String TAG = ApiCallback.class.getSimpleName();
    private final Context context;
    private String eventName;
    private int flags;

    public ApiCallback(Context context, String eventName, int flags) {
        this.context = context;
        this.eventName = eventName;
        this.flags = flags;
    }

    public ApiCallback(Context context) {
        this.context = context;
    }

    public String getEventName() {
        return eventName;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (NetworkUtils.isCallSuccess(getContext(), response)) {
            ApiResult baseApiResponse = response.body();
            if (baseApiResponse.getSuccess()) {
                onApiSuccess(response.body());
            } else {
                callApiFailure(response.body());
            }

        } else {

            callApiFailure(String.valueOf(response.code()), ErrorMessagesN.GENERIC_ERROR_MSG,
                    ErrorMessagesN.GENERIC_ERROR_TITLE, new ErrorData(ErrorMessagesN.GENERIC_ERROR_MSG,
                            String.valueOf(response.code()), null, ErrorMessagesN.GENERIC_ERROR_TITLE, null));
        }
    }


    private void callApiFailure(ApiResult baseApiResponse) {
        onApiFailure(new ApiFailure(baseApiResponse.getErrorCode(), baseApiResponse.getMessage(),
                baseApiResponse.getTitle(), baseApiResponse.getData(),
                new ErrorData(baseApiResponse.getMessage(), baseApiResponse.getErrorCode(),
                        null, baseApiResponse.getTitle(), baseApiResponse.getWarningDTO())));
    }

    private void callApiFailure(String errorCode, String errorMessage, String errorTitle, ErrorData errorData) {
        onApiFailure(new ApiFailure(errorCode, errorMessage, errorTitle, errorData));
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String errorMessage = ErrorMessagesN.GENERIC_ERROR_MSG;

        if (t instanceof SocketException) {
            errorMessage = ErrorMessagesN.REQUEST_TIMED_OUT;
        } else if (t instanceof IOException) {
            errorMessage = ErrorMessagesN.NETWORK_ISSUE;
        }

        onError(call, t, errorMessage);
    }


    public abstract void onApiSuccess(T t);

    public abstract void onApiFailure(ApiFailure apiFailure);

    public abstract void onError(Call<T> call, Throwable t, String errorMessage);

}


