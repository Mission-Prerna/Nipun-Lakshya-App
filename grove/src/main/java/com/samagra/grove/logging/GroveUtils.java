package com.samagra.grove.logging;


import android.text.TextUtils;

public class GroveUtils {

    private static String userName;

    private static String userData;


    static String getUserName() {
        return !TextUtils.isEmpty(userName)? userName : "";
    }

    public static void setUserName(String mUserName) {
        userName = mUserName;
    }
    static String getUserData() {
        return !TextUtils.isEmpty(userData)? userData : "";
    }

    public static void setUserData(String mUserData) {
        userData = mUserData;
    }
}

