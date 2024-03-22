package com.samagra.ancillaryscreens.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.samagra.ancillaryscreens.R;

/**
 * A utility class that prevents from writing boilerplate code to show various kinds of {@link Snackbar}s
 * in an activity.
 *
 * @author Pranav Sharma
 */
public final class SnackbarUtils {
    private static final int DURATION_SHORT = 3500;
    private static final int DURATION_LONG = 5500;

    private SnackbarUtils() {

    }

    public static void showShortSnackbar(@NonNull View view, @NonNull String message) {
        showSnackbar(view, message, DURATION_SHORT);
    }

    public static void showLongSnackbar(@NonNull View view, @NonNull String message) {
        showSnackbar(view, message, DURATION_LONG);
    }

    /**
     * Displays {@link Snackbar} with {@param message}
     * and multi-line message enabled.
     *
     * @param view    The view to find a parent from.
     * @param message The text to show.  Can be formatted text.
     */
    public static void showSnackbar(@NonNull View view, @NonNull String message, int duration) {
        if (message.isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(view, message.trim(), duration);
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }

    /**
     * Displays {@link Snackbar} with {@param message}
     * and multi-line message enabled.
     *
     * @param view    The view to find a parent from.
     * @param message The text to show.  Can be formatted text.
     */
    public static void showSnackbar(@NonNull View view, @NonNull String message, int duration, String dismissText) {
        if (message.isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(view, message.trim(), duration)
                .setAction(dismissText.trim(), v -> {
                });
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }

    /**
     * This method provides a snackbar with an indeterminate circular laoding spinner. While using it make sure that
     * multiple objects of snackbar and not created since this method will always return a new Snackbar.
     *
     * @param container - The parent root container for the snackbar (Usually the view with id android.R.id.content
     * @param context   - The current activity context
     * @param message   - The String message that needs to be displayed in the snackbar
     */
    public static Snackbar getSnackbarWithProgressIndicator(@io.reactivex.annotations.NonNull View container, @io.reactivex.annotations.NonNull Context context, String message) {
        Snackbar bar = Snackbar.make(container, message, Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(context);
        item.setScaleY(0.8f);
        item.setScaleX(0.8f);
        item.setInterpolator(new AccelerateInterpolator());
        item.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        contentLay.addView(item);
        return bar;
    }
}
