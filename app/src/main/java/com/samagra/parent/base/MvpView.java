package com.samagra.parent.base;

import android.content.Context;

/**
 * This is the Base interface that all 'View Contracts' must extend. Hence this interface only applies
 * to MVP based Activities. For instance, {@link com.samagra.parent.ui.HomeScreen.HomeMvpView} extends
 * this class and so does every other MVP based 'View Contract'. Methods maybe added to it as and
 * when required.
 *
 * @author Pranav Sharma
 */
public interface MvpView extends ODKTestActivity {

    Context getActivityContext();

    void showSnackbar(String message, int duration);
}
