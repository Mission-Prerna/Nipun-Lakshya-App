package com.morziz.network.models;

import com.morziz.network.network.ErrorCodesNKt;
import com.morziz.network.utils.ErrorMessagesN;

public class ErrorData {

    private String message;
    private String code;
    private Throwable t;
    private String title;
    private WarningDTO warningDTO;
    private Object data;
    private int serverState = ErrorCodesNKt.NO_SERVER_ERROR;


    public ErrorData() {
    }

    public ErrorData(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public ErrorData(String message, String code, Throwable t) {
        this.message = message;
        this.code = code;
        this.t = t;
    }

    public ErrorData(String message, String code, String title, Throwable t, int serverState) {
        this.message = message;
        this.code = code;
        this.title = title;
        this.t = t;
        this.serverState = serverState;
    }

    public ErrorData(String message, String code, Throwable t, String title, WarningDTO warningDTO) {
        this.message = message;
        this.code = code;
        this.t = t;
        this.title = title;
        this.warningDTO = warningDTO;
    }

    public ErrorData(String message, String code, Throwable t, String title, WarningDTO warningDTO,
                     Object data, int serverState) {
        this.message = message;
        this.code = code;
        this.t = t;
        this.title = title;
        this.warningDTO = warningDTO;
        this.data = data;
        this.serverState = serverState;
    }

    public String getMessage() {
        return message == null ? ErrorMessagesN.GENERIC_ERROR_MSG : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code == null ? ErrorCodesNKt.GENERIC_ERROR : code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Throwable getT() {
        return t;
    }

    public void setT(Throwable t) {
        this.t = t;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WarningDTO getWarningDTO() {
        return warningDTO;
    }

    public void setWarningDTO(WarningDTO warningDTO) {
        this.warningDTO = warningDTO;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getServerState() {
        return serverState;
    }

    public void setServerState(int serverState) {
        this.serverState = serverState;
    }
}
