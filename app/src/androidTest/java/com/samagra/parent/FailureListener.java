package com.samagra.parent;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class FailureListener extends RunListener {

    private RunNotifier runNotifier;

    public FailureListener(RunNotifier runNotifier) {
        super();
        this.runNotifier=runNotifier;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        this.runNotifier.pleaseStop();
    }

}
