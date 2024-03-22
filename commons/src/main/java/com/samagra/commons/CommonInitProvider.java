package com.samagra.commons;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.samagra.commons.posthog.PostHogManager;

public class CommonInitProvider extends ContentProvider {


    public CommonInitProvider() {
    }

    @Override
    public boolean onCreate() {
//        PostHogManager.init(getContext());
        /*AppPreferences.init(getContext().getApplicationContext());
        ProfileData request = new Gson().fromJson(AppPreferences.getInstance().getString(SharedPreferenceKeys.USER_DATA), ProfileData.class);
        if (request != null) {
            PostHogManager.INSTANCE.createBaseMap("e-Samwad", request.getUserId(), request.getUsername(),
                    TextUtils.isEmpty(request.getDesignation()) ? "school" : request.getDesignation(), request.getSchoolUdise() == null ? "-" : request.getSchoolUdise().toString());
        }*/
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo providerInfo) {
        if (providerInfo == null) {
            throw new NullPointerException("InitProvider ProviderInfo cannot be null.");
        }
        super.attachInfo(context, providerInfo);
    }

}