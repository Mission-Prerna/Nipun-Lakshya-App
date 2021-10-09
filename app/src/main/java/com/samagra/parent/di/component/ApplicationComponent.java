package com.samagra.parent.di.component;

import android.content.Context;

import com.samagra.commons.MainApplication;
import com.samagra.parent.data.prefs.PreferenceHelper;
import com.samagra.parent.di.ApplicationContext;
import com.samagra.parent.di.modules.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * A @{@link Component} annotated interface defines connection between provider of objects (@{@link dagger.Module}
 * and the objects which express a dependency. It is implemented internally by Dagger at build time.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MainApplication application);

    @ApplicationContext
    Context getContext();

    MainApplication getApplication();

    PreferenceHelper preferenceHelper();
}
