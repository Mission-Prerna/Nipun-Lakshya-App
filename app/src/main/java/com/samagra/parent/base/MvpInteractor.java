package com.samagra.parent.base;

import com.samagra.parent.data.prefs.PreferenceHelper;

/**
 * This is the base interface that all 'Interactor Contracts' must extend.
 * Methods may be added as and when required.
 *
 * @author Pranav Sharma
 */
public interface MvpInteractor {
    PreferenceHelper getPreferenceHelper();
}
