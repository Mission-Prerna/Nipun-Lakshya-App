package com.samagra.workflowengine.web.model.questions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Params {

    @SerializedName("resmsgid")
    @Expose
    private String resmsgid;
    @SerializedName("msgid")
    @Expose
    private Object msgid;
    @SerializedName("err")
    @Expose
    private Object err;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("errmsg")
    @Expose
    private Object errmsg;

    public String getResmsgid() {
        return resmsgid;
    }

    public void setResmsgid(String resmsgid) {
        this.resmsgid = resmsgid;
    }

    public Object getMsgid() {
        return msgid;
    }

    public void setMsgid(Object msgid) {
        this.msgid = msgid;
    }

    public Object getErr() {
        return err;
    }

    public void setErr(Object err) {
        this.err = err;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(Object errmsg) {
        this.errmsg = errmsg;
    }

}
