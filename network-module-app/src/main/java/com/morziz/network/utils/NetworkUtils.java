package com.morziz.network.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.morziz.network.helpers.NoConnectivityException;
import com.morziz.network.models.ErrorData;
import com.morziz.network.network.ErrorCodesNKt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

import retrofit2.Response;

/**
 * Created by ankitsharma on 2/25/16.
 */
public class NetworkUtils {


    /**
     * returns information on the active network connection
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return null;
        }
        // note that this may return null if no network is currently active
        return cm.getActiveNetworkInfo();
    }

    /**
     * returns true if a network connection is available
     */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * check's network call status
     * success - if statusCode is between 200 && 400
     * failure - else cases are failure
     * also if status is 406 the revalidates user if enabled
     *
     * @param context
     * @param response
     * @return true or false
     */
    public static boolean isCallSuccess(final Context context, Response response) {
        return isCallSuccess(context, response, true);
    }

    public static boolean isCallSuccess(final Context context, int responseCode) {
        return isCallSuccess(context, responseCode, true);
    }

    /**
     * check's network call status
     * success - if statusCode is between 200 && 400
     * failure - else cases are failure
     * also if status is 406 then revalidate user if enabled
     *
     * @param context
     * @param response
     * @param shouldRevalidate if user revalidation required or not, default is true,
     *                         only to be used in case invalid network calls are to be ignored
     * @return true or false
     */
    public static boolean isCallSuccess(final Context context, Response response, boolean shouldRevalidate) {
        return isCallSuccess(context, response.code(), shouldRevalidate);
    }

    public static boolean isCallSuccess(final Context context, int code, boolean shouldRevalidate) {

        return code >= 200 && code < 400;

        //TODO
        /*//in case some auth issue occurred
        if (shouldRevalidate && (code == 406 || code == 455 || code == 456 || code == 457)  ) {
            Network.Companion.reValidateUser(code);
        }*/
    }

    public static ErrorData processFailure(Throwable t) {
        ErrorData errorData = new ErrorData();
        errorData.setT(t);
        errorData.setTitle(ErrorMessagesN.GENERIC_ERROR_TITLE);

        try {
            if (t instanceof SSLException) {
                errorData.setMessage(ErrorMessagesN.NETWORK_ISSUE);
                errorData.setCode(ErrorCodesNKt.SSL_EXCEPTION);
//                FirebaseCrashlytics.getInstance().recordException(t);
            } else if (t instanceof SocketTimeoutException) {
                errorData.setMessage(ErrorMessagesN.REQUEST_TIMED_OUT);
                errorData.setCode(ErrorCodesNKt.REQUEST_TIMED_OUT);
            } else if (t instanceof SocketException) {
                errorData.setMessage(ErrorMessagesN.SOCKET_EXCEPTION);
                errorData.setCode(ErrorCodesNKt.SOCKET_EXCEPTION);
            } else if (t instanceof NoConnectivityException) {
                errorData.setMessage(ErrorMessagesN.NETWORK_ISSUE);
                errorData.setCode(ErrorCodesNKt.NO_INTERNET);
            } else if (t instanceof UnknownHostException) {
                errorData.setMessage(ErrorMessagesN.NETWORK_ISSUE);
                errorData.setCode(ErrorCodesNKt.UNKNOWN_HOST);
            } else if (t instanceof IOException){
                errorData.setMessage(ErrorMessagesN.IO_EXCEPTION);
                errorData.setCode(ErrorCodesNKt.IO_EXCEPTION);
            } else {
                errorData.setMessage(ErrorMessagesN.GENERIC_ERROR_MSG);
                errorData.setCode(ErrorCodesNKt.GENERIC_ERROR);
            }
            t.printStackTrace();
        } catch (Exception e) {
        }
        return errorData;
    }

    public static String getNetworkType(Context context) {
        String networkType = null;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            networkType = activeNetworkInfo.getTypeName();
            if (ConnectivityManager.TYPE_MOBILE == activeNetworkInfo.getType()) {
                networkType = activeNetworkInfo.getSubtypeName();
            }
        }

        return networkType;
    }


    public static String getDeviceIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    if (!inetAddress.isLoopbackAddress()) {
                        String sAddr = inetAddress.getHostAddress().toUpperCase();

                        if (useIPv4) {
                            return sAddr;
                        } else {
                            // drop ip6 port suffix
                            int delim = sAddr.indexOf('%');
                            return delim < 0 ? sAddr : sAddr.substring(0, delim);

                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static boolean isInternetConnected(Context context) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
