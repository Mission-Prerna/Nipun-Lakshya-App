package com.samagra.commons.TaskScheduler;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.work.Configuration;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;

import com.google.gson.Gson;
import com.samagra.commons.constants.Constants;
import com.samagra.commons.InitializationException;
import com.samagra.commons.MainApplication;
import com.samagra.grove.logging.Grove;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


/**
 * A Manager class for managing {@link ScheduledOneTimeWork} requests.
 *
 * @author Pranav Sharma
 */
public class Manager {

    private static MainApplication mainApplication = null;
    private static Set<String> incompleteTasksArrayList = new ArraySet<>();
    private static final String INCOMPLETE_TASK_LIST = "incomplete_work";

    /**
     * The init method for the Manager. This method <b>must</b> be called in {@link Application#onCreate()},
     * prior to using any {@link ScheduledOneTimeWork} requests.
     *
     * @param mainApplication - The Application instance for the main app.
     */
    public static void init(MainApplication mainApplication) {
        Manager.mainApplication = mainApplication;
        Configuration myConfig = new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
        WorkManager.initialize(mainApplication.getCurrentApplication().getApplicationContext(), myConfig);
        loadIncompleteTasksArrayList(mainApplication.getCurrentApplication().getApplicationContext());
    }

    /**
     * This method enqueues all the incomplete {@link ScheduledOneTimeWork} requests for running.
     *
     * @param context - The current activity/application context.
     * @throws InitializationException if the init method is not called prior to using this.
     * @apiNote Take extra caution while using activity context for this as it may cause memory leaks.
     * If there is no activity level use of the Work (like updating UI) you can safely pass in applicationContext.
     */
    public static void enqueueAllIncompleteTasks(@Nullable Context context) {
        checkInit();
        Grove.d("Enqueuing All Tasks");
        SharedPreferences sharedPreferences = mainApplication.getCurrentApplication()
                .getApplicationContext().getSharedPreferences(Constants.WORK_MANAGER_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        for (String uuid : incompleteTasksArrayList) {
            String json = sharedPreferences.getString(uuid, "");
            if (json != null && !json.equals("")) {
                SavedTask savedTask = new Gson().fromJson(json, SavedTask.class);
                ScheduledOneTimeWork scheduledOneTimeWork = savedTask.convertToScheduledOneTimeWork();
                updateTaskUuidInSharedPrefs(savedTask, Objects.requireNonNull(scheduledOneTimeWork.getScheduledTaskId()).toString());
                if (context != null)
                    scheduledOneTimeWork.enqueueTask(context);
                else
                    savedTask.convertToScheduledOneTimeWork().enqueueTask(mainApplication.getCurrentApplication().getApplicationContext());
                Grove.e("Task with id %s enqueued.", savedTask.convertToWorkRequest().getId());
            } else {
                Grove.w("Trying to access unsaved task");
            }
        }
        Grove.i("All Tasks enqueued");
    }

    static MainApplication getMainApplication() {
        if (mainApplication == null)
            throw new InitializationException(Manager.class, Manager.class.getCanonicalName() + " not initialised.\nPlease call init method.");
        return mainApplication;
    }

    /**
     * This function checks if a task with give uuid is already saved in {@link SharedPreferences}
     *
     * @param uuid - The {@link UUID} of the task to be checked.
     * @return {@code true} if the task is present in {@link SharedPreferences} {@code false} otherwise.
     */
    static boolean isTaskAlreadyInPrefs(UUID uuid) {
        SharedPreferences sharedPreferences = mainApplication.getCurrentApplication()
                .getApplicationContext()
                .getSharedPreferences(Constants.WORK_MANAGER_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return !Objects.requireNonNull(sharedPreferences.getString(uuid.toString(), "null")).equals("null");
    }

    /**
     * This function updates a previously {@link SavedTask} with a new UUID. Updating this requires
     * updating the incompleteTasksArrayList (which is also persisted in SharedPreferences) as well as
     * the {@link SavedTask} object saved in {@link SharedPreferences}.
     *
     * <p>The UUID for a given SavedTask needs to be updated because this is essentially a version of
     * {@link ScheduledOneTimeWork} that can be saved in {@link SharedPreferences} and while recreating a
     * {@link OneTimeWorkRequest} from a {@link ScheduledOneTimeWork}, the UUID of the work request
     * changes, thus it is required to be updated in the local storage.</p>
     *
     * @param oldTask - The old {@link SavedTask} that was stored in SharedPreferences.
     * @param newUUID - The UUID of the {@link OneTimeWorkRequest} created using {@link ScheduledOneTimeWork}
     *                generated from {@link SavedTask#convertToScheduledOneTimeWork()}
     */
    private static void updateTaskUuidInSharedPrefs(SavedTask oldTask, String newUUID) {
        if (incompleteTasksArrayList.contains(oldTask.strUUID)) {
            incompleteTasksArrayList.remove(oldTask.strUUID);
            incompleteTasksArrayList.add(newUUID);
            updateIncompleteTasksArrayList(mainApplication.getCurrentApplication().getApplicationContext());
            SavedTask.clearSavedTaskFromSharedPrefs(oldTask.strUUID);
            oldTask.strUUID = newUUID;
            oldTask.saveTaskInSharedPrefs();
        } else {
            Grove.w("incompleteTasksArrayList does not contain the UUID you are trying to update");
        }
    }

    /**
     * This function updates the {@code Manager.incompleteTasksArrayList} with the latest values from
     * the {@link SharedPreferences}
     *
     * @param context - The context used to access the {@link SharedPreferences}. Should ideally be
     *                applicationContext to prevent memory leaks.
     */
    private static void loadIncompleteTasksArrayList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.WORK_MANAGER_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        incompleteTasksArrayList = sharedPreferences.getStringSet(INCOMPLETE_TASK_LIST, new ArraySet<>());
        System.out.println("Updated List is " + incompleteTasksArrayList);
    }

    /**
     * This function updates the incompleteTasksArrayList stored in the {@link SharedPreferences} from
     * the values in {@code Manager.incompleteTasksArrayList}.
     *
     * @param context - The context used to access the SharedPreferences. Should ideally be applicationContext
     *                in order to prevent memory leaks.
     */
    @SuppressLint("ApplySharedPref")
    private static void updateIncompleteTasksArrayList(Context context) {
        Grove.d("Updating incomplete ArrayList");
        System.out.println("ARRAY LIST IS " + incompleteTasksArrayList.toString());
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.WORK_MANAGER_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(INCOMPLETE_TASK_LIST);
        editor.commit();
        editor.putStringSet(INCOMPLETE_TASK_LIST, incompleteTasksArrayList);
        editor.commit();
        loadIncompleteTasksArrayList(context);
    }

    /**
     * Function to check if the initialization of this class has been done.
     *
     * @throws InitializationException if class is not initialized prior to calling.
     */
    private static void checkInit() {
        if (mainApplication == null)
            throw new InitializationException(Manager.class, Manager.class.getCanonicalName() + " not initialised.\nPlease call init method.");
    }


    /**
     * A class that converts a {@link OneTimeWorkRequest} in a form that can be saved in the
     * {@link SharedPreferences}. This conversion is done using the {@link WorkInfo} of a {@link OneTimeWorkRequest}.
     *
     * @author Pranav Sharma
     * @apiNote This class can be structured in a way that it works for all {@link ScheduledTask}
     * instead of only {@link OneTimeWorkRequest}
     */
    static class SavedTask implements Serializable {

        private Data data;
        private String strUUID;
        private String name;

        /**
         * Creates a {@link SavedTask} that can be saved in the {@link SharedPreferences}.
         *
         * @param workInfo - The {@link WorkInfo} of the {@link OneTimeWorkRequest}.
         * @param clazz    - The {@link Worker} class used to create the {@link OneTimeWorkRequest}
         */
        static SavedTask createSavedTaskFromWorkInfo(WorkInfo workInfo, Class clazz) {
            return new SavedTask(workInfo.getOutputData(), workInfo.getId().toString(), clazz.getCanonicalName());
        }

        private SavedTask(@Nullable Data data, String strUUID, String name) {
            this.data = data; // Doubtful if this will serialize
            this.strUUID = strUUID;
            this.name = name;
        }

        /**
         * Converts the {@link SavedTask} to {@link ScheduledOneTimeWork}.
         *
         * @return ScheduledOneTimeWork equivalent of the current object.
         */
        ScheduledOneTimeWork convertToScheduledOneTimeWork() {
            try {
                if (data != null)
                    return ScheduledOneTimeWork.from(Class.forName(name).asSubclass(Worker.class), data);
                else
                    return ScheduledOneTimeWork.from(Class.forName(name).asSubclass(Worker.class));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Converts the {@link SavedTask} to Android's {@link WorkRequest}
         *
         * @return WorkRequest equivalent of this object.
         */
        WorkRequest convertToWorkRequest() {
            try {
                return new OneTimeWorkRequest.Builder(
                        Class.forName(name).asSubclass(Worker.class))
                        .setInputData(data)
                        .build();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Saves the current object in the SharedPreferences. This function does not check if the
         * task was already present in the Local storage, hence check should manually be made prior
         * to calling this method.
         */
        void saveTaskInSharedPrefs() {
            Grove.d("Saving Task in SharedPreferences");
            SharedPreferences sharedPreferences = mainApplication.getCurrentApplication()
                    .getApplicationContext()
                    .getSharedPreferences(Constants.WORK_MANAGER_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String json = new Gson().toJson(this);
            editor.putString(this.strUUID, json);
            editor.apply();
            addToIncompleteTaskArrayList();
        }

        /**
         * Clears a {@link SavedTask} with a given UUID from the SharedPreferences.
         * This method also updates the {@code Manager.incompleteTasksArrayList} to reflect the changes.
         */
        static void clearSavedTaskFromSharedPrefs(String UUID) {
            Grove.d("Clearing Task with UUID %s from Preferences", UUID);
            SharedPreferences sharedPreferences = mainApplication.getCurrentApplication()
                    .getApplicationContext()
                    .getSharedPreferences(Constants.WORK_MANAGER_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(UUID);
            editor.apply();
            removeFromIncompleteTaskArrayList(UUID);
        }

        /**
         * This method adds the current object's {@code strUUID} to the {@code Manager.incompleteTasksArrayList}
         * and calls upon {@link Manager#updateIncompleteTasksArrayList(Context)} to persist the
         * changes made to the object.
         */
        private void addToIncompleteTaskArrayList() {
            if (!incompleteTasksArrayList.contains(strUUID)) {
                Grove.d("Adding to incomplete ArrayList");
                incompleteTasksArrayList.add(strUUID);
                updateIncompleteTasksArrayList(mainApplication.getCurrentApplication().getApplicationContext());
            }
        }

        /**
         * This method removes the current object's {@code strUUID} from the {@code Manager.incompleteTasksArrayList}
         * and calls upon {@link Manager#updateIncompleteTasksArrayList(Context)} to persist the
         * changes made to the object.
         */
        private static void removeFromIncompleteTaskArrayList(String UUID) {
            if (incompleteTasksArrayList.contains(UUID)) {
                incompleteTasksArrayList.remove(UUID);
                updateIncompleteTasksArrayList(mainApplication.getCurrentApplication().getApplicationContext());
            } else {
                Grove.d("incompleteTasksArrayList does not contain UUID %s", UUID);
            }
        }
    }
}

