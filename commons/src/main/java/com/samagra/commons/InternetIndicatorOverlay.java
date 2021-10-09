package com.samagra.commons;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

import com.samagra.grove.logging.Grove;

import java.lang.ref.WeakReference;


/**
 * This class is responsible for showing a view like the Android {@link com.google.android.material.snackbar.Snackbar}
 * from the top indicating the new internet connectivity status in case of change. This class has a
 * package access level therefore it cannot be accessed outside this module. This class is internally
 * used by the {@link InternetIndicatorOverlay} to display internet connection changes.
 *
 * @author Pranav Sharma
 */
class InternetIndicatorOverlay {

    private String message;
    private long duration;
    private WeakReference<Activity> activityWeakReference;
    private Handler handler;
    private View inflatedView;
    private Animation showAnimation;
    private Animation hideAnimation;

    // Touch Listener
    private float startX;
    private float startY;

    private InternetIndicatorOverlay(Activity activity, String message, long duration) {
        this.message = message;
        this.duration = duration;
        this.handler = new Handler();
        this.activityWeakReference = new WeakReference<>(activity);
    }

    /**
     * Make a InternetIndicatorOverlay show for a particular time duration along with a message.
     * <p>InternetIndicatorOverlay will try and find a parent according to the current activity
     * returned through {@code mainApplication} and dynamically inflate and attach view to it.
     * The view is also shown with the animations.</p>
     *
     * <p>The view to which the InternetIndicatorOverlay is attached is the root element of the current
     * activity identified by the id {@code android.R.id.content}. The InternetIndicatorOverlay returned
     * also has animations pre-loaded which are used while showing and hiding the view.</p>
     *
     * @param mainApplication - The current application instance.
     * @param message         - The message to be displayed in the InternetIndicatorOverlay.
     * @param duration        - The time duration in milliseconds after which this view should automatically hide.
     * @return an instance of InternetIndicatorOverlay which is ready to be shown via the {@link InternetIndicatorOverlay#show()}
     * @see MainApplication
     */
    public static InternetIndicatorOverlay make(MainApplication mainApplication, String message, long duration) {
        InternetIndicatorOverlay indicatorOverlay = new InternetIndicatorOverlay(mainApplication.getCurrentActivity(), message, duration);
        indicatorOverlay.inflateAndAttachView(mainApplication);
        indicatorOverlay.loadAnimations();
        return indicatorOverlay;
    }

    /**
     * Shows the {@link InternetIndicatorOverlay} if the activity reference on which it was created
     * is still non-null and valid.
     */
    public void show() {
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            inflatedView.startAnimation(showAnimation);
            handler.postDelayed(this::hide, duration);
        }
    }

    /**
     * Inflates the XML file for the InternetIndicatorOverlay and attaches the inflated view to the
     * parent. This method also sets the message for the InternetIndicatorOverlay and any other
     * configurations need to be done on the InternetIndicatorOverlay.
     *
     * @param mainApplication - The current application instance.
     */
    private void inflateAndAttachView(MainApplication mainApplication) {
        inflatedView = LayoutInflater.from(mainApplication.getCurrentActivity()).inflate(R.layout.network_indicator_overlay,
                mainApplication.getCurrentActivity().findViewById(android.R.id.content), false);
        ((AppCompatTextView) inflatedView.findViewById(R.id.network_indicator_message)).setText(message);
        inflatedView.findViewById(R.id.network_indicator_message).setOnClickListener(view -> {
            mainApplication.getEventBus().send(createClickOnNetworkObjectForEventBus());
            Toast.makeText(view.getContext(), "Clicked", Toast.LENGTH_SHORT).show();
        });
        ((FrameLayout) mainApplication.getCurrentActivity().findViewById(android.R.id.content)).addView(inflatedView);
    }

    /**
     * Creates an {@link com.samagra.commons.ExchangeObject.EventExchangeObject} which sends a {@link Modules#PROJECT}
     * level event that the InternetIndicatorOverlay has been clicked.
     */
    private ExchangeObject createClickOnNetworkObjectForEventBus() {
        return new ExchangeObject.EventExchangeObject(Modules.PROJECT, Modules.COMMONS, CustomEvents.INTERNET_INFO_BANNER_CLICKED);
    }

    /**
     * Loads hide and show animations on the InternetIndicatorOverlay if the activity reference on which
     * the InternetIndicatorOverlay view was created is non-null and still valid.
     */
    private void loadAnimations() {
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            hideAnimation = AnimationUtils.loadAnimation(activityWeakReference.get(), R.anim.down_to_top);
            showAnimation = AnimationUtils.loadAnimation(activityWeakReference.get(), R.anim.top_to_down);
        }
    }

    /**
     * Hides the current InternetIndicatorOverlay if the activity reference on which the InternetIndicatorOverlay
     * view was created is non-null and still valid.. Note that, this method has a private access.
     * This method will automatically be called this class after certain milliseconds have passed
     * since calling the show method. This number of milliseconds is provided by the user.
     */
    private void hide() {
        if (inflatedView != null && activityWeakReference != null && activityWeakReference.get() != null) {
            inflatedView.startAnimation(hideAnimation);
            inflatedView = null;
        } else {
            Grove.e("Trying to call hide() on a null view InternetIndicatorOverlay.");
        }
    }
}
