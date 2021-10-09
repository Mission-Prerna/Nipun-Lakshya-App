package com.example.update;

import androidx.annotation.NonNull;

import com.samagra.commons.MainApplication;


public class UpdateDriver {

    public static MainApplication mainApplication = null;

    public static void init(@NonNull MainApplication mainApplication) {
        UpdateDriver.mainApplication = mainApplication;

    }
}


