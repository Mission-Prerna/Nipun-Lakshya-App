package com.samagra.parent.di.modules;

import com.samagra.parent.di.PerActivity;
import com.samagra.parent.ui.splash.SplashContract;
import com.samagra.parent.ui.splash.SplashInteractor;
import com.samagra.parent.ui.splash.SplashPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.migration.DisableInstallInCheck;

/**
 * This module is similar to previous ones, it just uses Binds instead of Provides for better performance
 * Using Binds generates a lesser number of files during build times.
 */
@Module
@DisableInstallInCheck
public abstract class ActivityAbstractProviders {

    @Binds
    @PerActivity
    abstract SplashContract.Presenter<SplashContract.View, SplashContract.Interactor> provideSplashMvpPresenter(
            SplashPresenter<SplashContract.View, SplashContract.Interactor> presenter);

    @Binds
    @PerActivity
    abstract SplashContract.Interactor provideSplashMvpInteractor(SplashInteractor splashInteractor);

}
