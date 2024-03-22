/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.samagra.grove.hyperlog;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.samagra.grove.hyperlog.utils.HLDateTimeUtility;
import com.samagra.grove.hyperlog.utils.Utils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class HyperLog {

    private static final String TAG = "HyperLog";
    private static final String TAG_ASSERT = "ASSERT";
    private static final String TAG_HYPERLOG = "HYPERLOG";

    private static int logLevel = Log.WARN;

    private static DeviceLogList mDeviceLogList;
    private static final int EXPIRY_TIME = 7 * 24 * 60 * 60;// 7 Days
    private static LogFormat mLogFormat;
    private static Context context;
    private static ExecutorService executorService;
    private static boolean isHyperlogEnabled = true;

    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will gets deleted automatically.
     *
     * @param context The current context.
     * @see #initialize(Context, int, LogFormat)
     */
    public static void initialize(@NonNull Context context, boolean isHyperlogEnabled) {
        if (isHyperlogEnabled)
            initialize(context, EXPIRY_TIME, new LogFormat(context));
    }

    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will gets deleted automatically.
     *
     * @param context   The current context.
     * @param logFormat {@link LogFormat} to set custom log message format.
     * @see #initialize(Context, int, LogFormat)
     */
    private static void initialize(@NonNull Context context, @NonNull LogFormat logFormat) {
        if (isHyperlogEnabled)
            initialize(context, EXPIRY_TIME, logFormat);
    }

    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will gets deleted automatically.
     *
     * @param context             The current context.
     * @param expiryTimeInSeconds Expiry time for logs in seconds.
     * @see #initialize(Context, int, LogFormat)
     */
    public static void initialize(@NonNull Context context, int expiryTimeInSeconds) {
        initialize(context, expiryTimeInSeconds, new LogFormat(context));
    }

    /**
     * Call this method to initialize HyperLog.
     * By default, seven days older logs will get expire automatically. You can change the expiry
     * period of logs by defining expiryTimeInSeconds.
     *
     * @param context             The current context.
     * @param expiryTimeInSeconds Expiry time for logs in seconds.
     * @param logFormat           {@link LogFormat} to set custom log message format.
     */
    private static void initialize(@NonNull Context context, int expiryTimeInSeconds,
                                   @NonNull LogFormat logFormat) {

        HyperLog.context = context.getApplicationContext();

        synchronized (HyperLog.class) {
            if (logFormat != null) {
                mLogFormat = logFormat;
                Utils.saveLogFormat(context, mLogFormat);
            } else {
                mLogFormat = Utils.getLogFormat(context);
            }

            if (mDeviceLogList == null) {
                DeviceLogDataSource logDataSource = DeviceLogDatabaseHelper.getInstance(context);
                mDeviceLogList = new DeviceLogList(logDataSource);
                mDeviceLogList.clearOldLogs(expiryTimeInSeconds);
            }
        }
    }

    private static boolean isInitialize() {
        return mDeviceLogList != null && mLogFormat != null;
    }

    /**
     * Sets the level of logging to display, where each level includes all those below it.
     * The default level is LOG_LEVEL_NONE. Please ensure this is set to Log#ERROR
     * or LOG_LEVEL_NONE before deploying your app to ensure no sensitive information is
     * logged. The levels are:
     * <ul>
     * <li>{@link Log#ASSERT}</li>
     * <li>{@link Log#VERBOSE}</li>
     * <li>{@link Log#DEBUG}</li>
     * <li>{@link Log#INFO}</li>
     * <li>{@link Log#WARN}</li>
     * <li>{@link Log#ERROR}</li>
     * </ul>
     *
     * @param logLevel The level of logcat logging that Parse should do.
     */
    public static void setLogLevel(int logLevel) {
        HyperLog.logLevel = logLevel;
    }

    private static void v(String tag, String message, Throwable tr) {
        if (Log.VERBOSE >= logLevel) {
            Timber.v(message + '\n' + Log.getStackTraceString(tr));
        }
        r(getFormattedLog(logLevel, tag, message));
    }

    public static void v(String tag, String message) {
        v(tag, message, null);
    }

    public static void d(String tag, String message, Throwable tr) {
        if (Log.DEBUG >= logLevel) {
            Timber.d(message + '\n' + Log.getStackTraceString(tr));
        }
        r(getFormattedLog(logLevel, tag, message));

    }

    public static void d(String tag, String message) {
        d(tag, message, null);
    }

    public static void i(String tag, String message, Throwable tr) {
        if (Log.INFO >= logLevel) {
            Timber.i(message + '\n' + Log.getStackTraceString(tr));
        }

        r(getFormattedLog(Log.INFO, tag, message));
    }

    /**
     * Info log level that will store into DB.
     */
    public static void i(String tag, String message) {
        i(tag, message, null);
    }

    public static void w(String tag, String message, Throwable tr) {
        if (Log.WARN >= logLevel) {
            Timber.w(message + '\n' + Log.getStackTraceString(tr));
        }

        r(getFormattedLog(Log.WARN, tag, message));
    }

    public static void w(String tag, String message) {
        w(tag, message, null);
    }

    public static void e(String tag, String message, Throwable tr) {
        if (Log.ERROR >= logLevel) {
            Timber.e(message + '\n' + Log.getStackTraceString(tr));
        }

        r(getFormattedLog(Log.ERROR, tag, message));
    }

    public static void e(String tag, String message) {
        e(tag, message, null);
    }

    private static void exception(String tag, String message, Throwable tr) {
        if (Log.ERROR >= logLevel) {
            Timber.e("**********************************************");
            Timber.e("EXCEPTION: " + getMethodName() + ", " + message + '\n' +
                    Log.getStackTraceString(tr));
            Timber.e("**********************************************");
        }
        r(getFormattedLog(Log.ERROR, tag, "EXCEPTION: " + getMethodName() + ", "
                + message));
    }

    public static void exception(String tag, String message) {
        exception(tag, message, null);
    }

    public static void exception(String tag, Exception e) {
        if (e == null)
            return;

        exception(tag, e.getMessage(), null);
    }

    public static void a(String message) {
        r(getFormattedLog(Log.ASSERT, TAG_ASSERT, message));
    }

    private static String getMethodName() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[1];//coz 0th will be getStackTrace so 1st
        return e.getMethodName();
    }

    private static void r(final String message) {
        if(isHyperlogEnabled) {
            try {

                if (executorService == null)
                    executorService = Executors.newSingleThreadExecutor();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!isInitialize() || message == null || message.isEmpty())
                                return;

                            mDeviceLogList.addDeviceLog(message);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };

                executorService.submit(runnable);

            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call this method to get a list of stored Device Logs
     *
     * @return List of {@link DeviceLogModel}
     */
    public static List<DeviceLogModel> getDeviceLogs() {
        return getDeviceLogs(true);
    }

    /**
     * Call this method to get a list of stored Device Logs
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @return List of {@link DeviceLogModel}
     */
    private static List<DeviceLogModel> getDeviceLogs(boolean deleteLogs) {
        return getDeviceLogs(deleteLogs, 1);
    }

    /**
     * Call this method to get a list of stored Device Logs.
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @param batchNo    If there are more than one batch of device log then specify the batch number.
     *                   Batch number should be greater than or equal to 1.
     * @return List of {@link DeviceLogModel} or empty list if batch number is greater than the
     * {@link HyperLog#getDeviceLogBatchCount()}
     */
    private static List<DeviceLogModel> getDeviceLogs(boolean deleteLogs, int batchNo) {
        List<DeviceLogModel> deviceLogs = new ArrayList<>();
        if (!isInitialize())
            return deviceLogs;

        deviceLogs = mDeviceLogList.getDeviceLogs(batchNo);
        if (deleteLogs) {
            mDeviceLogList.clearDeviceLogs(deviceLogs);
        }

        return deviceLogs;
    }

    /**
     * Call this method to get a list of stored Device Logs.
     * Device logs will gets deleted from device after fetching.
     *
     * @return List of {@link String}
     */
    public static List<String> getDeviceLogsAsStringList() {
        return getDeviceLogsAsStringList(true);
    }

    /**
     * Call this method to get a list of stored Device Logs
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @return List of {@link String}
     */
    private static List<String> getDeviceLogsAsStringList(boolean deleteLogs) {
        return getDeviceLogsAsStringList(deleteLogs, 1);
    }

    /**
     * Call this method to get a list of stored Device Logs
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @param batchNo    If there are more than one batch of device log then specify the batch number.
     *                   Batch number should be greater than or equal to 1.
     * @return List of {@link String} or if the given batchNo is greater than the
     * {@link HyperLog#getDeviceLogBatchCount()} then returns empty list;
     */
    private static List<String> getDeviceLogsAsStringList(boolean deleteLogs, int batchNo) {
        List<String> logsList = new ArrayList<>();
        if (!isInitialize())
            return logsList;

        if (!hasPendingDeviceLogs()) {
            return logsList;
        }

        return getDeviceLogsAsStringList(getDeviceLogs(deleteLogs, batchNo));
    }

    /**
     * Method to get a list of stored Device Logs
     *
     * @param deviceLogList List of all device logs
     * @return List of {@link String}
     */
    private static List<String> getDeviceLogsAsStringList(List<DeviceLogModel> deviceLogList) {
        List<String> logsList = new ArrayList<>();

        if (deviceLogList == null) {
            return logsList;
        }

        for (DeviceLogModel deviceLog : deviceLogList) {
            logsList.add(deviceLog.getDeviceLog());
        }

        return logsList;
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs with the current date time as
     * name of the file.
     *
     * @param mContext The current context.
     * @return {@link File} object or {@code null} if there is not any logs in device.
     */
    public static File getDeviceLogsInFile(Context mContext) {
        return getDeviceLogsInFile(mContext, null);
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs with the current date time as
     * name of the file.
     *
     * @param mContext   The current context.
     * @param deleteLogs If true then logs will delete from the device.
     * @return {@link File} object or {@code null} if there is not any logs in device.
     */
    public static File getDeviceLogsInFile(Context mContext, boolean deleteLogs) {
        return getDeviceLogsInFile(mContext, null, deleteLogs);
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs with the current date time as
     * name of the file.
     *
     * @param mContext The current context.
     * @param fileName Name of the file.
     * @return {@link File} object or {@code null} if there is not any logs in device.
     */
    private static File getDeviceLogsInFile(Context mContext, String fileName) {
        return getDeviceLogsInFile(mContext, fileName, true);
    }

    /**
     * Call this method to get a stored Device Logs as a File object.
     * A text file will create in the app folder containing all logs.
     *
     * @param mContext   The current context.
     * @param fileName   Name of the file.
     * @param deleteLogs If true then logs will delete from the device.
     * @return {@link File} object, or {@code null} if there is not any logs in device.
     */
    private static File getDeviceLogsInFile(Context mContext, String fileName, boolean deleteLogs) {

        if (!isInitialize())
            return null;

        File file = null;

        if (TextUtils.isEmpty(fileName)) {
            fileName = HLDateTimeUtility.getCurrentTime() + ".txt";
            fileName = fileName.replaceAll("[^a-zA-Z0-9_\\\\-\\\\.]", "_");
        }

        //Check how many batches of device logs are available to push
        int logsBatchCount = getDeviceLogBatchCount();

        while (logsBatchCount != 0) {
            List<DeviceLogModel> deviceLogList = getDeviceLogs(deleteLogs);

            if (deviceLogList != null && !deviceLogList.isEmpty()) {
                file = Utils.writeStringsToFile(mContext, getDeviceLogsAsStringList(deviceLogList),
                        fileName);
                if (file != null) {
                    if (deleteLogs)
                        mDeviceLogList.clearDeviceLogs(deviceLogList);
                    HyperLog.i(TAG, "Log File has been created at " +
                            file.getAbsolutePath());
                }
            }
            logsBatchCount--;
        }
        return file;
    }

    /**
     * Call this method to check whether any device logs are available.
     *
     * @return true If device has some pending logs otherwise false.
     */
    private static boolean hasPendingDeviceLogs() {
        if (!isInitialize())
            return false;

        long deviceLogsCount = mDeviceLogList.count();
        return deviceLogsCount > 0L;
    }

    /**
     * Call this method to get the count of stored device logs.
     *
     * @return The number of device logs.
     */
    public static long getDeviceLogsCount() {
        if (!isInitialize())
            return 0;

        return mDeviceLogList.count();
    }

    /**
     * Call this method to get number of device logs batches. Each batch contains the 5000 device
     * logs.
     *
     * @return The number of device logs batches.
     */
    public static int getDeviceLogBatchCount() {
        if (!isInitialize())
            return 0;

        return mDeviceLogList.getDeviceLogBatchCount();
    }

    /**
     * Call this method to push logs from device to the server with custom filename as a text file
     * or gzip compressed file.
     * <p>
     * Logs will get delete from the device once it successfully push to the server.
     * <p>
     * If device log count is greater than DEVICE_LOG_REQUEST_QUERY_LIMIT
     * then log will push to the server in batches.
     *
     * @throws IllegalArgumentException if the API endpoint url is empty or null
     */
    public static void deleteLogList() {
        if (!isInitialize())
            return;
        if (!hasPendingDeviceLogs())
            return;
        //Check how many batches of device logs are available to push
        int logsBatchCount = getDeviceLogBatchCount();
        while (logsBatchCount != 0) {
            final List<DeviceLogModel> deviceLogs = getDeviceLogs(false, logsBatchCount);
            deviceLogs.add(new DeviceLogModel(getFormattedLog(Log.INFO, TAG_HYPERLOG,
                    "Log Counts: " + deviceLogs.size() + " | File Size: " +
                            deviceLogs.toString().length() + " bytes.")));
            mDeviceLogList.clearDeviceLogs(deviceLogs);
            logsBatchCount--;
        }
    }

    /**
     * Call this method to delete all logs from device.
     */
    public static void deleteLogs() {
        if (!isInitialize())
            return;
        mDeviceLogList.clearSavedDeviceLogs();
    }

    private static String getFormattedLog(int logLevel, String tag, String message) {
        if (isInitialize()) {
            return mLogFormat.formatLogMessage(logLevel, tag, message);
        }
        return null;
    }
}