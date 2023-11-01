package com.samagra.parent.di.modules;

import android.content.Context;

import com.samagra.commons.MainApplication;
import com.samagra.parent.AppConstants;
import com.samagra.parent.data.prefs.AppPreferenceHelper;
import com.samagra.parent.data.prefs.PreferenceHelper;
import com.samagra.parent.di.ApplicationContext;
import com.samagra.parent.di.PreferenceInfo;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.migration.DisableInstallInCheck;

@Module
@DisableInstallInCheck
public class ApplicationModule {

    private final MainApplication mainApplication;

    public ApplicationModule(MainApplication application) {
        this.mainApplication = application;
    }

    @ApplicationContext
    @Provides
    Context provideContext() {
        return mainApplication.getCurrentApplication().getApplicationContext();
    }

    @Provides
    MainApplication provideApplication() {
        return mainApplication;
    }

    @Singleton
    @Provides
    PreferenceHelper providePreferenceHelper(AppPreferenceHelper appPreferenceHelper) {
        return appPreferenceHelper;
    }

    @Provides
    @PreferenceInfo
    String providePreferenceFileName() {
        return AppConstants.PREF_FILE_NAME;
    }

}
