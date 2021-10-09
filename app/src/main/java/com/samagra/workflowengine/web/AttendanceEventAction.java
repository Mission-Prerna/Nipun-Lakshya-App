package com.samagra.workflowengine.web;

public class AttendanceEventAction {

    private WebEvent event;
    private Object data;

    public AttendanceEventAction(WebEvent event) {
        this.event = event;
    }

    public AttendanceEventAction(WebEvent event, Object data) {
        this.event = event;
        this.data = data;
    }

    public WebEvent getEvent() {
        return event;
    }

    public void setEvent(WebEvent event) {
        this.event = event;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
