package com.samagra.parent.base;

import com.samagra.parent.data.prefs.PreferenceHelper;

import javax.inject.Inject;

/**
 * A base for all the interactors (Java classes that serves as links between presenter (business logic)
 * and database.) This class includes functionality that must be implemented by all the Interactors.
 * Must implement {@link MvpInteractor}.
 *
 * @author Pranav Sharma
 */
public class BaseInteractor implements MvpInteractor {
    private final PreferenceHelper preferenceHelper;

    @Inject
    public BaseInteractor(PreferenceHelper preferenceHelper) {
        this.preferenceHelper = preferenceHelper;
    }

    @Override
    public PreferenceHelper getPreferenceHelper() {
        return preferenceHelper;
    }
}
