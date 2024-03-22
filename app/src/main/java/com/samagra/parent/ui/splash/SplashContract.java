package com.samagra.parent.ui.splash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.commons.TaskCompleteListener;
import com.samagra.parent.base.MvpInteractor;
import com.samagra.parent.base.MvpPresenter;
import com.samagra.parent.base.MvpView;

/**
 * The interface contract for Splash Screen. This interface contains the methods that the Model, View & Presenter
 * for Splash Screen must implement
 *
 * @author Pranav Sharma
 */
public interface SplashContract {
    interface View extends MvpView {
        void showSimpleSplash();
        void finishSplashScreen();
        void redirectToHomeScreen();
        void closeSplashScreen();
        void showLogoutLoader();
        void dismissLogoutLoader();
        void moveToNextScreen(String jwtToken);
    }

    interface Interactor extends MvpInteractor {
        boolean isFirstRun();

        boolean isShowSplash();

        boolean isLoggedIn();

        String getRefreshToken();



        /**
         * This function updates the version number and sets firstRun flag to true.
         * Call this method if you have for some reason updated the version code of the app.
         *
         * @param packageInfo - {@link PackageInfo} to get the the current version code of the app.
         * @return boolean - {@code true} if current package version code is higher than the stored version code
         * (indicating an app update), {@code false} otherwise
         */
        boolean updateVersionNumber(PackageInfo packageInfo);

        /**
         * Updates the first Run flag according to the conditions.
         *
         * @param value - the updated value of the first run flag
         */
        void updateFirstRunFlag(boolean value);
        boolean isPrivacyPolicyAccepted();

        String getMinimumViableVersion();
    }

    interface Presenter<V extends View, I extends Interactor> extends MvpPresenter<V, I> {
        void requestStoragePermissions(String packageName, PackageManager packageManager, Context context);
        void verifyJWTTokenValidity(String apiKey,String jwtToken, Context context,String refreshToken);
        void updateJWT(String apiKey, String refreshToken,Context context);
        void updateJWTApi(String apiKey, JwtResponseListener listner);

        void initRealm();

        void forceLogout(Context ctx, CommonsPrefsHelperImpl prefs, long forceLogoutVersion, TaskCompleteListener listener);

        void setCrashCustomEvents(CommonsPrefsHelperImpl prefs);
    }
}
