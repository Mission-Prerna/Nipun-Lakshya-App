package org.odk.collect.android.contracts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.AlertDialogUtils;

import java.util.List;

import timber.log.Timber;

public class PermissionsHelper {
    public static boolean areStoragePermissionsGranted(Context context) {
        return isPermissionGranted(context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean isPhoneStatePermissionsGranted(Context context) {
        return isPermissionGranted(context,
                Manifest.permission.READ_PHONE_STATE);
    }


    /**
     * Returns true only if all of the requested permissions are granted to Collect, otherwise false
     */
    public static boolean isPermissionGranted(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    /**
     * Checks to see if the user granted Collect the permissions necessary for reading
     * and writing to storage and if not utilizes the permissions API to request them.
     *
     * @param activity needed for requesting permissions
     * @param action is a listener that provides the calling component with the permission result.
     */
    public void requestStoragePermissions(Activity activity, @NonNull AppPermissionUserActionListener action) {
        requestPermissions(activity, new AppPermissionUserActionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.storage_runtime_permission_denied_title,
                        R.string.storage_runtime_permission_denied_desc, R.drawable.sd, action);
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Checks to see if the user granted Collect the permissions necessary for reading phone state
     * and if not utilizes the permissions API to request them.
     *
     * @param activity needed for requesting permissions
     * @param action is a listener that provides the calling component with the permission result.
     */
    public void requestPhoneStatePermissions(Activity activity, @NonNull AppPermissionUserActionListener action) {
        requestPermissions(activity, new AppPermissionUserActionListener() {
            @Override
            public void granted() {
                action.granted();
            }

            @Override
            public void denied() {
                showAdditionalExplanation(activity, R.string.phone_state_runtime_permission_denied_title,
                        R.string.phone_state_runtime_permission_denied_desc, R.drawable.ic_phone, action);
            }
        }, Manifest.permission.READ_PHONE_STATE);
    }


    public void requestPermissions(Activity activity, @NonNull AppPermissionUserActionListener listener, String... permissions) {
        DexterBuilder builder = null;

        if (permissions.length == 1) {
            builder = createSinglePermissionRequest(activity, permissions[0], listener);
        } else if (permissions.length > 1) {
            builder = createMultiplePermissionsRequest(activity, listener, permissions);
        }

        if (builder != null) {
            builder.withErrorListener(error -> Timber.i(error.name())).check();
        }
    }


    private DexterBuilder createSinglePermissionRequest(Activity activity, String permission, AppPermissionUserActionListener listener) {
        Timber.i("permission DexterBuilder " + permission);
        return Dexter.withActivity(activity)
                .withPermission(permission)
                .withListener(new com.karumi.dexter.listener.single.PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Timber.i("permission DexterBuilder onPermissionGranted " + permission);
                        listener.granted();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Timber.i("permission DexterBuilder onPermissionDenied " + permission);
                        listener.denied();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        Timber.i("permission DexterBuilder onPermissionRationaleShouldBeShown " + permission);
                        token.continuePermissionRequest();
                    }
                });
    }

    private DexterBuilder createMultiplePermissionsRequest(Activity activity, AppPermissionUserActionListener listener, String[] permissions) {
        return Dexter.withActivity(activity)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            listener.granted();
                        } else {
                            listener.denied();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                });
    }

    protected void showAdditionalExplanation(Activity activity, int title, int message, int drawable, @NonNull AppPermissionUserActionListener action) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity, R.style.PermissionAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> action.denied())
                .setCancelable(false)
                .setIcon(drawable)
                .create();

        AlertDialogUtils.showDialog(alertDialog, activity);
    }

}
