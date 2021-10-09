package org.odk.collect.android.storage.migration;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.WebViewActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.material.MaterialFullScreenDialogFragment;
import org.odk.collect.android.preferences.AdminPasswordDialogFragment;
import org.odk.collect.android.preferences.AdminPasswordDialogFragment.Action;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.odk.collect.android.utilities.CustomTabHelper;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MultiClickGuard;

import javax.inject.Inject;

public class StorageMigrationDialog extends MaterialFullScreenDialogFragment {

    public static final String ARG_UNSENT_INSTANCES = "unsentInstances";

//    Button cancelButton;
    Button migrateButton;
    TextView messageText1;
    TextView errorText;
    LinearLayout progressBar;

    @Inject
    AdminPasswordProvider adminPasswordProvider;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.storage_migration_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            int unsentInstancesNumber = getArguments().getInt(ARG_UNSENT_INSTANCES);
        }

//        setUpToolbar();
        if (storageMigrationRepository.isMigrationBeingPerformed()) {
            disableDialog();
            showProgressBar();
        }


        migrateButton= view.findViewById(R.id.migrateButton);

        messageText1= view.findViewById(R.id.messageText1);


        errorText = view.findViewById(R.id.errorText);
        progressBar = view.findViewById(R.id.progressBar);
        migrateButton.setOnClickListener(v -> {
            if (MultiClickGuard.allowClick(getClass().getName())) {
                if (adminPasswordProvider.isAdminPasswordSet()) {
                    Bundle args = new Bundle();
                    args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.STORAGE_MIGRATION);
                    DialogUtils.showIfNotShowing(AdminPasswordDialogFragment.class, args, getActivity().getSupportFragmentManager());
                } else {
                    startStorageMigration();
                }
            }
        });
    }

    @Override
    protected void onCloseClicked() {
    }

    @Override
    protected void onBackPressed() {
    }

    @Nullable
    @Override
    protected Toolbar getToolbar() {
        return null;
    }


    private void disableDialog() {
        messageText1.setAlpha(.5f);
//        messageText3.setVisibility(View.GONE);
//
//        moreDetailsButton.setVisibility(View.GONE);
//
//        cancelButton.setEnabled(false);
//        cancelButton.setAlpha(.5f);

        migrateButton.setEnabled(false);
        migrateButton.setAlpha(.5f);

        errorText.setVisibility(View.GONE);
    }

    private void enableDialog() {
        messageText1.setAlpha(1);
//        messageText3.setVisibility(View.VISIBLE);

//        moreDetailsButton.setVisibility(View.VISIBLE);
//
//        cancelButton.setEnabled(true);
//        cancelButton.setAlpha(1);

        migrateButton.setEnabled(true);
        migrateButton.setAlpha(1);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void startStorageMigrationService() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, StorageMigrationService.class);
            activity.startService(intent);
        }
    }

    public void handleMigrationError(StorageMigrationResult result) {
        hideProgressBar();
        enableDialog();

        errorText.setVisibility(View.VISIBLE);
        errorText.setText(result.getErrorResultMessage(getContext()));
        migrateButton.setText(R.string.try_again);
    }

    public void startStorageMigration() {
        disableDialog();
        showProgressBar();
        startStorageMigrationService();
    }
}
