package com.samagra.workflowengine.web.model.questions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QuestionResponse {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("ver")
    @Expose
    private String ver;
    @SerializedName("ts")
    @Expose
    private String ts;
    @SerializedName("params")
    @Expose
    private Params params;
    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("result")
    @Expose
    private Result result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

}
