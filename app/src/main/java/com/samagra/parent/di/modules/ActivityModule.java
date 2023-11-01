package com.samagra.parent.di.modules;

import android.app.Activity;
import android.content.Context;

import com.samagra.ancillaryscreens.di.FormManagementCommunicator;
import com.samagra.parent.di.ActivityContext;
import com.samagra.parent.helper.BackendNwHelper;
import com.samagra.parent.helper.BackendNwHelperImpl;

import org.odk.collect.android.contracts.IFormManagementContract;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.migration.DisableInstallInCheck;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Classes marked with @{@link Module} are responsible for providing objects that can be injected.
 * Such classes define methods annotated with @{@link Provides}. The returned objects from such methods are
 * available for DI.
 */
@Module
@DisableInstallInCheck
public class ActivityModule {

    private Activity activity;

    public ActivityModule(Activity activity) {
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
    IFormManagementContract provideIFormManagementContract() {
        return FormManagementCommunicator.getContract();
    }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    BackendNwHelper provideApiHelper() {
        return BackendNwHelperImpl.getInstance();
    }
}