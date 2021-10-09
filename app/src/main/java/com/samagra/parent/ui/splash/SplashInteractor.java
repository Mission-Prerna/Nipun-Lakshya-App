package com.samagra.parent.ui.splash;

import android.content.pm.PackageInfo;

import com.samagra.commons.utils.RemoteConfigUtils;
import com.samagra.parent.base.BaseInteractor;
import com.samagra.parent.data.prefs.PreferenceHelper;
import javax.inject.Inject;


/**
 * This class interacts with the {@link SplashContract.Presenter} and the stored
 * app data. The class abstracts the source of the originating data - This means {@link SplashContract.Presenter}
 * has no idea if the data provided by the {@link SplashContract.Interactor} is
 * from network, database or SharedPreferences
 *
 * @author Pranav Sharma
 */
public class SplashInteractor extends BaseInteractor implements SplashContract.Interactor {

    @Inject
    public SplashInteractor(PreferenceHelper preferenceHelper) {
        super(preferenceHelper);
    }

    @Override
    public boolean isFirstRun() {
        return getPreferenceHelper().isFirstRun();
    }

    @Override
    public boolean isShowSplash() {
        return getPreferenceHelper().isShowSplash();
    }

    @Override
    public boolean isLoggedIn() {
        return getPreferenceHelper().isLoggedIn();
    }

    @Override
    public String getRefreshToken() {
        return getPreferenceHelper().getRefreshToken();
    }

    /**
     * This function updates the version number and sets firstRun flag to true.
     * Call this method if you have for some reason updated the version code of the app.
     *
     * @param packageInfo - {@link PackageInfo} to get the the current version code of the app.
     * @return boolean - {@code true} if current package version code is higher than the stored version code
     * (indicating an app update), {@code false} otherwise
     */
    @Override
    public boolean updateVersionNumber(PackageInfo packageInfo) {
        if (getPreferenceHelper().getLastAppVersion() < packageInfo.versionCode) {
            getPreferenceHelper().updateLastAppVersion(packageInfo.versionCode);
            return true;
        }
        return false;
    }

    /**
     * Updates the first Run flag according to the conditions.
     *
     * @param value - the updated value of the first run flag
     */
    @Override
    public void updateFirstRunFlag(boolean value) {
        getPreferenceHelper().updateFirstRunFlag(value);
    }

    @Override
    public boolean isPrivacyPolicyAccepted() {
        return  getPreferenceHelper().isPrivacyPolicyAccepted();
    }

    @Override
    public String getMinimumViableVersion() {
       return RemoteConfigUtils.getFirebaseRemoteConfigInstance().getString(RemoteConfigUtils.MINIMUM_VIABLE_VERSION);
    }
}

