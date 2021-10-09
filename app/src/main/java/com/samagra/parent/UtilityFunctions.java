package com.samagra.parent;

import static com.samagra.commons.utils.CommonConstants.ODK;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.commons.MetaDataExtensions;
import com.samagra.commons.models.FormStructure;
import com.samagra.parent.helper.RealmStoreHelper;
import com.samagra.parent.ui.HomeScreen.AssessmentRangeConfig;
import com.samagra.parent.ui.detailselection.ClassModel;
import com.samagra.parent.ui.detailselection.SubjectModel;
import com.samagra.commons.models.chaptersdata.ChapterMapping;
import com.samagra.workflowengine.workflow.model.FlowConfig;
import com.samagra.workflowengine.workflow.model.WorkflowConfig;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import io.reactivex.annotations.NonNull;
import io.realm.RealmList;
import timber.log.Timber;

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


    public static Object getObjectFromJson(String jsonString, Class resultObjectClass) {
        if (resultObjectClass == null) {
            // This block will return response data as it is (whether it is in JSON or other string
            // Hence user can get raw response string, if he pass responseObjectClass as null
            return jsonString;
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, resultObjectClass);
        } catch (Exception e) {
            return null;
        }
    }

    public static ArrayList<AssessmentRangeConfig> getJsonObject(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<AssessmentRangeConfig>>() {
        }.getType();
        return gson.fromJson(jsonString, type);
    }

    public static boolean appInstalledOrNot(PackageManager pm, @Nullable String uri) {
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
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
        InputMethodManager imm = (InputMethodManager) activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static Map<Integer, Set<String>> parseFlowConfig(WorkflowConfig workflowConfig) {
        Map<Integer, Set<String>> configMapper = new HashMap<>();
        try {
            List<FlowConfig> flowConfigs = workflowConfig.getFlowConfigs();
            for (FlowConfig flowConfig : flowConfigs) {
                Set<String> subjectsSet = configMapper.get(flowConfig.getGradeNumber());
                if (subjectsSet == null) {
                    subjectsSet = new TreeSet<>();
                    configMapper.put(flowConfig.getGradeNumber(), subjectsSet);
                }
                subjectsSet.add(flowConfig.getSubject());
            }
            Set<String> defaultSubjectsSet = new TreeSet<>();
            defaultSubjectsSet.add("hindi");
            defaultSubjectsSet.add("math");

            configMapper.put(null, defaultSubjectsSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configMapper;
    }

    public static HashSet<String> getOdkFormId() {
        HashSet<String> odkFormsSet = new HashSet<>();
        try {
            ArrayList<ChapterMapping> chapterMappingList = RealmStoreHelper.getChapterMapping();
            if (chapterMappingList != null) {
                for (ChapterMapping chapterMappingData : chapterMappingList) {
                    if (chapterMappingData.getType().equals(ODK)) {
                        List<String> refIds = chapterMappingData.getRefIds();
                        if (refIds != null && !refIds.isEmpty()) {
                            for (String refId : refIds) {
                                odkFormsSet.add(refId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return odkFormsSet;
    }

    public static HashSet<FormStructure> getOdkFormIds(CommonsPrefsHelperImpl prefs) {
        HashSet<FormStructure> odkFormsSet = new HashSet<>();
        try {
            List<ChapterMapping> chapterMappingList = RealmStoreHelper.getChapterMapping();
            if (chapterMappingList != null) {
                for (ChapterMapping chapterMappingData : chapterMappingList) {
                    if (chapterMappingData.getType().equals(ODK)) {
                        List<String> refIds = chapterMappingData.getRefIds();
                        String subject = MetaDataExtensions.getSubjectFromId(chapterMappingData.getSubjectId(), prefs.getSubjectsListJson());
                        String formName = "Grade " + chapterMappingData.getGrade() + " - " + subject;
                        if (refIds != null && !refIds.isEmpty()) {
                            for (String refId : refIds) {
                                FormStructure formStructure = new FormStructure(refId, formName, subject);
                                odkFormsSet.add(formStructure);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return odkFormsSet;
    }

    public static List<ClassModel> getClassData(Map<Integer, Set<String>> configMapper) {
        Set<Integer> classes = configMapper.keySet();
        List<ClassModel> mClassData = new ArrayList<>();

        for (Integer clazz : classes) {
            if (clazz != null) {
                mClassData.add(new ClassModel(clazz, "Grade " + clazz));
            }
        }
        return mClassData;
    }

    public static ArrayList<SubjectModel> getSubjectData(Map<Integer, Set<String>> configMapper, Integer clazz) {
        Set<String> subjects = configMapper.get(clazz);
        ArrayList<SubjectModel> subjectData = new ArrayList<>();
        for (String sub : subjects) {
            SubjectModel subjectModel = new SubjectModel(sub, sub);
            if (subjectModel.getTag().equalsIgnoreCase("English")) {
                subjectModel.setImgid(R.drawable.ic_sub_lang);
            } else if (subjectModel.getTag().equalsIgnoreCase("Hindi")) {
                subjectModel.setImgid(R.drawable.ic_hindi);
            } else if (subjectModel.getTag().equalsIgnoreCase("Maths")) {
                subjectModel.setImgid(R.drawable.ic_math);
            } else {
                subjectModel.setImgid(R.drawable.ic_math);
            }
            subjectData.add(subjectModel);

        }
        return subjectData;
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static long getTimeMilis() {
        return System.currentTimeMillis();
    }

    public static String getTotalTimeTaken(long startTime, long endTime) {
        return getTimeString(endTime - startTime);
    }

    public static long getTimeDifferenceMilis(long startTime, long endTime) {
        return endTime - startTime;
    }

    public static String getTimeString(long l) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return df.format(new Date(l));
    }

    public static int getTimeInSeconds(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // Parsing the Time Period
        Date date = null;
        try {
            date = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Calculating Seconds
        long seconds = 0L;
        if (date != null){
            seconds = (date.getTime() / 1000) % 60;
        }
//        Log.e("-->>", "time in seconds : " + seconds);
        return (int) Math.abs(seconds);
    }

    public static String timeDifference(String startTime, String endTime) {
//        Log.e("-->>", "start time " + startTime + " end time " + endTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        // Parsing the Time Period
        Date date1 = null;
        try {
            date1 = simpleDateFormat.parse(startTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date2 = null;
        try {
            date2 = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Calculating the difference in milliseconds
        long differenceInMilliSeconds = 0;
        if (date2 != null && date1 != null) {
            differenceInMilliSeconds = Math.abs(date2.getTime() - date1.getTime());
        }
        // Calculating the difference in Hours
        long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
        // Calculating the difference in Minutes
        long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
        // Calculating the difference in Seconds
        long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;

        Timber.e("time difference " + differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds);

        return differenceInHours + ":" + differenceInMinutes + ":" + differenceInSeconds;
    }

    private static boolean isNetwork2G(Context context) {
        TelephonyManager teleMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        int networkType = teleMan.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_GSM:
                return true;
            // 3G case
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:

                // 4G case
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:

                // 5G case

                // Wifi case
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return false;
        }
        return false;
    }

    public static boolean isInternetAvailable(Context context) {
        if (context != null) {
            ConnectivityManager localConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean isNetworkStable = true;
            isNetworkStable = localConnectivityManager.getActiveNetworkInfo() != null && (localConnectivityManager.getActiveNetworkInfo().isAvailable()) && (localConnectivityManager.getActiveNetworkInfo().isConnected());
            NetworkInfo active_network = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (isNetworkStable) {
                if (active_network.getType() != ConnectivityManager.TYPE_WIFI) {
                    isNetworkStable = !isNetwork2G(context);
                }
            }
            return isNetworkStable;
        }
        return false;
    }

    /*
     * if changes done here please also make changes in another module method present in same name file
     * */
    public static String getVersionName(Context context) {
        try {
            return "V " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public static int getVersionCode() {
        try {
           return BuildConfig.VERSION_CODE;
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getFirstDateOfMonth() {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return sdf1.format(calendar.getTime());
    }

    public static Integer getCurrentMonth() {
        Integer[] month = new Integer[]{R.string.january, R.string.february, R.string.march, R.string.april, R.string.may, R.string.june, R.string.july, R.string.august, R.string.september, R.string.october, R.string.november, R.string.december};
        return month[Calendar.getInstance().get(Calendar.MONTH)];
    }

    public static String currentTimeWithStamp() {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf1.format(timestamp);
    }

    //todo convert in kotlin by shashank
    /*
    * Get month in Int start from 1 (Jan) to 12 (Dec)
    * */
    public static int getCurrentMonthInteger() {
//        SimpleDateFormat simpleFormat = new SimpleDateFormat("MMMM");
//        return simpleFormat.format(new Date());
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    /*
    * get month name
    * */
    public static String getCurrentMonthEn() {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("MMMM");
        return simpleFormat.format(new Date());
    }

    public static int getCurrentYearInteger() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static String selectRandomId(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

}