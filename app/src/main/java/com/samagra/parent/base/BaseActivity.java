package com.samagra.parent.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.samagra.commons.constants.Constants;
import com.samagra.parent.MyApplication;
import com.samagra.parent.di.component.ActivityComponent;
import com.samagra.parent.di.component.DaggerActivityComponent;
import com.samagra.parent.di.modules.ActivityModule;

import org.odk.collect.android.utilities.LocaleHelper;

import static android.content.pm.PackageManager.GET_META_DATA;


/**
 * This abstract class serves as the Base for all other activities used in this module. The class is
 * designed to support MVP Pattern with Dagger support. Any methods that need to be executed in all
 * activities, must be mentioned here. App level configuration changes (like theme change, language change, etc)
 * can be easily made through a BaseActivity. This must implement {@link MvpView}.
 * Since the app module expresses a dependency on the odk-collect, this base activity must also
 * extend {@link AppCompatActivity}.
 *
 * @author Pranav Sharma
 */
public abstract class BaseActivity extends AppCompatActivity implements MvpView {

    private ActivityComponent activityComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitles();
    }


    public ActivityComponent getActivityComponent() {
        if (activityComponent == null) {
            activityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(MyApplication.get(this).getApplicationComponent())
                    .build();
        }
        return activityComponent;
    }


    protected void resetTitles() {
        try {
            ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new LocaleHelper().updateLocale(base, PreferenceManager.getDefaultSharedPreferences(base).getString(Constants.APP_LANGUAGE_KEY, "en")));
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void showSnackbar(String message, int duration) {
        Snackbar.make(findViewById(android.R.id.content), message, duration).show();
    }
}
