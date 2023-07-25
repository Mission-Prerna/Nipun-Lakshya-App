package com.samagra.parent.ui.splash;

import static com.samagra.commons.posthog.PostHogEventKt.APP_ID;
import static com.samagra.commons.posthog.PostHogEventKt.EID_IMPRESSION;
import static com.samagra.commons.posthog.PostHogEventKt.EVENT_FORCE_LOGOUT;
import static com.samagra.commons.posthog.PostHogEventKt.EVENT_TYPE_SYSTEM;
import static com.samagra.commons.posthog.PostHogEventKt.NL_APP_FORCE_LOGOUT;
import static com.samagra.commons.posthog.PostHogEventKt.SPLASH_SCREEN;
import static com.samagra.commons.utils.CommonUtilsKt.addFragment;
import static net.yslibrary.android.keyboardvisibilityevent.util.UIUtil.hideKeyboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.bumptech.glide.Glide;
import com.example.assets.uielements.CustomMessageDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.posthog.android.Properties;
import com.samagra.ancillaryscreens.data.pinverification.PinModel;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.ancillaryscreens.di.FormManagementCommunicator;
import com.samagra.ancillaryscreens.fcm.UpdateTokenWorker;
import com.samagra.ancillaryscreens.utils.TagConstants;
import com.samagra.commons.AppProgressDialog;
import com.samagra.commons.CommonUtilities;
import com.samagra.commons.constants.Constants;
import com.samagra.commons.constants.DeeplinkConstants;
import com.samagra.commons.models.Result;
import com.samagra.commons.models.schoolsresponsedata.SchoolsData;
import com.samagra.commons.posthog.LogEventsHelper;
import com.samagra.commons.posthog.PostHogManager;
import com.samagra.commons.posthog.data.Cdata;
import com.samagra.commons.prefs.SystemPreferences;
import com.samagra.commons.utils.RemoteConfigUtils;
import com.samagra.grove.logging.Grove;
import com.samagra.parent.AppConstants;
import com.samagra.parent.BuildConfig;
import com.samagra.parent.R;
import com.samagra.parent.UtilityFunctions;
import com.samagra.parent.authentication.AuthenticationActivity;
import com.samagra.parent.base.BaseActivity;
import com.samagra.parent.ui.assessmenthome.AssessmentHomeActivity;
import com.samagra.parent.ui.dietmentorassessmenttype.DIETAssessmentTypeActivity;
import com.samagra.parent.ui.privacy.PrivacyPolicyScreen;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * The View Part for the Splash Screen, must implement {@link SplashContract.View}
 * This Activity needs to be declared as the launcher activity in the AndroidManifest.xml
 *
 * @author Pranav Sharma
 */
@DeepLink(DeeplinkConstants.HOME)
public class SplashActivity extends BaseActivity implements SplashContract.View {

    @Inject
    SplashPresenter<SplashContract.View, SplashContract.Interactor> splashPresenter;
    private CommonsPrefsHelperImpl prefs;
    private AppProgressDialog logoutDialog;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.pbLoading);
        getActivityComponent().inject(this);
        splashPresenter.initRealm();
        setSplashBanner();
        prefs = new CommonsPrefsHelperImpl(this, "prefs");
        splashPresenter.onAttach(this);
        splashPresenter.setCrashCustomEvents(prefs);
        splashPresenter.requestStoragePermissions(getActivityContext().getPackageName(), getActivityContext().getPackageManager(), getActivityContext());
        FormManagementCommunicator.getContract().applyODKCollectSettings(
                this,
                com.samagra.ancillaryscreens.R.raw.settings
        );
        setUpTokenSyncer();
    }

    /**
     * and renders it on screen. This includes the Splash screen image and other UI configurations.
     * This function configures the Splash Screen
     */
    @Override
    public void showSimpleSplash() {
        TextView version = findViewById(R.id.tv_version);
        version.setText(UtilityFunctions.getVersionName(this));
        setEnableScopedStorage();

        // Check if force update is not required
        if (Integer.parseInt(splashPresenter.getMinimumViableVersion()) <= BuildConfig.VERSION_CODE) {
            long forceLogoutVersion = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtils.FORCE_LOGOUT_VERSION);
            long lastAppForceLogoutVersion = SystemPreferences.INSTANCE.getForceLogoutVersion();
            // Force Logout
            if (!CommonUtilities.isFirstInstall(this)
                    && forceLogoutVersion > BuildConfig.VERSION_CODE
                    && lastAppForceLogoutVersion != forceLogoutVersion
            ) {
                sendLogoutTelemetry(lastAppForceLogoutVersion, forceLogoutVersion);
                splashPresenter.forceLogout(this, prefs, forceLogoutVersion, this::setLoginFlow);
            } else {
                setLoginFlow();
            }
        } else {
            showForceUpdateDialog();
        }
    }

    private void sendLogoutTelemetry(long lastAppForceLogoutVersion, long serverForceLogoutVersion) {
        ArrayList<Cdata> list = new ArrayList<>();
        list.add(new Cdata("lastAppForceLogoutVersion", "" + lastAppForceLogoutVersion));
        list.add(new Cdata("serverForceLogoutVersion", "" + serverForceLogoutVersion));
        Properties properties = PostHogManager.INSTANCE.createProperties(
                SPLASH_SCREEN,
                EVENT_TYPE_SYSTEM,
                EID_IMPRESSION,
                PostHogManager.INSTANCE.createContext(APP_ID, NL_APP_FORCE_LOGOUT, list),
                null,
                null,
                null
        );
        PostHogManager.INSTANCE.capture(this, EVENT_FORCE_LOGOUT, properties);
    }

    private void updateJwtToken() {
        if (splashPresenter.isNetworkConnected()) {
            splashPresenter.updateJWTApi(BuildConfig.DEFAULT_FUSION_AUTH_API_KEY, new JwtResponseListener() {
                @Override
                public void onSuccess(JSONObject updatedToken) {
                    try {
                        prefs.saveAuthToken(updatedToken.getString("token"));
                        prefs.saveRefreshToken(updatedToken.getString("refreshToken"));
                        Timber.d("update Jwt Token onSuccess : openPinDialogFragment");
                    } catch (JSONException e) {
                        Timber.e("update Jwt Token onSuccess : catch block %s", e);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {
                    LogEventsHelper.setEventOnJwtFailure(SplashActivity.this);
                    Timber.e("update Jwt Token onFailure : openPinDialogFragment");
                }

            });
        } else {
            Timber.e("updateJwtToken call : no internet");
            setEnableScopedStorage();
        }
    }

    private void setLoginFlow() {
        if (prefs.getIsUserLoggedIn() && (prefs.getSelectedUser().equalsIgnoreCase(AppConstants.USER_MENTOR)
                || prefs.getSelectedUser().equalsIgnoreCase(AppConstants.USER_EXAMINER)
                || prefs.getSelectedUser().equalsIgnoreCase(Constants.USER_DIET_MENTOR)
                || prefs.getSelectedUser().equalsIgnoreCase(AppConstants.USER_TEACHER))) {
            updateJwtToken();
            splashPresenter.getIFormManagementContract().resetPreviousODKForms(this, failedResetActions -> {
            });
            splashPresenter.getIFormManagementContract().enableUsingScopedStorage();
            redirectToAssessmentHome();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::redirectToHomeScreen, 500);
        }
    }

    private void setEnableScopedStorage() {
        if (!splashPresenter.getIFormManagementContract().isScopedStorageUsed()) {
            splashPresenter.getIFormManagementContract().enableUsingScopedStorage();
        }
    }

    private void setSplashBanner() {
        ImageView imageVi = findViewById(R.id.splashqqq);
        Glide.with(getActivityContext()).load(R.drawable.ic_splash)
                .into(imageVi)
                .onLoadFailed(ContextCompat.getDrawable(getActivityContext(), R.drawable.ic_splash));
    }

    @Override
    public void finishSplashScreen() {
        progressBar.setVisibility(View.GONE);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        splashPresenter.onDetach();
    }


    @Override
    public void redirectToHomeScreen() {
        splashPresenter.getIFormManagementContract().enableUsingScopedStorage();
        if (splashPresenter.getMvpInteractor().isPrivacyPolicyAccepted()) {
            closeSplashScreen();
        } else {
            new CountDownTimer(500, 250) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    splashPresenter.getIFormManagementContract().resetEverythingODK(getActivityContext(), failedResetActions -> {
                        if (failedResetActions != null && failedResetActions.size() > 0) {
                            Grove.d("Number of failed Actions while starting app are: " + failedResetActions.size());
                        }
                        Grove.d("Redirecting to Home screen from Splash screen >>> ");
                        Intent intent = new Intent(getActivityContext(), PrivacyPolicyScreen.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        Grove.d("Closing Splash Screen");
                        finishSplashScreen();
                    });
                }
            }.start();
        }
    }


    @Override
    public void closeSplashScreen() {
        progressBar.setVisibility(View.GONE);
        splashPresenter.getMvpInteractor().getPreferenceHelper().updateProfileShown(false);
        splashPresenter.getIFormManagementContract().resetPreviousODKForms(getActivityContext(), this::redirectToAuthFlow);
    }

    @Override
    public void showLogoutLoader() {
        logoutDialog = new AppProgressDialog(this, getString(R.string.setting_up), null);
        logoutDialog.show();
    }

    @Override
    public void dismissLogoutLoader() {
        if (logoutDialog != null) {
            logoutDialog.dismiss();
        }
    }

    private void redirectToAuthFlow(List<Integer> failedResetActions) {
        if (failedResetActions != null && failedResetActions.size() > 0) {
            Grove.d("Number of failed Actions while starting app are: " + failedResetActions.size());
        }
        Grove.d("Redirecting to auth flow from Splash screen >>> ");
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
        Grove.d("Closing Splash Screen");
        finishSplashScreen();
    }

    @Override
    public void moveToNextScreen(String jwtToken) {
        closeSplashScreen();
    }

    @Override
    public void setupToolbar() {
    }

    /*@Override
    public void onBottomSheetSlideDown() {
        finish();
    }*/

    private void redirectToAssessmentHome() {
        Intent intent;
            if (prefs.getSelectedUser().equalsIgnoreCase(Constants.USER_DIET_MENTOR)) {
                intent = new Intent(this, DIETAssessmentTypeActivity.class);
            } else if (prefs.getSelectedUser().equalsIgnoreCase(AppConstants.USER_TEACHER)) {
                SchoolsData schoolsData = null;
                Result mentorDetail = prefs.getMentorDetailsData();
                if (mentorDetail != null) {
                    schoolsData = new SchoolsData(Long.parseLong(String.valueOf(mentorDetail.getUdise())), mentorDetail.getSchoolName(), mentorDetail.getSchoolId(), true, mentorDetail.getDistrict_name(),
                            mentorDetail.getSchoolDistrictId(), mentorDetail.getSchoolBlock(), mentorDetail.getSchoolBlockId(), mentorDetail.getSchoolNyayPanchayat(), mentorDetail.getSchoolNyayPanchayatId(), mentorDetail.getSchoolLat(), mentorDetail.getSchoolLong(), mentorDetail.getSchoolGeoFenceEnabled());
                }
                intent = new Intent(this, AssessmentHomeActivity.class);
                intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData);
            } else {
                if (prefs.getSelectedUser().equalsIgnoreCase(AppConstants.USER_EXAMINER)) {
                    prefs.saveAssessmentType(Constants.STATE_LED_ASSESSMENT);
                }
                intent = new Intent(this, AssessmentHomeActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
    }

    public void showForceUpdateDialog() {
        progressBar.setVisibility(View.GONE);
        CustomMessageDialog customDialog = new CustomMessageDialog(
                this, null,
                getString(R.string.youAreNotUpdatedTitle),
                getString(R.string.youAreNotUpdatedMessage)
        );
        customDialog.setOnFinishListener(getString(R.string.update), getString(R.string.exit), () ->
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())))
                , this::finish);
        customDialog.show();
    }

    private void setUpTokenSyncer() {
        long tokenSyncRepeatInDays = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtils.TOKEN_SYNC_REPEAT_IN_DAYS);
        Timber.d("setUpTokenSyncer: repeatIn "+tokenSyncRepeatInDays+" days");
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                UpdateTokenWorker.class,
                tokenSyncRepeatInDays,
                TimeUnit.DAYS
        ).build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("tokenSyncer",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        request);
    }

}
