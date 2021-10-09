package com.samagra.commons;

import android.app.PendingIntent;

public class PushNotification {
    PendingIntent intent;
    int notificationID;
    int title;
    String body;


    public PushNotification(PendingIntent intent, int notificationID, int title, String body) {
        this.intent = intent;
        this.notificationID = notificationID;
        this.title = title;
        this.body = body;
    }

    public PendingIntent getIntent() {
        return intent;
    }

    public void setIntent(PendingIntent intent) {
        this.intent = intent;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
