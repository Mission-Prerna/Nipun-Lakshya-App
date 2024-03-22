package com.morziz.network.utils.butler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Monika on 18/01/17.
 */

public class PopupDataDTO {
    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("actions")
    @Expose
    public List<ActionDTO> actions;

    public PopupDataDTO(String title, String message, List<ActionDTO> actions) {
        this.title = title;
        this.message = message;
        this.actions = actions;
    }
}
