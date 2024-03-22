package com.morziz.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ankitmaheswari on 24/08/17.
 */

public class WarningDTO {

    @SerializedName("warningCode")
    @Expose
    public String code;

    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("actions")
    @Expose
    public List<PopupActionDTO> actionDTOs;
}
