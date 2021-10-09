package com.samagra.commons;

import androidx.annotation.Nullable;

import com.google.firebase.database.annotations.NotNull;


/**
 * A {@link RuntimeException} indicating that a class is not initialised properly.
 *
 * @author Pranav Sharma
 */
public class InitializationException extends RuntimeException {

    private Class clazz;
    private String reason;

    public InitializationException(Class clazz, @Nullable String reason) {
        this.clazz = clazz;
        if (reason == null || reason.equalsIgnoreCase(""))
            this.reason = "Not Available";
        else
            this.reason = reason;
    }

    @NotNull
    @Override
    public String toString() {
        return "InitializationException occurred in " + clazz.getCanonicalName() + " Reason : " + reason;
    }
}
