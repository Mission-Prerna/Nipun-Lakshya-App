package com.morziz.network.network;


import com.morziz.network.models.ErrorData;

/**
 * Created by rohitsingh on 13/06/16.
 */
public class ApiFailure {
    public String errorCode;
    public String message;
    public Object data;
    public String title;
    public ErrorData errorData;

    public ApiFailure(String errorCode, String message, String title, ErrorData errorData) {
        this.errorCode = errorCode;
        this.message = message;
        this.title = title;
        this.errorData = errorData;
    }

    public ApiFailure(String errorCode, String message, String title, Object data, ErrorData errorData) {
        this.errorCode = errorCode;
        this.message = message;
        this.title = title;
        this.data = data;
        this.errorData = errorData;
    }
}
