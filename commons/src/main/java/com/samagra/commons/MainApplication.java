package com.samagra.commons;

import android.app.Activity;
import android.app.Application;

import androidx.lifecycle.LifecycleOwner;

import okhttp3.OkHttpClient;

/**
 * The Application class must implement this interface. This makes sure that the application class of the app
 * implements certain functionality required by the commons module.
 *
 * @author Pranav Sharma
 */
public interface MainApplication extends LifecycleOwner {

    void updateInternetStatus(Boolean status);

    boolean isOnline();

    OkHttpClient provideOkHttpClient();

    /**
     * Must provide a {@link androidx.annotation.NonNull} activity instance of the activity running in foreground.
     * You can use {@link Application#registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks)} to
     * get the currently resumed activity (activity in foreground)
     */
    Activity getCurrentActivity();

    /**
     * Must provide a {@link androidx.annotation.NonNull} instance of the current {@link Application}.
     */
    Application getCurrentApplication();

    /**
     * Must provide a {@link androidx.annotation.NonNull} instance of {@link RxBus} which acts as an event bus
     * for the app.
     */
    RxBus getEventBus();

    /**
     * Optional method to teardown a module after its use is complete.
     * Not all modules require to be teared down.
     */
    void teardownModule(Modules module);

    EventBus eventBusInstance();
}
