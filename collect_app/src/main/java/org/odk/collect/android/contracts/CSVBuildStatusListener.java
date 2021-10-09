package org.odk.collect.android.contracts;

/**
 * Interface/Listener for the whole operation response.
 */
public interface CSVBuildStatusListener {

    void onSuccess();

    void onFailure(Exception exception, CSVHelper.BuildFailureType buildFailureType);
}
