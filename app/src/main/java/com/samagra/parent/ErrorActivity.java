package com.samagra.parent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.posthog.android.Properties;
import com.samagra.commons.constants.UserConstants;
import com.samagra.commons.posthog.PostHogEventKt;
import com.samagra.commons.posthog.PostHogManager;
import com.samagra.commons.posthog.data.Cdata;
import com.samagra.commons.posthog.data.Edata;
import com.samagra.commons.slack.SendSlackMessageTask;
import com.samagra.grove.logging.Grove;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ErrorActivity extends AppCompatActivity {

    private String strCurrentErrorLog;
    private SharedPreferences sharedPreferences;
    public static final String EXTRA_STACK_TRACE = "EXTRA_STACK_TRACE";
    public static final String EXTRA_ACTIVITY_LOG = "EXTRA_ACTIVITY_LOG";
    public static final String ACTIVITY_NAME = "ACTIVITY_NAME";
    private String mailSubject;
    private String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_error);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ErrorActivity.this);
        appName = "org.samagra.missionPrerna";
        TextView versionName = findViewById(R.id.tv_version);
        versionName.setText(UtilityFunctions.getVersionName(this));
        Grove.e("Starting Error Activity");
//        sendSlackMessage();
        sendPostHogEvent(this);
        findViewById(R.id.restartApp_button).setOnClickListener(v -> android.os.Process.killProcess(android.os.Process.myPid()));


    }

    private void sendPostHogEvent(Context ctx) {
        String[] lines = getStackTraceFromIntent(getIntent()).split(":");
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String userName = PreferenceManager.getDefaultSharedPreferences(this).getString(UserConstants.MENTOR_DETAIL, "");
        String subject = "[CTT]  " + appName.toUpperCase() + " User: " + userName +
                " - ver(" + versionCode + ") - " + lines[0].substring(10) + ":" + lines[1];
        String body = getAllErrorDetailsFromIntent(ErrorActivity.this, getIntent());
        ArrayList<Cdata> cDataList = new ArrayList<>();
        cDataList.add(new Cdata("logs", subject + "----------\n\n" + body));
        Properties properties = PostHogManager.INSTANCE.createProperties("odk-flow", PostHogEventKt.EVENT_TYPE_SUMMARY,
                PostHogEventKt.EID_INTERACT, PostHogManager.INSTANCE.createContext("com.samagra.parent.", "error", cDataList), new Edata("odk-flow", "view"), null,PreferenceManager.getDefaultSharedPreferences(ErrorActivity.this));
        PostHogManager.INSTANCE.capture(ctx, "nl_error", properties);
    }

    private void sendSlackMessage() {
        String[] lines = getStackTraceFromIntent(getIntent()).split(":");
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        String userName = PreferenceManager.getDefaultSharedPreferences(this).getString(UserConstants.MENTOR_DETAIL, "");
        String subject = "[CTT]  " + appName.toUpperCase() + " User: " + userName +
                " - ver(" + versionCode + ") - " + lines[0].substring(10) + ":" + lines[1];
        String body = getAllErrorDetailsFromIntent(ErrorActivity.this, getIntent());

        new SendSlackMessageTask(subject, body, s -> {
        }).execute();
    }

    private String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String getActivityLogFromIntent(Intent intent) {
        return intent.getStringExtra(EXTRA_ACTIVITY_LOG);
    }

    private String getStackTraceFromIntent(Intent intent) {
        return intent.getStringExtra(EXTRA_STACK_TRACE);
    }

    private String getActivityName(Intent intent) {
        return intent.getStringExtra(ACTIVITY_NAME);
    }

    private String getAllErrorDetailsFromIntent(Context context, Intent intent) {
        if (TextUtils.isEmpty(strCurrentErrorLog)) {
            String LINE_SEPARATOR = "\n";
            String userName = sharedPreferences.getString("user.fullName", "");
            StringBuilder errorReport = new StringBuilder();
            errorReport.append("\n***** Error Title \n");
            errorReport.append(appName);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Error: ");
            String[] lines = getStackTraceFromIntent(intent).split(":");
            if(lines[0].contains("java.lang."))
                errorReport.append(lines[0].substring(10));
            String versionName = getVersionName(context);
            String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
            mailSubject = "[CTT]  "+appName.toUpperCase() + " User: " + userName + " - ver(" + versionCode+ ") - " +lines[0].substring(10)+ ":" +lines[1];

            errorReport.append(lines[1]);
            String[] line = lines[3].split("at");
            errorReport.append(LINE_SEPARATOR);
            errorReport.append(line[0]);
            errorReport.append(LINE_SEPARATOR);

            errorReport.append("\n***** BreadCrumbs \n");
            errorReport.append(intent.getStringExtra("LOGS"));

            errorReport.append("\n***** USER INFO \n");
            errorReport.append("Name: ");
            errorReport.append(userName);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("User Data: ");
            errorReport.append(sharedPreferences.getString("user.data", ""));
            errorReport.append(LINE_SEPARATOR);
            errorReport.append(sharedPreferences.getString("user.data", ""));
            errorReport.append(LINE_SEPARATOR);

            errorReport.append("\n***** DEVICE INFO \n");
            errorReport.append("Brand: ");
            errorReport.append(Build.BRAND);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Device: ");
            errorReport.append(Build.DEVICE);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Model: ");
            errorReport.append(Build.MODEL);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Manufacturer: ");
            errorReport.append(Build.MANUFACTURER);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Product: ");
            errorReport.append(Build.PRODUCT);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("SDK: ");
            errorReport.append(Build.VERSION.SDK_INT);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Release: ");
            errorReport.append(Build.VERSION.RELEASE);
            errorReport.append(LINE_SEPARATOR);

            errorReport.append("\n***** APP INFO \n");
            errorReport.append("Version: ");
            errorReport.append(versionName);
            errorReport.append(LINE_SEPARATOR);
            Date currentDate = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String firstInstallTime = getFirstInstallTimeAsString(context, dateFormat);
            if (!TextUtils.isEmpty(firstInstallTime)) {
                errorReport.append("Installed On: ");
                errorReport.append(firstInstallTime);
                errorReport.append(LINE_SEPARATOR);
            }
            String lastUpdateTime = getLastUpdateTimeAsString(context, dateFormat);
            if (!TextUtils.isEmpty(lastUpdateTime)) {
                errorReport.append("Updated On: ");
                errorReport.append(lastUpdateTime);
                errorReport.append(LINE_SEPARATOR);
            }
            errorReport.append("Current Date: ");
            errorReport.append(dateFormat.format(currentDate));
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("\n***** ERROR LOG \n");
            errorReport.append(getStackTraceFromIntent(intent));
            errorReport.append(LINE_SEPARATOR);
            String activityLog = getActivityLogFromIntent(intent);
            errorReport.append(getActivityName(intent));
            errorReport.append(LINE_SEPARATOR);
            if (activityLog != null) {
                errorReport.append("\n***** USER ACTIVITIES \n");
                errorReport.append("User Activities: ");
                errorReport.append(activityLog);
                errorReport.append(LINE_SEPARATOR);
            }
            errorReport.append("\n***** END OF LOG *****\n");
            strCurrentErrorLog = errorReport.toString();
        }
        return strCurrentErrorLog;
    }

    private String getFirstInstallTimeAsString(Context context, DateFormat dateFormat) {
        long firstInstallTime;
        try {
            firstInstallTime = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .firstInstallTime;
            return dateFormat.format(new Date(firstInstallTime));
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private String getLastUpdateTimeAsString(Context context, DateFormat dateFormat) {
        long lastUpdateTime;
        try {
            lastUpdateTime = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .lastUpdateTime;
            return dateFormat.format(new Date(lastUpdateTime));
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

}
