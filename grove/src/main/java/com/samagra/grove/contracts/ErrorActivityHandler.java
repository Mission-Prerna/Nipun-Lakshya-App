package com.samagra.grove.contracts;

import android.content.Context;

public interface ErrorActivityHandler {
    void onUncaughtExceptionReceived(Context context, String stackTraceString, String toString, String toString1, String logs);
}
