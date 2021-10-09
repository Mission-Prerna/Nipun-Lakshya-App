package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.FormIndex;

import org.odk.collect.android.application.Collect1;
import org.odk.collect.android.javarosawrapper.FormController;

public class FormControllerWaitingForDataRegistry implements WaitingForDataRegistry {

    @Override
    public void waitForData(FormIndex index) {
        Collect1 collect = Collect1.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(index);
    }

    @Override
    public boolean isWaitingForData(FormIndex index) {
        Collect1 collect = Collect1.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return false;
        }

        return index.equals(formController.getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForData() {
        Collect1 collect = Collect1.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(null);
    }
}
