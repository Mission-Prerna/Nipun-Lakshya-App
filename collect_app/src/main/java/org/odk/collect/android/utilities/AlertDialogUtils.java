package org.odk.collect.android.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;


import org.odk.collect.android.R;

import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class AlertDialogUtils {

    private AlertDialogUtils() {
    }

    /**
     * Ensures that a dialog is shown safely and doesn't causes a crash. Useful in the event
     * of a screen rotation, async operations or activity navigation.
     *
     * @param dialog   that needs to be shown
     * @param activity that has the dialog
     */
    public static void showDialog(Dialog dialog, Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (dialog == null || dialog.isShowing()) {
            return;
        }

        try {
            dialog.show();
        } catch (Exception e) {
            Timber.e(e);
        }
    }


    /**
     * Creates an error dialog on an activity
     *
     * @param errorMsg The message to show on the dialog box
     * @param shouldExit Finish the activity if Ok is clicked
     */
    public static Dialog createErrorDialog(@NonNull Activity activity, String errorMsg, final boolean shouldExit) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = (dialog, i) -> {
            if (i == BUTTON_POSITIVE) {
                if (shouldExit) {
                    activity.finish();
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, activity.getString(R.string.ok), errorListener);

        return alertDialog;
    }


    /**
     * Creates an error dialog on an activity
     *
     * @param context Context Instance
     * @param errorMsg The message to show on the dialog box
     * @param shouldExit Finish the activity if Ok is clicked
     */
    public static Dialog createErrorDialog(@NonNull Context context, String errorMsg, final boolean shouldExit) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = (dialog, i) -> {
            if (i == BUTTON_POSITIVE) {
                if (shouldExit) {
                    System.exit(0);
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, context.getResources().getString(R.string.ok), errorListener);

        return alertDialog;
    }


}
