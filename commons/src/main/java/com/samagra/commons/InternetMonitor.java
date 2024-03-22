package com.samagra.commons;

import android.content.Context;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy;
import com.samagra.grove.logging.Grove;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * This class exposes API that enables the app to monitor internet connectivity status.
 * This class can detect internet connected and disconnected state. Note that internet connectivity
 * monitoring is different than network connectivity monitoring. The Phone maybe connected to a WiFi
 * network that is not providing internet access.
 * This class uses {@link ReactiveNetwork APIs internally.
 * You can configure the internet monitoring behaviour of this class by providing an InternetObservingSettings}
 * object.
 *
 * @author Pranav Sharma
 * @see {https://github.com/pwittchen/ReactiveNetwork}
 * @see InternetObservingSettings
 */
public class InternetMonitor {

    private static MainApplication mainApplication = null;
    private static InternetObservingSettings internetObservingSettings = null;
    private static Disposable monitorSubscription = null;
    private static boolean lastConnectedState = false;

    /**
     * Initialisation method for the class. Must be called <b>once</b> in the lifetime of the application
     * prior to using any of the functions from this class. This method initialises the
     * {@link InternetMonitor#internetObservingSettings} with the valid default values.
     *
     * @param mainApplication - The application instance of the current app.
     * @see MainApplication
     */
    public static void init(MainApplication mainApplication) {
        InternetMonitor.mainApplication = mainApplication;
        internetObservingSettings = InternetObservingSettings.builder()
                .interval(5000)
                .strategy(new SocketInternetObservingStrategy())
                .host("www.google.com")
                .httpResponse(200)
                .timeout(5000)
                .errorHandler((exception, message) -> Grove.e(exception, "Exception in Lib message - %s", message))
                .build();
    }

    /**
     * Initialisation method for the class. Must be called <b>once</b> in the lifetime of the application
     * prior to using any of the functions from this class. This method allows user to provide the
     * {@link InternetMonitor#internetObservingSettings} object.
     *
     * @param mainApplication - The application instance of the current app.
     * @see MainApplication
     * @see InternetObservingSettings
     */
    public static void init(MainApplication mainApplication, InternetObservingSettings internetObservingSettings) {
        InternetMonitor.mainApplication = mainApplication;
        InternetMonitor.internetObservingSettings = internetObservingSettings;
    }

    /**
     * This function starts monitoring for internet connectivity changes. This method uses Reactive
     * approach and uses a {@link org.reactivestreams.Subscription} approach. A {@link Disposable}
     * keeps track of this subscription. Once started, internet monitoring will not be stopped until
     * an explicit call is made to {@link InternetMonitor#stopMonitoringInternet()}.
     *
     * @throws InitializationException if {@link InternetMonitor#init(MainApplication)} <b>OR</b>
     *                                 {@link InternetMonitor#init(MainApplication, InternetObservingSettings)} is not called prior
     *                                 to calling this.
     * @param applicationContext
     */
    public static void startMonitoringInternet(MainApplication applicationContext) throws InitializationException {
        checkValidConfig();
        Grove.d("Starting Monitoring");
        monitorSubscription = ReactiveNetwork
                .observeInternetConnectivity(internetObservingSettings)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isConnectedToHost -> {
                    Grove.d("Here, Checking N/W");
                    if (isConnectedToHost != lastConnectedState) {
                        Grove.d("Is Connected To Host ? %s", isConnectedToHost);
                        String message = isConnectedToHost ? "नेटवर्क उपलब्ध है" : "नेटवर्क नहीं उपलब्ध है";
                        InternetIndicatorOverlay.make(applicationContext, message, 5000).show();
                        lastConnectedState = isConnectedToHost;
                        InternetStatus status = new InternetStatus(isConnectedToHost, new Date());
                        mainApplication.getEventBus().send(new ExchangeObject.DataExchangeObject<InternetStatus>(Modules.MAIN_APP, Modules.COMMONS, status));

                    }
                }, throwable -> Grove.e(throwable, "Some error occurred %s", throwable.getMessage()));
    }

    /**
     * This function stops the internet connection monitoring. It is safe to call even if monitoring
     * is not yet started via {@link InternetMonitor#startMonitoringInternet(MainApplication)}. However an exception
     * will be thrown if this call is made without first initialising the class.
     *
     * @throws InitializationException if  {@link InternetMonitor#init(MainApplication)}
     *                                 <b>OR</b> {@link InternetMonitor#init(MainApplication, InternetObservingSettings)} is not
     *                                 called before calling this method.
     */
    public static void stopMonitoringInternet() throws InitializationException {
        checkValidConfig();
        if (!monitorSubscription.isDisposed()) {
            monitorSubscription.dispose();
            Grove.d("Monitor Disposed.");
        } else {
            Grove.w("Monitor Subscription already disposed.");
        }
    }

    /**
     * This function checks if the class is properly initialised.
     *
     * @throws InitializationException if mainApplication is null; this means that {@link InternetMonitor#init(MainApplication)}
     *                                 <b>OR</b> {@link InternetMonitor#init(MainApplication, InternetObservingSettings)} is not called.
     */
    private static void checkValidConfig() throws InitializationException {
        if (mainApplication == null) {
            throw new InitializationException(InternetMonitor.class, "InternetMonitor not initialised. Please call init method.");
        }
    }
}
