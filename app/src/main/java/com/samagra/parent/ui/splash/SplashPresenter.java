package com.samagra.parent.ui.splash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.androidnetworking.error.ANError;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.commons.AppPreferences;
import com.samagra.commons.CommonUtilities;
import com.samagra.commons.TaskCompleteListener;
import com.samagra.commons.models.Result;
import com.samagra.commons.prefs.SystemPreferences;
import com.samagra.commons.utils.CustomEventCrashUtil;
import com.samagra.commons.utils.RemoteConfigListener;
import com.samagra.commons.utils.RemoteConfigUtils;
import com.samagra.grove.logging.Grove;
import com.samagra.parent.AppConstants;
import com.samagra.parent.BuildConfig;
import com.samagra.parent.base.BasePresenter;
import com.samagra.parent.helper.BackendNwHelper;
import com.samagra.parent.helper.RealmStoreHelper;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.samagra.odk.collect.extension.listeners.ODKProcessListener;
import io.samagra.odk.collect.extension.utilities.ODKProvider;
import timber.log.Timber;

/**
 * The presenter for the Splash Screen. This class controls the interactions between the View and the data.
 * Must implement {@link SplashContract.Presenter}
 *
 * @author Pranav Sharma
 */
@SuppressWarnings("deprecation")
public class SplashPresenter<V extends SplashContract.View, I extends SplashContract.Interactor> extends BasePresenter<V, I> implements SplashContract.Presenter<V, I> {

    @Inject
    public SplashPresenter(I mvpInteractor, CompositeDisposable compositeDisposable, BackendNwHelper backendNwHelper) {
        super(mvpInteractor, compositeDisposable, backendNwHelper);
    }

    private boolean jwtTokenValid = false;


    public boolean isNetworkConnected() {
        if (getMvpView() != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getMvpView()
                    .getActivityContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return true;
    }

    /**
     * This function initialises the {@link SplashActivity} by setting up the layout and updating necessary flags in
     * the {@link android.content.SharedPreferences}.
     */
    private void init(String packageName, PackageManager packageManager, Context context) {
        Timber.i("init 1");
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Grove.e(e, "Unable to get package info");
        }

        boolean firstRun = getMvpInteractor().isFirstRun();
        boolean showSplash = getMvpInteractor().isShowSplash();
        Timber.i("init 2");
        // if you've increased version code, then update the version number and set firstRun to true
        boolean appUpdated = getMvpInteractor().updateVersionNumber(packageInfo);
        if (appUpdated)
            firstRun = true;
        Timber.i("init 3");
        if (firstRun || showSplash)
            getMvpInteractor().updateFirstRunFlag(false);
        updateCurrentVersion();
        Timber.i("init 4");
        checkIfFirebaseRCInitialized(context);
        setSplash(context);
    }

    private void setSplash(Context context) {
        if (getMvpView() != null) {
            getMvpView().showSimpleSplash();
        } else if (context instanceof SplashActivity) {
            ((SplashActivity) context).showSimpleSplash();
        } else {
            Timber.e("from firebase initialization success, Splash screen context is null!");
        }
    }

    private void checkIfFirebaseRCInitialized(Context context) {
        if (isNetworkConnected()) {
            if (!RemoteConfigUtils.INSTANCE.isFirebaseInitialized()) {
                RemoteConfigUtils.INSTANCE.addFirebaseCallback(new RemoteConfigListener() {
                    @Override
                    public void onSuccess() {
                        Timber.e("firebaseInitialized remote callback success!");
                    }

                    @Override
                    public void onFailure() {
                    }
                });
            }
        }
    }

    @Override
    public void requestStoragePermissions(String packageName, PackageManager packageManager, Context context) {
        Timber.i("permissions");
        init(packageName, packageManager, context);
    }

    private void updateCurrentVersion() {
        int currentVersion = BuildConfig.VERSION_CODE;
        int previousSavedVersion = getMvpInteractor().getPreferenceHelper().getPreviousVersion();
        if (previousSavedVersion < currentVersion) {
            getMvpInteractor().getPreferenceHelper().updateAppVersion(currentVersion);
            Grove.e("Up version detected");
        }
    }

    @Override
    public void verifyJWTTokenValidity(String apiKey, String jwtToken, Context context, String refreshToken) {
        if (isNetworkConnected()) {
            getCompositeDisposable().add(getApiHelper()
                    .validateToken(jwtToken)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updatedToken -> {
                        if (updatedToken != null && updatedToken.has("jwt")) {
                            ((SplashActivity) context).moveToNextScreen(jwtToken);
                            Grove.e("JWT Token found to be valid for this user with value: " + updatedToken);
                        } else {
                            Grove.d("JWT Token expired for this user, trying to update the JWT Token");
                            updateJWT(apiKey, refreshToken, context);
                        }

                    }, throwable -> {
                        Grove.d("JWT Token network call failed for this user, trying to update the JWT Token");
                        updateJWT(apiKey, refreshToken, context);
                        Grove.e(throwable);
                    }));
        } else {
            ((SplashActivity) context).closeSplashScreen();
        }
    }

    @Override
    public void updateJWT(String apiKey, String refreshToken, Context context) {
        Grove.e(refreshToken);
        CommonsPrefsHelperImpl prefs = new CommonsPrefsHelperImpl(getMvpView().getActivityContext(), "prefs");
        getCompositeDisposable().add(getApiHelper()
                .refreshToken(apiKey, prefs)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updatedToken -> {
                    if (updatedToken != null && updatedToken.has("token")) {
                        AppConstants.JWTToken = updatedToken.getString("token");
                    }
                    ((SplashActivity) context).closeSplashScreen();
                }, throwable -> ((SplashActivity) context).closeSplashScreen()));

    }

    @Override
    public void updateJWTApi(String apiKey, JwtResponseListener listener) {
        boolean firstRun = getMvpInteractor().isFirstRun();
        Timber.d("get refreshToken if not first run: %s", firstRun);
        if (!firstRun) {
            CommonsPrefsHelperImpl prefs = new CommonsPrefsHelperImpl(getMvpView().getActivityContext(), "prefs");
            getCompositeDisposable().add(getApiHelper()
                    .refreshToken(apiKey, prefs)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updatedToken -> {
                        if (updatedToken != null && updatedToken.has("token") && updatedToken.has("refreshToken")) {
                            jwtTokenValid = true;
                            listener.onSuccess(updatedToken);
                        } else {
                            jwtTokenValid = false;
                            listener.onFailure();
                        }
                    }, throwable -> {
                        jwtTokenValid = false;
                        if (throwable instanceof ANError) {
                            Grove.e("ANError Received while fetching JWT Token with error " + throwable);
                        } else {
                            Grove.e("Fetch JWT Failed... " + throwable);
                        }
                        listener.onFailure();
                    }));
//            }
        }
    }

    @Override
    public void initRealm() {
        try {
            RealmStoreHelper.INSTANCE.getDefaultInstance();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void forceLogout(Context ctx, CommonsPrefsHelperImpl prefs, long forceLogoutVersion, TaskCompleteListener listener) {
        if (getMvpView() == null) return;
        prefs.clearData();
        AppPreferences.INSTANCE.clearLocal();
        AsyncTask.execute(() -> {
            RealmStoreHelper.INSTANCE.clearAllTables();
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    SystemPreferences.INSTANCE.setForceLogoutVersion(forceLogoutVersion);
                    listener.onComplete();
                    if (getMvpView() != null) {
                        getMvpView().dismissLogoutLoader();
                    }
                });
            }
        });
    }

    public void setCrashCustomEvents(CommonsPrefsHelperImpl prefs) {
        Result loggedInMentor = prefs.getMentorDetailsData();
        if (loggedInMentor != null && loggedInMentor.getId() != 0) {
            String mentorId = String.valueOf(loggedInMentor.getId());
            CommonUtilities.setCrashlyticsProperty(mentorId);
            CustomEventCrashUtil.setCrashPropsLoggedInUser(prefs.getSelectedUser(), mentorId, loggedInMentor.getDesignation_id(), loggedInMentor.getActorId());
        } else {
            CustomEventCrashUtil.setSelectedUserProperty(prefs.getSelectedUser());
        }
    }

    public String getMinimumViableVersion() {
        return getMvpInteractor().getMinimumViableVersion();
    }
}


