package com.samagra.parent.ui.splash;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.androidnetworking.error.ANError;
import com.example.assets.uielements.CustomMessageDialog;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.ancillaryscreens.di.FormManagementCommunicator;
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
import com.samagra.parent.R;
import com.samagra.parent.base.BasePresenter;
import com.samagra.parent.helper.BackendNwHelper;
import com.samagra.parent.helper.RealmStoreHelper;

import org.odk.collect.android.contracts.AppPermissionUserActionListener;
import org.odk.collect.android.contracts.IFormManagementContract;
import org.odk.collect.android.contracts.PermissionsHelper;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * The presenter for the Splash Screen. This class controls the interactions between the View and the data.
 * Must implement {@link SplashContract.Presenter}
 *
 * @author Pranav Sharma
 */
@SuppressWarnings("deprecation")
public class SplashPresenter<V extends SplashContract.View, I extends SplashContract.Interactor> extends BasePresenter<V, I> implements SplashContract.Presenter<V, I> {
    private static final boolean EXIT = true;

    @Inject
    public SplashPresenter(I mvpInteractor, CompositeDisposable compositeDisposable, BackendNwHelper backendNwHelper, IFormManagementContract iFormManagementContract) {
        super(mvpInteractor, compositeDisposable, backendNwHelper, iFormManagementContract);
    }

    public boolean isJwtTokenValid() {
        return jwtTokenValid;
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
        Timber.i("crackit", "init 4");
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
        getIFormManagementContract().enableUsingScopedStorage();
        PermissionsHelper permissionUtils = new PermissionsHelper();
        if (!PermissionsHelper.areStoragePermissionsGranted(context) || !PermissionsHelper.isPhoneStatePermissionsGranted(context)) {
            Timber.i("permission 1 start");
            permissionUtils.requestPermissions((SplashActivity) context, new AppPermissionUserActionListener() {
                        @Override
                        public void granted() {
                            Timber.i("permission 1 success");
                            try {
                                getIFormManagementContract().createODKDirectories();
                            } catch (RuntimeException e) {
                                CustomMessageDialog customDialog = new CustomMessageDialog(
                                        context, ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_info),
                                        context.getString(R.string.unable_to_open_app_form_module_init_failure),
                                        null
                                );
                                customDialog.setOnFinishListener(() -> {
                                    System.exit(0);
                                });
                                customDialog.show();
                                return;
                            }
                            init(packageName, packageManager, context);
                        }

                        @Override
                        public void denied() {
                            if (!PermissionsHelper.areStoragePermissionsGranted(context)) {
                                showAdditionalExplanation((SplashActivity) context, org.odk.collect.android.R.string.storage_runtime_permission_denied_title,
                                        org.odk.collect.android.R.string.storage_runtime_permission_denied_desc, org.odk.collect.android.R.drawable.sd);
                            } else if (!PermissionsHelper.isPhoneStatePermissionsGranted(context)) {
                                showAdditionalExplanation((SplashActivity) context, org.odk.collect.android.R.string.phone_state_runtime_permission_denied_title,
                                        org.odk.collect.android.R.string.phone_state_runtime_permission_denied_desc, org.odk.collect.android.R.drawable.ic_phone);
                            } else {
                                ((SplashActivity) context).finishSplashScreen();
                            }
                        }
                    }, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE);
        } else {
            Timber.i("permission init only");
            init(packageName, packageManager, context);
        }
    }

    protected void showAdditionalExplanation(Activity activity, int title, int message, int drawable) {
        CustomMessageDialog customDialog = new CustomMessageDialog(
                activity, ContextCompat.getDrawable(activity, drawable),
                activity.getString(title),
                activity.getString(message)
        );
        customDialog.setOnFinishListener(() -> {
            ((SplashActivity) activity).finishSplashScreen();
        });
        customDialog.show();
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
        FormManagementCommunicator.getContract()
                .resetEverythingODK(
                        ctx, failedResetActions -> {
                            Grove.d("Failure to reset actions at Assessment Home screen $failedResetActions");
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
                );
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


