package com.morziz.network.models;

public class BaseDataResponse<T> extends BaseResponse {


    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
