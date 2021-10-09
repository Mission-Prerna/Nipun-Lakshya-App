package com.samagra.grove.logging;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.samagra.grove.hyperlog.HyperLog;

import timber.log.Timber;

@SuppressWarnings("ConstantConditions")
public class Grove {
    private static LoggableApplication applicationInstance;
    private static String TAG = "org.samagra.missionPrerna";

    public static void init(LoggableApplication applicationInstance, Context context, boolean isHyperLogEnabled) {
        Grove.applicationInstance = applicationInstance;
        HyperLog.initialize(context, isHyperLogEnabled);
        HyperLog.setLogLevel(Log.VERBOSE);
    }

    private static Class initClass() {
        if (applicationInstance == null) return null;
        Activity currentActivity = applicationInstance.getCurrentActivity();
        Class clazz = applicationInstance.getClass();
        if (currentActivity != null) {
            TAG = currentActivity.getClass().getName();
            clazz = currentActivity.getClass();
        }
        return clazz;
    }

    public static void e(Throwable t, String message, Object... args) {
        Timber.e(t, message, args);
        StringBuilder stringBuilder = new StringBuilder();
        for (Object argument : args) {
            stringBuilder.append("").append(argument.toString());
        }
        HyperLog.e(TAG, "Error with Arguments\n" + stringBuilder.toString() + " " + message, t);
    }

    public static void e(String message, Object... args) {
        Class clazz = initClass();
        if (clazz != null && args != null) {
            Timber.e(message, args);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object argument : args) {
                stringBuilder.append("").append(argument.toString());
            }
            HyperLog.e(clazz.getName(), "Error with Arguments\n" + stringBuilder.toString() + " " + message);
        } else {
            Timber.e(message);
            HyperLog.e(clazz.getName(), "Error is: " + message);
        }
    }

    public static void e(Throwable e) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.tag(clazz.getName());
            Timber.e(e);
            HyperLog.e(clazz.getName(), "", e);
        } else {
            Timber.tag(TAG);
            Timber.e(e);
            HyperLog.e(TAG, "", e);
        }
    }

    public static void e(String message) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.tag(clazz.getName());
            Timber.e(message);
            HyperLog.e(clazz.getName(), message);
        } else {
            Timber.tag(TAG);
            Timber.e(message);
            HyperLog.e(TAG, message);
        }
    }

    public static void d(Throwable e) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.tag(clazz.getName());
            Timber.d(e);
            HyperLog.d(clazz.getName(), "", e);
        } else {
            Timber.tag(TAG);
            Timber.d(e);
            HyperLog.d(TAG, "", e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void d(String message) {

        Class clazz = initClass();
        if (clazz != null) {
            Timber.tag(clazz.getName());
            Timber.d(clazz.getName());
            HyperLog.d(clazz.getName(), message);
        } else {
            Timber.tag(TAG);
            Timber.d(TAG);
            HyperLog.d(TAG, message);
        }
    }

    public static void i(Throwable e) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.i(e);
        HyperLog.i(clazz.getName(), "", e);
    }

    public static void d(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.d(t, message, args);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object argument : args) {
                stringBuilder.append("").append(argument.toString());
            }
            HyperLog.d(clazz.getName(), "Debug Log with Arguments\n" + stringBuilder.toString() + " " + message, t);
        } else {
            Timber.d(t, message, args);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object argument : args) {
                stringBuilder.append("").append(argument.toString());
            }
            HyperLog.d(TAG, "Debug Log with Arguments\n" + stringBuilder.toString() + " " + message, t);
        }
    }

    public static void d(String message, Object... args) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.d(message, args);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object argument : args) {
                stringBuilder.append("").append(argument.toString());
            }
            HyperLog.d(clazz.getName(), "Warning with Arguments\n" + stringBuilder.toString() + " " + message);
        } else {
            Timber.d(message, args);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object argument : args) {
                stringBuilder.append("").append(argument.toString());
            }
            HyperLog.d(clazz.getName(), "Warning with Arguments\n" + stringBuilder.toString() + " " + message);
        }
    }

    public static void i(String message) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.tag(clazz.getName());
            Timber.i(message);
            HyperLog.i(clazz.getName(), message);
        } else {
            Timber.tag(TAG);
            Timber.i(message);
            HyperLog.i(TAG, message);
        }
    }

    public static void i(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        Timber.i(t, message, args);
        StringBuilder stringBuilder = new StringBuilder();
        for (Object argument : args) {
            stringBuilder.append("").append(argument.toString());
        }
        HyperLog.i(clazz.getName(), "Info Log with Arguments\n" + stringBuilder.toString() + " " + message, t);
    }

    public static void i(String message, Object... args) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.i(message, args);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object argument : args) {
                stringBuilder.append("").append(argument.toString());
            }
            HyperLog.i(clazz.getName(), "Info Log with Arguments\n" + stringBuilder.toString() + " " + message);
        } else {
            Timber.i(message, args);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object argument : args) {
                stringBuilder.append("").append(argument.toString());
            }
            HyperLog.i(TAG, "Info Log with Arguments\n" + stringBuilder.toString() + " " + message);
        }
    }

    public static void v(String message) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.tag(clazz.getName());
            Timber.v(message);
            HyperLog.v(clazz.getName(), message);
        } else {
            Timber.tag(TAG);
            Timber.v(message);
            HyperLog.v(TAG, message);
        }
    }

    public static void v(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        Timber.v(t, message, args);
        HyperLog.v(clazz.getName(), message);
    }

    public static void v(String message, Object... args) {
        Class clazz = initClass();
        Timber.v(message, args);
        HyperLog.v(clazz.getName(), message);

    }

    public static void w(String message) {
        Class clazz = initClass();
        if (clazz != null) {
            Timber.tag(clazz.getName());
            Timber.w(message);
            HyperLog.w(clazz.getName(), message);
        } else {
            Timber.tag(TAG);
            Timber.w(message);
            HyperLog.w(TAG, message);
        }
    }

    public static void w(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        Timber.w(t, message, args);
        StringBuilder stringBuilder = new StringBuilder();
        for (Object argument : args) {
            stringBuilder.append("").append(argument.toString());
        }
        HyperLog.d(clazz.getName(), "Warning with Arguments\n" + stringBuilder.toString() + " " + message, t);
    }

    public static void w(String message, Object... args) {
        Class clazz = initClass();
        Timber.w(message, args);
        StringBuilder stringBuilder = new StringBuilder();
        for (Object argument : args) {
            stringBuilder.append("").append(argument.toString());
        }
        HyperLog.w(clazz.getName(), "Warning with Arguments\n" + stringBuilder.toString() + " " + message);
    }

    public static void w(Throwable e) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.w(e);
        HyperLog.w(clazz.getName(), "", e);
    }

    /**
     * Formats a log message with optional arguments.
     */
    private static String formatMessage(String message, Object[] args) {
        return String.format(message, args);
    }

}