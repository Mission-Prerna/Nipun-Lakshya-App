package com.samagra.parent;

import static com.samagra.commons.constants.Constants.CHATBOT_SERVICES_BASE_URL;
import static com.samagra.commons.utils.RemoteConfigUtils.LOGIN_SERVICE_BASE_URL;
import static com.samagra.parent.AppConstants.BASE_API_URL;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_APP_LANGUAGE;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.assessment.flow.workflowengine.odk.AssessmentsFormCommunicator;
import com.chatbot.model.ChatbotUrlResponseObject;
import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.data.db.DbHelper;
import com.data.db.NLDatabase;
import com.example.assets.uielements.CustomMessageDialog;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.morziz.network.config.ClientType;
import com.morziz.network.config.NetworkConfig;
import com.morziz.network.network.ModuleDependency;
import com.morziz.network.network.Network;
import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.ancillaryscreens.di.FormManagementCommunicator;
import com.samagra.commons.AppPreferences;
import com.samagra.commons.AppProperties;
import com.samagra.commons.CommonUtilities;
import com.samagra.commons.EventBus;
import com.samagra.commons.ExchangeObject;
import com.samagra.commons.InternetMonitor;
import com.samagra.commons.InternetStatus;
import com.samagra.commons.MainApplication;
import com.samagra.commons.Modules;
import com.samagra.commons.NetworkConnectionInterceptor;
import com.samagra.commons.RxBus;
import com.samagra.commons.notifications.AppNotificationUtils;
import com.samagra.commons.posthog.PostHogManager;
import com.samagra.commons.prefs.SystemPreferences;
import com.samagra.commons.utils.CommonConstants;
import com.samagra.commons.utils.NetworkMonitoringUtil;
import com.samagra.commons.utils.RemoteConfigListener;
import com.samagra.commons.utils.RemoteConfigUtils;
import com.samagra.grove.contracts.GroveLoggingComponentLauncher;
import com.samagra.grove.contracts.IGroveLoggingComponent;
import com.samagra.grove.contracts.LoggingComponentManager;
import com.samagra.grove.logging.Grove;
import com.samagra.grove.logging.LoggableApplication;
import com.samagra.network.AppHeadersInterceptor;
import com.samagra.network.HasuraAuthorizationInterceptor;
import com.samagra.parent.di.component.ApplicationComponent;
import com.samagra.parent.di.component.DaggerApplicationComponent;
import com.samagra.parent.di.modules.ApplicationModule;
import com.samagra.parent.helper.CrashReportingTree;
import com.samagra.parent.helper.OkHttpClientProvider;
import com.samagra.parent.helper.SyncManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.application.Collect1;
import org.odk.collect.android.application.FormManagmentModuleInitialisationListener;
import org.odk.collect.android.contracts.ComponentManager;
import org.odk.collect.android.contracts.FormManagementSectionInteractor;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.LocaleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;


/**
 * The {@link Application} class for the app. This extends {@link Application} because the app module has a dependency on
 * the odk-collect library. Also, since the app module expresses a dependency on the commons module, the {@link Application}
 * class for app module must implement the {@link MainApplication}.
 *
 * @author Pranav Sharma
 * @see MainApplication
 */
@HiltAndroidApp
public class MyApplication extends Application implements MainApplication, LifecycleObserver, LoggableApplication {

    private static MyApplication app;
    protected ApplicationComponent applicationComponent;

    private Activity currentActivity = null;
    private RxBus eventBus = null;
    private EventBus rxEventBus = null;
    private static CompositeDisposable compositeDisposable = new CompositeDisposable();
    public static boolean isOnline = true;

    @Inject
    NLDatabase nlDatabase;

    /**
     * All the external modules must be initialised here. This includes any modules that have an init
     * function in their drivers. Also, any application level subscribers for the event bus,
     * in this case {@link RxBus} must be defined here.
     */
    @Override
    public void onCreate() {
        app = this;
        super.onCreate();
        eventBus = new RxBus();
        initialiseLoggingComponent();
        Collect1.getInstance().init(this, getApplicationContext(), new FormManagmentModuleInitialisationListener() {
                    @Override
                    public void onSuccess() {
                        Grove.d("Form Module has been initialised correctly");
                    }

                    @Override
                    public void onFailure(String message) {
                        Grove.d("Form Module could not be initialised correctly");
                        CustomMessageDialog customDialog = new CustomMessageDialog(
                                getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_dialog_info),
                                getApplicationContext().getString(R.string.unable_to_open_app_form_module_init_failure),
                                null
                        );
                        customDialog.setOnFinishListener(() -> {
                            System.exit(0);
                        });
                        customDialog.show();
                    }
                }, this, R.drawable.ic_splash, R.style.LoginTheme,
                R.style.FormEntryActivityTheme, R.style.BaseAppTheme_SettingsTheme_Dark, Long.MAX_VALUE);
        DbHelper.INSTANCE.setDb(nlDatabase);
        setupRemoteConfig();
        setupActivityLifecycleListeners();
        InternetMonitor.init(this);
        initializeFormManagementPackage();
        AppNotificationUtils.createNotificationChannel(this);
        AppPreferences.INSTANCE.init(this);
        SystemPreferences.INSTANCE.init(this);
        Grove.d("Initialising Ancillary Screens Module >>>>");
        AncillaryScreensDriver.init(this, BASE_API_URL);
        Grove.d("Ancillary Screens Module initialised >>>>");
        initBus();
//        UpdateDriver.init(this);
        rxEventBus = new EventBus();
        registerEventsTOGetNetworkStates();
        AppProperties.INSTANCE.setVersions(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME);
        Network.Companion.init(new ModuleDependency() {
            @NotNull
            @Override
            public String getBaseUrl(@NotNull String type) {
                return RemoteConfigUtils.getFirebaseRemoteConfigInstance().getString(RemoteConfigUtils.LOGIN_SERVICE_BASE_URL);
            }

            @Nullable
            @Override
            public HashMap<String, String> getHeaders() {
                return null;
            }

            @Override
            public void reValidateUer(int code) {

            }

            @Nullable
            @Override
            public String getGoogleKeys() {
                return null;
            }

            @NotNull
            @Override
            public Context getAppContext() {
                return getApplicationContext();
            }

            @Nullable
            @Override
            public List<Interceptor> getExtraInterceptors() {
                return null;
            }
        });
        Realm.init(this);
        downloadURLsRemoteConfig();
    }

    private void registerEventsTOGetNetworkStates() {
        NetworkMonitoringUtil monitoringUtil = new NetworkMonitoringUtil(getApplicationContext());
        monitoringUtil.checkNetworkState();
        monitoringUtil.registerNetworkCallbackEvents();
    }

    private void initialiseLoggingComponent() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
        LoggingComponentManager.registerGroveLoggingComponent(new GroveLoggingComponentLauncher());
        IGroveLoggingComponent initializer = LoggingComponentManager.iGroveLoggingComponent;
        if (initializer != null) {
            initializer.initializeLoggingComponent(this, this, getApplicationContext(), (context, s, s1, s2, s3) -> {
            }, true, true, AppConstants.SENDER_EMAIL_ID, AppConstants.RECEIVER_EMAIL_ID);
        }
        new UCEHandler.Builder(getCurrentApplication())
                .setTrackActivitiesEnabled(true)
                .setBackgroundModeEnabled(true)
                .build();
    }

    private void initializeFormManagementPackage() {
        Grove.d("Initialising Form Management Module >>>>");
        ComponentManager.registerFormManagementPackage(new FormManagementSectionInteractor());
        FormManagementCommunicator.setContract(ComponentManager.iFormManagementContract);
        AssessmentsFormCommunicator.setContract(ComponentManager.iFormManagementContract);
        Grove.d("Form Management Module initialised >>>>");
    }


    private void initBus() {
        compositeDisposable.add(this.getEventBus()
                .toObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(exchangeObject -> {
                    if (exchangeObject instanceof ExchangeObject) {
                        if (((ExchangeObject) exchangeObject).to == Modules.MAIN_APP
                                && ((ExchangeObject) exchangeObject).from == Modules.ANCILLARY_SCREENS
                                && isSignalExchangeType((ExchangeObject) exchangeObject)) {
                            ExchangeObject.SignalExchangeObject signalExchangeObject = (ExchangeObject.SignalExchangeObject) exchangeObject;
                            if (signalExchangeObject.shouldStartAsNewTask) {
                                Grove.d("Exchange event from %s intended to launch a new activity as a new task", currentActivity.getLocalClassName());
                                if (currentActivity != null) {
                                    CommonUtilities.startActivityAsNewTask(signalExchangeObject.intentToLaunch, currentActivity);
                                }
                            } else
                                startActivity(signalExchangeObject.intentToLaunch);
                        } else if (exchangeObject instanceof ExchangeObject.EventExchangeObject) {
                            // TODO : Remove this just for test
                            ExchangeObject.EventExchangeObject eventExchangeObject = (ExchangeObject.EventExchangeObject) exchangeObject;
                            Grove.d("Event Received as Event exchange object %s ", eventExchangeObject.customEvents);
                            if (eventExchangeObject.to == Modules.MAIN_APP || eventExchangeObject.to == Modules.PROJECT) {
                                Grove.d("Event Received from Main App to Project Module is %s ", eventExchangeObject.customEvents);
                            }
                        } else if (exchangeObject instanceof ExchangeObject.NotificationExchangeObject) {
                            PendingIntent pendingIntent = ((ExchangeObject.NotificationExchangeObject) exchangeObject).data.getIntent();
                            int notificationID = ((ExchangeObject.NotificationExchangeObject) exchangeObject).data.getNotificationID();
                            int title = ((ExchangeObject.NotificationExchangeObject) exchangeObject).data.getTitle();
                            String body = ((ExchangeObject.NotificationExchangeObject) exchangeObject).data.getBody();
                            AppNotificationUtils.showNotification(getApplicationContext(), pendingIntent, notificationID, title, body);
                            Grove.d("Event Received for Push Notification consumption is %s ", title);
                        } else if (exchangeObject instanceof ExchangeObject.DataExchangeObject && (((ExchangeObject.DataExchangeObject) exchangeObject).to == Modules.MAIN_APP) &&
                                (((ExchangeObject.DataExchangeObject) exchangeObject).from == Modules.COMMONS) && (((ExchangeObject.DataExchangeObject) exchangeObject).data instanceof InternetStatus)) {
                            if (((ExchangeObject.DataExchangeObject) exchangeObject).data != null) {
                                InternetStatus internetStatus = (InternetStatus) ((ExchangeObject.DataExchangeObject) exchangeObject).data;
                                boolean status = internetStatus.isCurrentStatus();
                                updateInternetStatus(status);
                            }
                        } else if (!((((ExchangeObject) exchangeObject).type != ExchangeObject.ExchangeObjectTypes.SIGNAL)
                                && ((InternetStatus) ((ExchangeObject.DataExchangeObject) exchangeObject).data).isCurrentStatus())) {
                            try {
                                boolean status = ((InternetStatus) ((ExchangeObject.DataExchangeObject) exchangeObject).data).isCurrentStatus();
                                updateInternetStatus(status);
                            } catch (Exception e) {
                            }
                        } else {
                            Grove.e("Exchange Object received but not intended hence not mapped as per conditions, hence couldn't be consumed.");
                            Grove.e("Unconsumed exchange object values is %s", exchangeObject);
                        }
                    }
                }, Grove::e));
    }

    private boolean isSignalExchangeType(ExchangeObject exchangeObject) {
        return exchangeObject.type == ExchangeObject.ExchangeObjectTypes.SIGNAL;
    }

    @Override
    public void updateInternetStatus(Boolean status) {
        isOnline = status;
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public OkHttpClient provideOkHttpClient() {
        NetworkConnectionInterceptor networkConnectionInterceptor = OkHttpClientProvider.getInterceptor(this);
        return OkHttpClientProvider.provideOkHttpClient(networkConnectionInterceptor);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    public static MyApplication getInstance() {
        return app;
    }

    @Override
    public Application getLoggableApplication() {
        return this;
    }

    /**
     * Must provide a {@link androidx.annotation.NonNull} activity instance of the activity running in foreground.
     * You can use {@link Application#registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks)} to
     * get the currently resumed activity (activity in foreground)
     */
    @Override
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Must provide a {@link androidx.annotation.NonNull} instance of the current {@link Application}.
     */
    @Override
    public Application getCurrentApplication() {
        return this;
    }

    /**
     * Must provide a {@link androidx.annotation.NonNull} instance of {@link RxBus} which acts as an event bus
     * for the app.
     */
    @Override
    public RxBus getEventBus() {
        return bus();
    }

    /**
     * Optional method to teardown a module after its use is complete.
     * Not all modules require to be teared down.
     */
    @Override
    public void teardownModule(Modules module) {

    }

    @Override
    public EventBus eventBusInstance() {
        return rxEventBus;
    }

    private void setupActivityLifecycleListeners() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (activity != null) {
                    currentActivity = activity;
                    Timber.d("onCreate() called for Activity ... " + activity.getLocalClassName());
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activity != null) {
                    currentActivity = activity;
                    Timber.d("onStart() called for Activity ... " + activity.getLocalClassName());
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
                if (activity != null) {
                    Timber.d("onResume() called for Activity ... " + activity.getLocalClassName());
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Timber.d("onActivityPaused() called for Activity ... " + activity != null ? activity.getLocalClassName() : "No Activity Found");
                currentActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Timber.d("onActivityStopped() called for Activity ... " + activity != null ? activity.getLocalClassName() : "No Activity Founr");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Timber.d("onActivitySaveInstanceState() called for Activity ... " + activity != null ? activity.getLocalClassName() : "No Activity Founr");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Timber.d("onActivityDestroyed() called for Activity ... " + activity != null ? activity.getLocalClassName() : "No Activity Founr");
            }
        });
    }

    private void setupRemoteConfig() {
        RemoteConfigUtils.INSTANCE.init(new RemoteConfigListener() {
            @Override
            public void onFailure() {
                Timber.e("remote App initialization failure!");
            }

            @Override
            public void onSuccess() {
                Timber.e("remote App initialization successful!");
                downloadURLsRemoteConfig();
            }
        });
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
    }

    private void downloadURLsRemoteConfig() {
        FirebaseRemoteConfig firebaseRemoteConfig = RemoteConfigUtils.getFirebaseRemoteConfigInstance();
        String postHogServerUrl = firebaseRemoteConfig.getString(RemoteConfigUtils.POSTHOG_SERVER_URL);
        String postHogServerApiKey = firebaseRemoteConfig.getString(RemoteConfigUtils.POSTHOG_SERVER_API_KEY);
        String odkServerUrl = BuildConfig.DEBUG ? BuildConfig.STAGING_ODK_SERVER_URL
                : firebaseRemoteConfig.getString(RemoteConfigUtils.ODK_SERVER_URL);
        String odkServerSubmissionUrl = firebaseRemoteConfig.getString(RemoteConfigUtils.ODK_SERVER_SUBMISSION_URL);
        CommonsPrefsHelperImpl prefs = new CommonsPrefsHelperImpl(getCurrentApplication(), "prefs");
        Timber.d("downloadURLsRemoteConfig: postHogServerUrl: %s", postHogServerUrl);
        Timber.d("downloadURLsRemoteConfig: odkServerUrl: %s", odkServerUrl);
        Timber.d("downloadURLsRemoteConfig: odkServerSubmissionUrl: %s", odkServerSubmissionUrl);
        String authToken =
                firebaseRemoteConfig.getString(RemoteConfigUtils.HASURA_SERVER_AUTH_TOKEN);

        if (!prefs.getAuthToken().isEmpty()) {
            authToken = prefs.getAuthToken();
        }
        if (prefs.isLoggedIn()) {
            prefs.saveAuthToken(authToken);
        }
        prefs.saveOdkServerUrl(odkServerUrl);
        prefs.saveOdkServerSubmissionUrl(odkServerSubmissionUrl);
        FormManagementCommunicator.getContract().setServerUrl(odkServerUrl);
        PostHogManager.init(MyApplication.this, postHogServerUrl, postHogServerApiKey);
        setupNetworkConfig(firebaseRemoteConfig, authToken);
        SyncManager.INSTANCE.init(this);
    }

    private void setupNetworkConfig(FirebaseRemoteConfig firebaseRemoteConfig, String authToken) {
        String loginServiceUrl = BuildConfig.DEBUG ? BuildConfig.STAGING_LOGINSERVICE_URL
                : firebaseRemoteConfig.getString(LOGIN_SERVICE_BASE_URL);
        String appServiceUrl = BuildConfig.DEBUG ? BuildConfig.DEFAULT_API_BASE_URL
                : firebaseRemoteConfig.getString(RemoteConfigUtils.API_BASE_URL);
        String hasuraUrl = BuildConfig.DEBUG ? BuildConfig.STAGING_HASURA_URL
                : firebaseRemoteConfig.getString(RemoteConfigUtils.HASURA_SERVER_BASE_URL);
        String chatbotUrlString = BuildConfig.DEBUG ? BuildConfig.DEFAULT_CHATBOT_URLS
                : firebaseRemoteConfig.getString(RemoteConfigUtils.CHATBOT_URLS);

        /*String loginServiceUrl = firebaseRemoteConfig.getString(LOGIN_SERVICE_BASE_URL);
        String appServiceUrl = firebaseRemoteConfig.getString(RemoteConfigUtils.API_BASE_URL);
        String hasuraUrl = firebaseRemoteConfig.getString(RemoteConfigUtils.HASURA_SERVER_BASE_URL);
        String chatbotUrlString = firebaseRemoteConfig.getString(RemoteConfigUtils.CHATBOT_URLS);*/

        Timber.d("setupNetworkConfig: loginServiceUrl: %s", loginServiceUrl);
        Timber.d("setupNetworkConfig: appServiceUrl: %s", appServiceUrl);
        Timber.d("setupNetworkConfig: hasuraUrl: %s", hasuraUrl);
        Timber.d("setupNetworkConfig: chatbotUrls: %s", chatbotUrlString);

        ChuckerInterceptor chuckerInterceptor = new ChuckerInterceptor(this);

        List<Interceptor> hasuraInterceptors = new ArrayList<>();
        hasuraInterceptors.add(new HasuraAuthorizationInterceptor());
        hasuraInterceptors.add(chuckerInterceptor);
        NetworkConfig graphqlProdConfig = new NetworkConfig.Builder()
                .baseUrl(hasuraUrl)
                .identity(CommonConstants.IDENTITY_HASURA)
                .interceptors(hasuraInterceptors)
                .clientType(ClientType.GRAPHQL).build();
        Network.Companion.addNetworkConfig(graphqlProdConfig);

        ChatbotUrlResponseObject chatbotUrlResponseObject = ChatbotUrlResponseObject.fromJson(
                chatbotUrlString,
                new Gson()
        );
        NetworkConfig uciTelemetryConfig = new NetworkConfig.Builder()
                .baseUrl(chatbotUrlResponseObject.getServicesUrl())
                .identity(CHATBOT_SERVICES_BASE_URL)
                .clientType(ClientType.RETROFIT).build();
        Network.Companion.addNetworkConfig(uciTelemetryConfig);

        AppHeadersInterceptor appHeadersInterceptor = new AppHeadersInterceptor(this);

        //Login service
        ArrayList<Interceptor> loginServiceInterceptors = new ArrayList<>();
        loginServiceInterceptors.add(appHeadersInterceptor);
        loginServiceInterceptors.add(chuckerInterceptor);
        NetworkConfig loginServiceConfig = new NetworkConfig.Builder()
                .baseUrl(loginServiceUrl)
                .identity(LOGIN_SERVICE_BASE_URL)
                .interceptors(loginServiceInterceptors)
                .clientType(ClientType.RETROFIT).build();
        Network.Companion.addNetworkConfig(loginServiceConfig);

        //API service
        ArrayList<Interceptor> appServiceInterceptors = new ArrayList<>();
        appServiceInterceptors.add(appHeadersInterceptor);
        appServiceInterceptors.add(chuckerInterceptor);
        // TODO :: Inject authorization via header and remove from api method calls
//        appServiceInterceptors.add(new HasuraAuthorizationInterceptor());
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            appServiceInterceptors.add(logging);
        }
        NetworkConfig apiServiceConfig = new NetworkConfig.Builder()
                .baseUrl(appServiceUrl)
                .identity(CommonConstants.IDENTITY_APP_SERVICE)
                .interceptors(appServiceInterceptors)
                .clientType(ClientType.RETROFIT)
                .build();
        Network.Companion.addNetworkConfig(apiServiceConfig);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Collect1.defaultSysLanguage = newConfig.locale.getLanguage();
        boolean isUsingSysLanguage = GeneralSharedPreferences.getInstance().get(KEY_APP_LANGUAGE).equals("");
        if (!isUsingSysLanguage) {
            Grove.d("Changing App language to: " + newConfig.locale.getLanguage());
            new LocaleHelper().updateLocale(this);
        }
    }

    private RxBus bus() {
        return eventBus;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackgrounded() {
        InternetMonitor.stopMonitoringInternet();
        if (compositeDisposable != null && !compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }


    /**
     * Returns the Lifecycle of the provider.
     *
     * @return The lifecycle of the provider.
     */
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return ProcessLifecycleOwner.get().getLifecycle();
    }

}