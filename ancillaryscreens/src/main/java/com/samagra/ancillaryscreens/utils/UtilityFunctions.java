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

    private static final int DURATION_SHORT = 3500;
    private static final int DURATION_LONG = 5500;

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static ArrayList<String> makeUnique(ArrayList<String> districts) {
        Set<String> set = new HashSet<>(districts);
        districts.clear();
        districts.addAll(set);
        Collections.sort(districts);
        return districts;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Displays snackbar with {@param message}
     * and multi-line message enabled.
     *
     * @param view    The view to find a parent from.
     * @param message The text to show.  Can be formatted text.
     */
    public static void showSnackbar(@NonNull View view, @NonNull String message, int duration) {
        if (message.isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(view, message.trim(), duration);
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }

    /**
     * Displays snackbar with {@param message}
     * and multi-line message enabled.
     *
     * @param view    The view to find a parent from.
     * @param message The text to show.  Can be formatted text.
     */
    public static void showSnackbar(@NonNull View view, @NonNull String message, int duration, String dismissText) {
        if (message.isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(view, message.trim(), duration);
        snackbar.setAction(view.getResources().getString(R.string.ok), v -> {
            snackbar.dismiss();
        });
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }


    public static Object getObjectFromJson( String jsonString,Class resultObjectClass) {
        if (resultObjectClass == null) {
            // This block will return response data as it is (whether it is in JSON or other string
            // Hence user can get raw response string, if he pass responseObjectClass as null
            return jsonString;
        }
        try {
            Gson gson =new Gson();
            return gson.fromJson(jsonString, resultObjectClass);
        } catch (Exception e) {
            return null;
        }
    }



    /*public static ArrayList<AssessmentRangeConfig> getJsonObject( String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<AssessmentRangeConfig>>() {}.getType();
        return gson.fromJson(jsonString, type);
    }*/

    public static boolean appInstalledOrNot(PackageManager pm, @Nullable String uri) {
         try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch ( PackageManager.NameNotFoundException e) {
           return false;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager.getActiveNetwork() == null) return false;
            Network nw = connectivityManager.getActiveNetwork();
            if (connectivityManager.getNetworkCapabilities(nw) == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true;
            if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true;
            if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return true;
            return actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH);
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    public static String toJson(Object inputObject) {
        try {
            Gson gson = new Gson();
            return gson.toJson(inputObject);
        } catch (Exception e) {
            return "";
        }
    }

    public static int getRandomSample(List<Integer> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
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
