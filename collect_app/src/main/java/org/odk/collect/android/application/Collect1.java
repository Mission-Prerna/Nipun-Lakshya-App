package org.odk.collect.android.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.annotation.Nullable;
import androidx.multidex.BuildConfig;

import com.google.gson.Gson;
import com.samagra.commons.MainApplication;

import org.odk.collect.android.ODKDriver;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.location.client.GoogleFusedLocationClient;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormListDownloader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Locale;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.MetaKeys.KEY_GOOGLE_BUG_154855417_FIXED;

public class Collect1 {

    private static Collect1 singleton = null;
    private static Application applicationVal = null;
    private static Context appContext =null;
    @Inject
    public
    StorageMigrationRepository storageMigrationRepository;

    public MainApplication getMainApplication() {
        return mainApplication;
    }

    private MainApplication mainApplication;

    public StorageStateProvider getStorageStateProvider() {
        return storageStateProvider;
    }

    @Inject
    StorageStateProvider storageStateProvider;
    private AppDependencyComponent applicationComponent;
    @Inject
    PreferencesProvider preferencesProvider;
    @Inject
    ApplicationInitializer applicationInitializer;

    public SettingsImporter getSettingsImporter() {
        return settingsImporter;
    }

    @Inject
    SettingsImporter settingsImporter;

    public static GoogleFusedLocationClient getGoogleFusedLocationClient() {
        return googleFusedLocationClient;
    }

    public static void setGoogleFusedLocationClient(GoogleFusedLocationClient googleFusedLocationClient) {
        Collect1.googleFusedLocationClient = googleFusedLocationClient;
    }

    private static GoogleFusedLocationClient googleFusedLocationClient;


    public StoragePathProvider getStoragePathProvider() {
        return storagePathProvider;
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    FormListDownloader formListDownloader;

    @Inject
    Analytics analytics;

    @Nullable
    private FormController formController;
    public static String defaultSysLanguage;
    private ExternalDataManager externalDataManager;

    private void setupDagger(Application appContext) {
        applicationComponent = DaggerAppDependencyComponent.builder()
                .application(appContext)
                .build();

        applicationComponent.inject(this);
    }


    public ExternalDataManager getExternalDataManager() {
        return externalDataManager;
    }

    public void setExternalDataManager(ExternalDataManager externalDataManager) {
        this.externalDataManager = externalDataManager;
    }


    @Nullable
    public FormController getFormController() {
        return formController;
    }

    public void setFormController(@Nullable FormController controller) {
        formController = controller;
    }



    public static Collect1 getInstance() {
        if (singleton == null)
            singleton = new Collect1();
        return singleton;
    }

    public Application getApplicationVal() {
        return applicationVal;
    }

    public Context getAppContext() {
        return appContext;
    }


    private void fixGoogleBug154855417(Context context) {
        try {
            SharedPreferences metaSharedPreferences = preferencesProvider.getMetaSharedPreferences();

            boolean hasFixedGoogleBug154855417 = metaSharedPreferences.getBoolean(KEY_GOOGLE_BUG_154855417_FIXED, false);

            if (!hasFixedGoogleBug154855417) {
                File corruptedZoomTables = new File(context.getFilesDir(), "ZoomTables.data");
                corruptedZoomTables.delete();

                metaSharedPreferences
                        .edit()
                        .putBoolean(KEY_GOOGLE_BUG_154855417_FIXED, true)
                        .apply();
            }
        } catch (Exception ignored) {
            // ignored
        }
    }


    /**
     * Predicate that tests whether a directory path might refer to an
     * ODK Tables instance data directory (e.g., for media attachments).
     */
    public static boolean isODKTablesInstanceDataDirectory(File directory) {
        /*
         * Special check to prevent deletion of files that
         * could be in use by ODK Tables.
         */
        String dirPath = directory.getAbsolutePath();
        StoragePathProvider storagePathProvider = new StoragePathProvider();
        if (dirPath.startsWith(storagePathProvider.getStorageRootDirPath())) {
            dirPath = dirPath.substring(storagePathProvider.getStorageRootDirPath().length());
            String[] parts = dirPath.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
            // [appName, instances, tableId, instanceId ]
            return parts.length == 4 && parts[1].equals("instances");
        }
        return false;
    }

        /* Enable StrictMode and log violations to the system log.
     * This catches disk and network access on the main thread, as well as leaked SQLite
     * cursors and unclosed resources.
            */
    private void setupStrictMode() {
        if (BuildConfig.DEBUG) {
            android.os.StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .permitDiskReads()  // shared preferences are being read on main thread
                    .penaltyLog()
                    .build());
            android.os.StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    public void init(Application application, Context context, FormManagmentModuleInitialisationListener informer,
                     MainApplication mainApplication, int login_bg, int baseAppTheme,
                     int formEntryActivityTheme, int baseAppTheme_SettingsTheme_Dark, long maxValue) {
        if(application != null && context != null) {
            applicationVal = application;
            appContext = context;
            this.mainApplication = mainApplication;
            setupDagger(application);
            applicationInitializer.initialize();

            fixGoogleBug154855417(context);

            setupStrictMode();
            defaultSysLanguage = Locale.getDefault().getLanguage();
            init1(mainApplication,context, login_bg,baseAppTheme, formEntryActivityTheme,baseAppTheme_SettingsTheme_Dark, maxValue);
            informer.onSuccess();
            googleFusedLocationClient  = new GoogleFusedLocationClient(getApplicationVal());

        }else{
            informer.onFailure("Failed");
        }
    }

    public void init1(MainApplication myApplication, Context applicationContext, int login_bg,
                      int baseAppTheme, int formEntryActivityTheme, int baseAppTheme_SettingsTheme_Dark, long maxValue) {
        ODKDriver.init(myApplication, login_bg, baseAppTheme,
                formEntryActivityTheme, baseAppTheme_SettingsTheme_Dark, Long.MAX_VALUE);
    }

    public AppDependencyComponent getComponent() {
        return applicationComponent;
    }

    public void setComponent(AppDependencyComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
        applicationComponent.inject(this);
    }

    /**
     * Gets a unique, privacy-preserving identifier for the current form.
     *
     * @return md5 hash of the form title, a space, the form ID
     */
    public static String getCurrentFormIdentifierHash() {
        FormController formController = getInstance().getFormController();
        if (formController != null) {
            return formController.getCurrentFormIdentifierHash();
        }

        return "";
    }

    /**
     * Gets a unique, privacy-preserving identifier for a form based on its id and version.
     *
     * @param formId      id of a form
     * @param formVersion version of a form
     * @return md5 hash of the form title, a space, the form ID
     */
    public static String getFormIdentifierHash(String formId, String formVersion) {
        String formIdentifier = new FormsDao().getFormTitleForFormIdAndFormVersion(formId, formVersion) + " " + formId;
        return FileUtils.getMd5Hash(new ByteArrayInputStream(formIdentifier.getBytes()));
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
    public FormListDownloader getDDon() {
        return formListDownloader;
    }
}
