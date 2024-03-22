package com.morziz.network.models;

import java.util.ArrayList;

public class BaseListResponse<T> extends BaseResponse {


    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        this.data = data;
    }

    private ArrayList<T> data;

}
