package com.samagra.ancillaryscreens.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.samagra.ancillaryscreens.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.reactivex.annotations.NonNull;

/**
 * This class contains Utility function that can be accessed anywhere throughout the module 'app'.
 * All the functions in this class will be public and static.
 *
 * @author Pranav Sharma
 */
public class UtilityFunctions {

    public static String toJson(Object inputObject) {
        try {
            Gson gson = new Gson();
            return gson.toJson(inputObject);
        } catch (Exception e) {
            return "";
        }
    }

    public static void hideKeyboard(Context activityContext, Activity activity) {
        InputMethodManager imm = (InputMethodManager)activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /*
    * if changes done here please also make changes in parent module method present in same name file
    * */
    public static String getVersionName(Context context) {
        try {
            return "V " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
