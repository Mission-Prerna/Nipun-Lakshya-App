package com.samagra.commons.notifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.samagra.grove.logging.Grove;


public class SimpleDialog extends DialogFragment {

    public static final String COLLECT_DIALOG_TAG = "collectDialogTag";

    private static final String DIALOG_TITLE = "dialogTitle";
    private static final String ICON_ID = "iconId";
    private static final String MESSAGE = "message";
    private static final String BUTTON_TITLE = "buttonTitle";
    private static final String FINISH_ACTIVITY = "finishActivity";

    public static SimpleDialog newInstance(String dialogTitle, int iconId, String message, String buttonTitle, boolean finishActivity) {
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_TITLE, dialogTitle);
        bundle.putInt(ICON_ID, iconId);
        bundle.putString(MESSAGE, message);
        bundle.putString(BUTTON_TITLE, buttonTitle);
        bundle.putBoolean(FINISH_ACTIVITY, finishActivity);

        SimpleDialog dialogFragment = new SimpleDialog();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    /*
    We keep this just in case to avoid problems if someone tries to show a dialog after
    the activity’s state have been saved. Basically it shouldn't take place since we should control
    the activity state if we want to show a dialog (especially after long tasks).
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            manager
                    .beginTransaction()
                    .add(this, tag)
                    .commit();
        } catch (IllegalStateException e) {
            Grove.e(e);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(DIALOG_TITLE))
                .setIcon(getArguments().getInt(ICON_ID))
                .setMessage(getArguments().getString(MESSAGE))
                .setPositiveButton(getArguments().getString(BUTTON_TITLE), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (getArguments().getBoolean(FINISH_ACTIVITY)) {
                            getActivity().finish();
                        }
                    }
                })
                .create();
    }

}
