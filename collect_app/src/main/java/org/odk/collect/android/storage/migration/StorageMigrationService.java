package org.odk.collect.android.storage.migration;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;


import org.odk.collect.android.application.Collect1;

import javax.inject.Inject;

public class StorageMigrationService extends IntentService {

    public static final String SERVICE_NAME = "StorageMigrationService";

    @Inject
    StorageMigrator storageMigrator;

    public StorageMigrationService() {
        super(SERVICE_NAME);
        Collect1.getInstance().getComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        storageMigrator.performStorageMigration();
    }
}
