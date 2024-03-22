package com.samagra.workflowengine.web.model.questions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseDeclaration {

    @SerializedName("response1")
    @Expose
    private Response1 response1;

    public Response1 getResponse1() {
        return response1;
    }

    public void setResponse1(Response1 response1) {
        this.response1 = response1;
    }

}
