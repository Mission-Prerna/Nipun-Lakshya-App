/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.application.Collect1;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.contracts.CSVBuildStatusListener;
import org.odk.collect.android.contracts.CSVHelper;
import org.odk.collect.android.contracts.FormManagementSectionInteractor;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.material.MaterialBanner;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPasswordDialogFragment;
import org.odk.collect.android.preferences.AdminPasswordDialogFragment.Action;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.migration.StorageMigrationDialog;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.odk.collect.android.utilities.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.utilities.DialogUtils.getDialog;
import static org.odk.collect.android.utilities.DialogUtils.showIfNotShowing;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends CollectAbstractActivity implements AdminPasswordDialogFragment.AdminPasswordDialogCallback {
    private static final boolean EXIT = true;
    // buttons
    private Button manageFilesButton;
    private Button sendDataButton;
    private Button viewSentFormsButton;
    private Button reviewDataButton;
    private Button getFormsButton;
    private AlertDialog alertDialog;
    private MenuItem qrcodeScannerMenuItem;
    private int completedCount;
    private int savedCount;
    private int viewSentCount;
    private Cursor finalizedCursor;
    private Cursor savedCursor;
    private Cursor viewSentCursor;
    private final IncomingHandler handler = new IncomingHandler(this);
    private final MyContentObserver contentObserver = new MyContentObserver();

    @Inject
    public Analytics analytics;

    MaterialBanner storageMigrationBanner;


    TextView versionSHAView;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    StorageStateProvider storageStateProvider;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    AdminPasswordProvider adminPasswordProvider;

    @Inject
    SettingsImporter settingsImporter;

    @Inject
    MainMenuViewModel.Factory viewModelFactory;
    private MainMenuViewModel viewModel;


    private JSONArray buildJSONArray() {
        String jsonString = "{\"status\":\"Success\",\"data\":[{\"index\":55506,\"id\":55506,\"udise\":110,\"name\":\"Sukhpr\",\"parentContact\":\"9873887425\",\"grade\":2,\"section\":\"C\",\"fatherName\":\"Shsh\",\"motherName\":\"Shsh\",\"gender\":\"Male\",\"rollNumber\":5643,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":5643},{\"index\":55515,\"id\":55515,\"udise\":110,\"name\":\"new tool\",\"parentContact\":\"9873887425\",\"grade\":4,\"section\":\"B\",\"fatherName\":\"ok\",\"motherName\":\"ok\",\"gender\":\"Male\",\"rollNumber\":9653,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":9653},{\"index\":55737,\"id\":55737,\"udise\":110,\"name\":\"Kranti \",\"parentContact\":\"9418129628\",\"grade\":5,\"section\":\"A\",\"fatherName\":\"Divya fbks\",\"motherName\":\"Divya fbks\",\"gender\":\"Male\",\"rollNumber\":1245,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":1245},{\"index\":183968,\"id\":183968,\"udise\":110,\"name\":\"testing\",\"parentContact\":\"9415787824\",\"grade\":3,\"section\":\"A\",\"fatherName\":\"testing\",\"motherName\":\"testing\",\"gender\":\"Male\",\"rollNumber\":999999,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":999999},{\"index\":232306,\"id\":232306,\"udise\":110,\"name\":\"Saurav Kaul\",\"parentContact\":\"9873887425\",\"grade\":1,\"section\":\"A\",\"fatherName\":\"Ami\",\"motherName\":\"Ami\",\"gender\":\"Male\",\"rollNumber\":4,\"category\":\"General\",\"isCWSN\":\"No\",\"admissionNumber\":4},{\"index\":232309,\"id\":232309,\"udise\":110,\"name\":\"Shashikala\",\"parentContact\":\"9873887425\",\"grade\":1,\"section\":\"A\",\"fatherName\":\"Ami\",\"motherName\":\"Ami\",\"gender\":\"Female\",\"rollNumber\":6,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":6},{\"index\":232307,\"id\":232307,\"udise\":110,\"name\":\"Vidhi Kapoor\",\"parentContact\":\"9873887425\",\"grade\":1,\"section\":\"A\",\"fatherName\":\"Ami\",\"motherName\":\"Ami\",\"gender\":\"Female\",\"rollNumber\":3,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":3},{\"index\":232308,\"id\":232308,\"udise\":110,\"name\":\"Ramkrishan Galgotia\",\"parentContact\":\"9873887425\",\"grade\":1,\"section\":\"A\",\"fatherName\":\"Ami\",\"motherName\":\"Ami\",\"gender\":\"Female\",\"rollNumber\":2,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":2},{\"index\":232310,\"id\":232310,\"udise\":110,\"name\":\"Gaurav Sood\",\"parentContact\":\"9873887425\",\"grade\":1,\"section\":\"A\",\"fatherName\":\"Ami\",\"motherName\":\"Ami\",\"gender\":\"Male\",\"rollNumber\":5,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":5},{\"index\":232305,\"id\":232305,\"udise\":110,\"name\":\"Amitabh Ghose\",\"parentContact\":\"9873887425\",\"grade\":1,\"section\":\"A\",\"fatherName\":\"Ami\",\"motherName\":\"Ami\",\"gender\":\"Male\",\"rollNumber\":1,\"category\":\"General\",\"isCWSN\":\"Yes\",\"admissionNumber\":1}]}";
        Gson gson = new Gson();
        Map m = gson.fromJson(jsonString, Map.class);
        ArrayList<LinkedTreeMap> students = (ArrayList<LinkedTreeMap>) m.get("data");
        JSONArray jsonArray = new JSONArray();
        for(LinkedTreeMap linkedTreeMap : students){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("stud", linkedTreeMap.get("name").toString());
                jsonObject.put("section", linkedTreeMap.get("section").toString());
                if(linkedTreeMap.get("grade") != null){
                    String value =linkedTreeMap.get("grade").toString().split("\\.")[0];
                    jsonObject.put("class", "class"+value);
                }
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect1.getInstance().getComponent().inject(this);
        setContentView(R.layout.main_menu);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainMenuViewModel.class);

        initToolbar();
        DaggerUtils.getComponent(this).inject(this);
        FormManagementSectionInteractor i = new FormManagementSectionInteractor();

        storageMigrationRepository.getResult().observe(this, new Observer<StorageMigrationResult>() {
            @Override
            public void onChanged(StorageMigrationResult result) {
                MainMenuActivity.this.onStorageMigrationFinish(result);
            }
        });
        versionSHAView = findViewById(R.id.version_sha);

        storageMigrationBanner = findViewById(R.id.storageMigrationBanner);
        // enter data button. expects a result.
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setText(getString(R.string.enter_data_button));
//        enterDataButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for(Form form:  i.getDownloadedFormsNamesFromDatabase()){
//                    i.updateFormBasedOnIdentifier(form.getDisplayName(), "udise", "110");
//                }
//                if (MultiClickGuard.allowClick(getClass().getName())) {
//                    Intent icc= new Intent(getApplicationContext(),
//                            FormChooserListActivity.class);
//                    startActivity(icc);
//                }
//            }
//        });

        // review data button. expects a result.
        reviewDataButton = findViewById(R.id.review_data);
        reviewDataButton.setText(getString(R.string.review_data_button));
        reviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.EDIT_SAVED);
                    startActivity(i);
                }
            }
        });

        // send data button. expects a result.
        sendDataButton = findViewById(R.id.send_data);
        sendDataButton.setText(getString(R.string.send_data_button));
        sendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String referenceFileName = "student_list.csv";
            ArrayList<String> mediaDirectoriesNames =  (PermissionUtils.areStoragePermissionsGranted(getApplicationContext())? CSVHelper.fetchFormMediaDirectoriesWithMedia(referenceFileName) : new ArrayList<>());
            if (mediaDirectoriesNames.size() > 0) {
                CSVHelper.buildCSVForODK(new CSVBuildStatusListener() {
                    @Override
                    public void onSuccess() {
                            Timber.d("getCurrentStudentListForForms: kjHAHAHAHHAHAH");
                        ToastUtils.showLongToast("ho gya");

                    }

                    @Override
                    public void onFailure(Exception exception, org.odk.collect.android.contracts.CSVHelper.BuildFailureType buildFailureType) {
                        Timber.d("kachra ho gya");
                        ToastUtils.showLongToast("hua nahi");

                    }

                }, mediaDirectoriesNames, buildJSONArray(), referenceFileName);
            }else{
               Timber.e("eveklkerjerk Mila hi nahi");
            }
//                if (MultiClickGuard.allowClick(getClass().getName())) {
//                    Intent i = new Intent(getApplicationContext(),
//                            InstanceUploaderListActivity.class);
//                    startActivity(i);
//                }
            }
        });

        //View sent forms
        viewSentFormsButton = findViewById(R.id.view_sent_forms);
        viewSentFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.VIEW_SENT);
                    startActivity(i);
                }
            }
        });

        // manage forms button. no result expected.
        getFormsButton = findViewById(R.id.get_forms);
        getFormsButton.setText(getString(R.string.get_forms));
        getFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(MainMenuActivity.this);
                    String protocol = sharedPreferences.getString(
                            GeneralKeys.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
                    Intent i = null;
                    if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
                        if (new PlayServicesChecker().isGooglePlayServicesAvailable(MainMenuActivity.this)) {
                            i = new Intent(getApplicationContext(),
                                    GoogleDriveActivity.class);
                        } else {
                            new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(MainMenuActivity.this);
                            return;
                        }
                    } else {
                        i = new Intent(getApplicationContext(),
                                FormDownloadListActivity.class);
                    }
                    startActivity(i);
                }
            }
        });

        // manage forms button. no result expected.
        manageFilesButton = findViewById(R.id.manage_forms);
        manageFilesButton.setText(getString(R.string.manage_files));
        manageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            FileManagerTabs.class);
                    startActivity(i);
                }
            }
        });

        String versionSHA = viewModel.getVersionCommitDescription();
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }

        // must be at the beginning of any activity that can be called from an
        // external intent
        Timber.i("Starting up, creating directories");
        try {
            new StorageInitializer().createOdkDirsOnStorage();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        importSettingsFromLegacyFiles();
    }

    @Override
    protected void onResume() {
        super.onResume();

        countSavedForms();
        updateButtons();
        if (!storageMigrationRepository.isMigrationBeingPerformed()) {
            getContentResolver().registerContentObserver(InstanceColumns.CONTENT_URI, true, contentObserver);
        }

        setButtonsVisibility();
        invalidateOptionsMenu();
        setUpStorageMigrationBanner();
    }

    private void setButtonsVisibility() {
        reviewDataButton.setVisibility(viewModel.shouldEditSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        sendDataButton.setVisibility(viewModel.shouldSendFinalizedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        viewSentFormsButton.setVisibility(viewModel.shouldViewSentFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        getFormsButton.setVisibility(viewModel.shouldGetBlankFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        manageFilesButton.setVisibility(viewModel.shouldDeleteSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public void onDestroy() {
        storageMigrationRepository.clearResult();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        qrcodeScannerMenuItem = menu.findItem(R.id.menu_configure_qr_code);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        qrcodeScannerMenuItem.setVisible(this.getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).getBoolean(AdminKeys.KEY_QR_CODE_SCANNER, true));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_configure_qr_code) {
            analytics.logEvent(AnalyticsEvents.SCAN_QR_CODE, "MainMenu");

            if (adminPasswordProvider.isAdminPasswordSet()) {
                Bundle args = new Bundle();
                args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.SCAN_QR_CODE);
                showIfNotShowing(AdminPasswordDialogFragment.class, args, getSupportFragmentManager());
            } else {
                startActivity(new Intent(this, QRCodeTabsActivity.class));
            }
            return true;
        } else if (itemId == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (itemId == R.id.menu_general_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        } else if (itemId == R.id.menu_admin_preferences) {
            if (adminPasswordProvider.isAdminPasswordSet()) {
                Bundle args = new Bundle();
                args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.ADMIN_SETTINGS);
                showIfNotShowing(AdminPasswordDialogFragment.class, args, getSupportFragmentManager());
            } else {
                startActivity(new Intent(this, AdminPreferencesActivity.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(String.format("%s %s", getString(R.string.app_name), viewModel.getVersion()));
        setSupportActionBar(toolbar);
    }

    private void countSavedForms() {
        InstancesDao instancesDao = new InstancesDao();

        // count for finalized instances
        try {
            finalizedCursor = instancesDao.getFinalizedInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (finalizedCursor != null) {
            startManagingCursor(finalizedCursor);
        }
        completedCount = finalizedCursor != null ? finalizedCursor.getCount() : 0;

        // count for saved instances
        try {
            savedCursor = instancesDao.getUnsentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (savedCursor != null) {
            startManagingCursor(savedCursor);
        }
        savedCount = savedCursor != null ? savedCursor.getCount() : 0;

        //count for view sent form
        try {
            viewSentCursor = instancesDao.getSentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }
        if (viewSentCursor != null) {
            startManagingCursor(viewSentCursor);
        }
        viewSentCount = viewSentCursor != null ? viewSentCursor.getCount() : 0;
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), errorListener);
        alertDialog.show();
    }

    private void updateButtons() {
        if (finalizedCursor != null && !finalizedCursor.isClosed()) {
            finalizedCursor.requery();
            completedCount = finalizedCursor.getCount();
            if (completedCount > 0) {
                sendDataButton.setText(
                        getString(R.string.send_data_button, String.valueOf(completedCount)));
            } else {
                sendDataButton.setText(getString(R.string.send_data));
            }
        } else {
            sendDataButton.setText(getString(R.string.send_data));
            Timber.w("Cannot update \"Send Finalized\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (savedCursor != null && !savedCursor.isClosed()) {
            savedCursor.requery();
            savedCount = savedCursor.getCount();
            if (savedCount > 0) {
                reviewDataButton.setText(getString(R.string.review_data_button,
                        String.valueOf(savedCount)));
            } else {
                reviewDataButton.setText(getString(R.string.review_data));
            }
        } else {
            reviewDataButton.setText(getString(R.string.review_data));
            Timber.w("Cannot update \"Edit Form\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (viewSentCursor != null && !viewSentCursor.isClosed()) {
            viewSentCursor.requery();
            viewSentCount = viewSentCursor.getCount();
            if (viewSentCount > 0) {
                viewSentFormsButton.setText(
                        getString(R.string.view_sent_forms_button, String.valueOf(viewSentCount)));
            } else {
                viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            }
        } else {
            viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            Timber.w("Cannot update \"View Sent\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }
    }

    @Override
    public void onCorrectAdminPassword(Action action) {
        switch (action) {
            case ADMIN_SETTINGS:
                startActivity(new Intent(this, AdminPreferencesActivity.class));
                break;
            case STORAGE_MIGRATION:
                StorageMigrationDialog dialog = showStorageMigrationDialog();
                if (dialog != null) {
                    dialog.startStorageMigration();
                }

                break;
            case SCAN_QR_CODE:
                startActivity(new Intent(this, QRCodeTabsActivity.class));
                break;
        }
    }

    @Override
    public void onIncorrectAdminPassword() {
        ToastUtils.showShortToast(R.string.admin_password_incorrect);
    }

    /*
     * Used to prevent memory leaks
     */
    static class IncomingHandler extends Handler {
        private final WeakReference<MainMenuActivity> target;

        IncomingHandler(MainMenuActivity target) {
            this.target = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainMenuActivity target = this.target.get();
            if (target != null) {
                target.updateButtons();
            }
        }
    }

    /**
     * notifies us that something changed
     */
    private class MyContentObserver extends ContentObserver {

        MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handler.sendEmptyMessage(0);
        }
    }

    private void onStorageMigrationFinish(StorageMigrationResult result) {
        if (result == StorageMigrationResult.SUCCESS) {
            DialogUtils.dismissDialog(StorageMigrationDialog.class, getSupportFragmentManager());
            displayBannerWithSuccessStorageMigrationResult();
        } else {
            StorageMigrationDialog dialog = showStorageMigrationDialog();

            if (dialog != null) {
                dialog.handleMigrationError(result);
            }
        }
    }

    @Nullable
    private StorageMigrationDialog showStorageMigrationDialog() {
        Bundle args = new Bundle();
        args.putInt(StorageMigrationDialog.ARG_UNSENT_INSTANCES, savedCount);

        showIfNotShowing(StorageMigrationDialog.class, args, getSupportFragmentManager());
        return getDialog(StorageMigrationDialog.class, getSupportFragmentManager());
    }

    private void setUpStorageMigrationBanner() {
        if (!storageStateProvider.isScopedStorageUsed()) {
            displayStorageMigrationBanner();
        }
    }

    private void displayStorageMigrationBanner() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        storageMigrationBanner.setText(getText(R.string.scoped_storage_banner_text));
        storageMigrationBanner.setActionText(getString(R.string.scoped_storage_learn_more));
        storageMigrationBanner.setAction(() -> {
            showStorageMigrationDialog();
            getContentResolver().unregisterContentObserver(contentObserver);
        });
    }

    private void displayBannerWithSuccessStorageMigrationResult() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        storageMigrationBanner.setText(getString(R.string.storage_migration_completed));
        storageMigrationBanner.setActionText(getString(R.string.scoped_storage_dismiss));
        storageMigrationBanner.setAction(() -> {
            storageMigrationBanner.setVisibility(View.GONE);
            storageMigrationRepository.clearResult();
        });
    }

    private void importSettingsFromLegacyFiles() {
//        try {
//            String settings = new LegacySettingsFileReader(storagePathProvider).toJSON();
//
//            if (settings != null) {
//                if (settingsImporter.fromJSON(settings)) {
//                    ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
//                    recreate();
//                } else {
//                    ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
//                }
//            }
//        } catch (LegacySettingsFileReader.CorruptSettingsFileException e) {
//            ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
//        }
    }
}
