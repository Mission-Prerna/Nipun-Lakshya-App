package com.samagra.parent.base;

/**
 * A parent interface that contains functions which <b>must</b> to be implemented by <b>all</b> the
 * Activities defined in the app module. Its also the parent class of {@link MvpView}.
 *
 * @author Pranav Sharma
 */
public interface ODKTestActivity {

    /**
     * Only set the title and action bar here; do not make further modifications.
     * Any further modifications done to the toolbar here will be overwritten if you
     * use {@link org.odk.collect.android.ODKDriver}. If you wish to prevent modifications
     * from being overwritten, do them after onCreate is complete.
     * This method should be called in onCreate of your activity.
     */
    void setupToolbar();
}
