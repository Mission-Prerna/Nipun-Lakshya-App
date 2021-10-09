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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.samagra.commons.constants.Constants;
import com.samagra.commons.ExchangeObject;
import com.samagra.commons.Modules;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect1;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.material.MaterialBanner;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.migration.StorageMigrationDialog;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.utilities.DialogUtils;

import javax.inject.Inject;

import static org.odk.collect.android.utilities.DialogUtils.getDialog;
import static org.odk.collect.android.utilities.DialogUtils.showIfNotShowing;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class StorageMigrationActivity extends CollectAbstractActivity {

    @Inject
    public Analytics analytics;
    MaterialBanner storageMigrationBanner;
    @Inject
    StorageMigrationRepository storageMigrationRepository;
    @Inject
    StorageStateProvider storageStateProvider;

    private int savedCount;
    private Cursor finalizedCursor;
    private Cursor savedCursor;
    private Cursor viewSentCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect1.getInstance().getComponent().inject(this);
        setContentView(R.layout.main_menu1);
        storageMigrationRepository.getResult().observe(this, new Observer<StorageMigrationResult>() {
            @Override
            public void onChanged(StorageMigrationResult result) {
                StorageMigrationActivity.this.onStorageMigrationFinish(result);
            }
        });
        storageMigrationBanner = findViewById(R.id.storageMigrationBanner1);
        // enter data button. expects a result.
    }

    @Override
    protected void onResume() {
        super.onResume();
        countSavedForms();
        setUpStorageMigrationBanner();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        storageMigrationRepository.clearResult();
        super.onDestroy();
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

    private void countSavedForms() {
        InstancesDao instancesDao = new InstancesDao();

        // count for finalized instances
        try {
            finalizedCursor = instancesDao.getFinalizedInstancesCursor();
        } catch (Exception e) {

            return;
        }

        if (finalizedCursor != null) {
            startManagingCursor(finalizedCursor);
        }

        // count for saved instances
        try {
            savedCursor = instancesDao.getUnsentInstancesCursor();
        } catch (Exception e) {
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
            return;
        }
        if (viewSentCursor != null) {
            startManagingCursor(viewSentCursor);
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
        showStorageMigrationDialog();
    }

    private void displayBannerWithSuccessStorageMigrationResult() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        storageMigrationBanner.setText(getString(R.string.storage_migration_completed));
        storageMigrationBanner.setActionText(getString(R.string.scoped_storage_dismiss));
        storageMigrationBanner.setAction(() -> {
            storageMigrationBanner.setVisibility(View.GONE);
            storageMigrationRepository.clearResult();
            Intent intent = new Intent(Constants.INTENT_LAUNCH_HOME_ACTIVITY);
            ExchangeObject.SignalExchangeObject signalExchangeObject = new ExchangeObject.SignalExchangeObject(Modules.MAIN_APP, Modules.COLLECT_APP, intent, true);
            Collect1.getInstance().getMainApplication().getEventBus().send(signalExchangeObject);
            finish();
        });
    }

}

