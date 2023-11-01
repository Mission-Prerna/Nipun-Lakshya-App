package com.samagra.ancillaryscreens.di.modules;

import android.app.Activity;
import android.content.Context;

import com.samagra.ancillaryscreens.data.network.BackendCallHelper;
import com.samagra.ancillaryscreens.data.network.BackendCallHelperImpl;
import com.samagra.ancillaryscreens.data.prefs.CommonsPreferenceHelper;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.ancillaryscreens.di.ActivityContext;
import com.samagra.ancillaryscreens.di.ApplicationContext;
import com.samagra.ancillaryscreens.di.FormManagementCommunicator;
import com.samagra.ancillaryscreens.di.PreferenceInfo;
import com.samagra.commons.constants.Constants;

import org.odk.collect.android.contracts.IFormManagementContract;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.migration.DisableInstallInCheck;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Classes marked with @{@link Module} are responsible for providing objects that can be injected.
 * Such classes define methods annotated with @{@link Provides}. The returned objects from such methods are
 * available for DI.
 *
 * @author Pranav Sharma
 */
@Module
@DisableInstallInCheck
public class CommonsActivityModule {

    private Activity activity;

    public CommonsActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return activity;
    }

    @Provides
    Activity provideActivity() {
        return activity;
    }

    @Provides
    @ApplicationContext
    Context provideApplicationContext() {
        return activity.getApplication().getApplicationContext();
    }

    @Provides
    CommonsPreferenceHelper provideCommonsPreferenceHelper(CommonsPrefsHelperImpl commonsPrefsHelper) {
        return commonsPrefsHelper;
    }


    @Provides
    IFormManagementContract provideIFormManagementContract() {
        return FormManagementCommunicator.getContract();
    }


    @Provides
    @PreferenceInfo
    String providePreferenceFileName() {
        return Constants.COMMON_SHARED_PREFS_NAME;
    }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    BackendCallHelper provideApiHelper() {
        return BackendCallHelperImpl.getInstance();
    }

}
