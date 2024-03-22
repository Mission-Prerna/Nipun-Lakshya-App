package com.morziz.network.utils.butler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Monika on 18/01/17.
 */

public class ActionDTO {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("label")
    @Expose
    public String label;

    @SerializedName("type")
    @Expose
    public String type;

    @SerializedName("url")
    @Expose
    public String url;

    @SerializedName("enabled")
    @Expose
    public boolean enabled;

    @SerializedName("popupData")
    @Expose
    public PopupDataDTO popupData;

    @SerializedName("requestBody")
    @Expose
    public String requestBody; // stringed json, parse into RequestBody of particular api call

    @SerializedName("queryParams")
    @Expose
    public String queryParams; // string, query parameters to be added in the url of particular api call

    @SerializedName("buttonType")
    @Expose
    public String buttonType; // decides the style of the action button

    public ActionDTO(String id, String label, boolean enabled, String type, String actionUrl) {
        this.id = id;
        this.label = label;
        this.enabled = enabled;
        this.type = type;
        this.url = actionUrl;
    }

    public ActionDTO(){

    }
}
