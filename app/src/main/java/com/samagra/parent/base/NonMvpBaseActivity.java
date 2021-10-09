package com.samagra.parent.base;

import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;


/**
 * This abstract class serves as base to all Activities that are not based on MVP Architecture.
 * {@link BaseActivity} could not be used since it contained additional functionality and initialization
 * which is not required in case of simple Activities like
 * Activity. However, like the {@link BaseActivity} this <b>must</b> also extend the {@link AppCompatActivity}
 * since the app module expresses a dependency on the odk-collect module.
 *
 * @author Pranav Sharma
 */
public abstract class NonMvpBaseActivity extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
