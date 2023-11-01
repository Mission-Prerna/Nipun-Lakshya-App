package com.samagra.commons;

import static com.benasher44.uuid.UuidKt.uuid4;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.samagra.commons.utils.NetworkStateManager;
import com.samagra.grove.logging.Grove;

import java.util.List;
import java.util.Random;


/**
 * This class contains the common utilities that can be used independently in any module using this library.
 * All the functions in this class must be public static.
 *
 * @author Pranav Sharma
 */
public class CommonUtilities {
    /**
     * Starts activity as a new task. This means all the activities in the current Task will be removed.
     * Basically, clears the back stack.
     *
     * @param intent  - The {@link Intent} responsible for the new activity
     * @param context - The {@link Context} for the current Activity.
     */
    public static void startActivityAsNewTask(Intent intent, @NonNull Context context) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    /**
     * This functions takes the current {@link Context} and tells if the device is connected to the internet.
     *
     * @param context - Non-null {@link Context} of calling Activity.
     * @return boolean - true if internet available, false otherwise.
     */
    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } else {
            Grove.e("ConnectivityManager is null");
            return false;
        }
    }

    public static boolean isNetworkConnected(ConnectivityManager connectivityManager) {
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

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int convertSecondToMinute(int sec) {
        return Math.round(sec / 60F);
    }

    public static boolean isFirstInstall(Context context) {
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
            long lastUpdateTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
            return firstInstallTime == lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int)(displayMetrics.widthPixels / displayMetrics.density);
    }

    public static String createUUID() {
        return uuid4().toString();
    }

    public static void setCrashlyticsProperty(String value) {
        FirebaseCrashlytics.getInstance().setUserId(value);
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static long getTimeDifferenceMilis(long startTime, long endTime) {
        return endTime - startTime;
    }

    public static String selectRandomId(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }
}
