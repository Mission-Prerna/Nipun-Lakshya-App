package com.samagra.commons.TaskScheduler;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;


import com.samagra.commons.InitializationException;
import com.samagra.grove.logging.Grove;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * A custom wrapper around the {@link OneTimeWorkRequest} class that allows additional functionality
 * defined in the {@link ScheduledTask} interface and also allows storing the work requests in
 * {@link android.content.SharedPreferences}. This wrapper uses Android's {@link WorkManager} APIs.
 * The {@link Manager} can keep a track of unfinished/cancelled {@link ScheduledOneTimeWork} requests
 * and provides the user with an API to restart these tasks.
 * This is useful in cases where some Chinese OEMs override default implementation of {@link WorkManager}
 * to prevent running the background operations to increase battery life.
 *
 * @author Pranav Sharma
 * @apiNote This class API can be extended to allow the user to pass "Constraints" like the one used by
 * Android's WorkManager ({@link androidx.work.Constraints}). If {@link androidx.work.Constraints}
 * class is not serializable, Constraints would have to be serialized manually.
 */
public class ScheduledOneTimeWork implements ScheduledTask {

    private OneTimeWorkRequest oneTimeWorkRequest;
    private Class clazz;

    /**
     * This function attempts to initialize a {@link ScheduledOneTimeWork} from a {@link Worker} class.
     *
     * @param clazz - The Worker class where you define the work that needs to be done in background.
     * @throws InitializationException if the clazz parameter is not an instance of {@link Worker}.
     */
    public static ScheduledOneTimeWork from(Class clazz) {
        if (Worker.class.isAssignableFrom(clazz)) {
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(clazz).build();
            return new ScheduledOneTimeWork(workRequest, clazz);
        } else {
            throw new InitializationException(ScheduledOneTimeWork.class, "Unable to instantiate class. Trying to create ScheduledTask from a class other than Worker class.");
        }
    }

    /**
     * This function attempts to initialize a {@link ScheduledOneTimeWork} from a {@link Worker} class.
     *
     * @param clazz              - The Worker class where you define the work that needs to be done in background.
     * @param inputDataForWorker - Additional input parameters that may be required by the {@link Worker}
     * @throws InitializationException if the clazz parameter is not an instance of {@link Worker}.
     * @apiNote the inputDataForWorker is of type {@link Data} which is serializable, however, object
     * size greater than 10KB would not be able to serialize, hence keep size of this param to a
     * minimum.
     * @see {https://developer.android.com/reference/androidx/work/Data.html}
     */
    public static ScheduledOneTimeWork from(Class clazz, Data inputDataForWorker) {
        if (Worker.class.isAssignableFrom(clazz)) {
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(clazz)
                    .setInputData(inputDataForWorker)
                    .build();
            return new ScheduledOneTimeWork(workRequest, clazz);
        } else {
            throw new InitializationException(ScheduledOneTimeWork.class, "Unable to instantiate class. Trying to create ScheduledTask from a class other than Worker class.");
        }
    }

    private ScheduledOneTimeWork(OneTimeWorkRequest oneTimeWorkRequest, Class clazz) {
        this.oneTimeWorkRequest = oneTimeWorkRequest;
        this.clazz = clazz;
    }

    @Override
    @Nullable
    public UUID getScheduledTaskId() {
        if (oneTimeWorkRequest != null)
            return oneTimeWorkRequest.getId();
        else return null;
    }

    @Override
    public void enqueueTask(Context context) {
        Grove.d("Enqueuing task");
        WorkManager workManager = WorkManager.getInstance(context);
        LiveData<WorkInfo> status = workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.getId());
        status.observe(Manager.getMainApplication(), workInfo -> {
            AtomicBoolean nqd = new AtomicBoolean(false);
            Grove.d("Work info current state is %s", workInfo.getState());
            if ((workInfo.getState() == WorkInfo.State.ENQUEUED || workInfo.getState() == WorkInfo.State.RUNNING) && !nqd.get()) {
                Grove.i("Task enqueued");
                nqd.set(true);
                if (!Manager.isTaskAlreadyInPrefs(workInfo.getId())) {
                    Manager.SavedTask.createSavedTaskFromWorkInfo(workInfo, clazz).saveTaskInSharedPrefs();
                } else {
                    Grove.i("Task already in Preferences");
                }
            } else if (workInfo.getState().isFinished()) {
                Grove.i("Task finished %s ", workInfo.getId());
                Manager.SavedTask.clearSavedTaskFromSharedPrefs(workInfo.getId().toString());
            }
        });
        workManager.enqueue(oneTimeWorkRequest);
    }

    @Override
    public void cancelTask(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        if (getScheduledTaskId() != null) {
            Grove.i("Task cancelled");
            workManager.getWorkInfoById(getScheduledTaskId()).cancel(true);
        } else {
            Grove.w("Trying to cancel a task that was never scheduled. How did you get this UUID ??!!");
        }
    }

}
