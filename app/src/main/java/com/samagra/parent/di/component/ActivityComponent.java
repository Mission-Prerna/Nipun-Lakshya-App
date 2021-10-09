package com.samagra.parent.di.component;

import com.samagra.parent.di.PerActivity;
import com.samagra.parent.di.modules.ActivityAbstractProviders;
import com.samagra.parent.di.modules.ActivityModule;
import com.samagra.parent.ui.splash.SplashActivity;

import dagger.Component;

/**
 * A @{@link Component} annotated interface defines connection between provider of objects (@{@link dagger.Module}
 * and the objects which express a dependency. It is implemented internally by Dagger at build time.
 */
@PerActivity
@Component(modules = {ActivityModule.class, ActivityAbstractProviders.class}, dependencies = {ApplicationComponent.class})
public interface ActivityComponent {

    void inject(SplashActivity splashActivity);


}
