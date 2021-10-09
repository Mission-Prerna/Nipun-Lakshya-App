package com.samagra.commons.TaskScheduler;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.UUID;

/**
 * An interface enforcing the necessary implementations by custom wrappers around concrete implementations
 * of {@link androidx.work.WorkRequest}.
 * {@link ScheduledOneTimeWork} is a wrapper around {@link androidx.work.OneTimeWorkRequest} which is
 * a concrete implementation of {@link androidx.work.WorkRequest}
 *
 * @author Pranav Sharma
 */
public interface ScheduledTask {

    /**
     * Returns the current {@link UUID} of the {@link ScheduledTask}.
     */
    @Nullable
    UUID getScheduledTaskId();

    /**
     * Enqueues the {@link ScheduledTask} to run via the Android {@link androidx.work.WorkManager}
     * API. This call should also save the Task enqueued in the {@link android.content.SharedPreferences}
     * if not already saved.
     *
     * @param context - The context required to get instance of {@link androidx.work.WorkManager}
     *                and access {@link android.content.SharedPreferences}
     */
    void enqueueTask(Context context);

    /**
     * Cancels the execution of an enqueued task.
     *
     * @param context - The context required to get instance of {@link androidx.work.WorkManager}
     */
    void cancelTask(Context context);
}
