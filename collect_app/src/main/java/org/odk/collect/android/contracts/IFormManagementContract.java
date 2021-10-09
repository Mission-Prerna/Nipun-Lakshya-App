package org.odk.collect.android.contracts;

import android.content.Context;

import com.samagra.commons.models.FormStructure;

import org.json.JSONArray;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.listeners.FormProcessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public interface IFormManagementContract {

    /**
     * Sets up Styling facets for ODK, you could use the methods to globally apply theme and UI assets
     * @param mainApplication - {@link MainApplication} Instance of Main Application, used to communicate to and fro with the main App Module.
     * @param splashScreenDrawableID Drawable ID for the Splash screen.
     * @param baseAppThemeStyleID- Base App Theme Style ID
     * @param formActivityThemeID- Form Activity Theme ID
     * @param customThemeId_Settings- Custom Theme Id for Settings
     * @param toolbarIconResId- Toolbar Icon Resource Id
     */
//    void setODKModuleStyle(MainApplication mainApplication, int splashScreenDrawableID, int baseAppThemeStyleID,
//                           int formActivityThemeID, int customThemeId_Settings, long toolbarIconResId);

    /**
     * This method resets  a) Saved forms (instances folder, instances database); b) Blank forms (forms folder, forms database, itemsets database);
     * c) Form load cache (.cache folder)
     */
    void resetPreviousODKForms(Context context,IResetActionListener iResetActionListener);

    /*
     * Retrieves forms from db without thread
     * */
    List<Form> getDownloadedFormsNamesFromDatabase();

    /*
    * Retrieves forms from db via thread using callback
    * */
    void getDownloadedFormsNamesFromDatabase(FormsCallback callback);

    /**
     * This will delete a) Saved forms (instances folder, instances database); b) Blank forms (forms folder, forms database, itemsets database);
     * c) Form load cache (.cache folder); d) All settings (internal settings, saved settings), preference data
     */
    void resetEverythingODK(Context context, IResetActionListener iResetActionListener);

    /**
     * This method creates Storage directories, invoked one time after the storage permissions have been granted.
     */
    void createODKDirectories();

    /**
     *  This method resets  previously downloaded only Blank forms (forms folder, forms database, item-sets database).
     * @param context Context Instance
     */
    void resetODKForms(Context context, IResetActionListener aa);

    /**
     * @param subject - JSON Array String containing Form ID and Form Name
     * @return boolean Flag is true if forms match else false
     */
    void checkIfODKFormsMatch(String subject, ArrayList<FormStructure> filteredFormList, ICheckAllOdkFormsAvailableInDbCallBack listener);

    /**
     * This method triggers download ODK Form List.
     * @param formListDownloadResultCallback Callback for the download process result.
     */
    void startDownloadODKFormListTask(FormListDownloadResultCallback formListDownloadResultCallback);

    /**
     * Filters the forms out of fom list which are to be downloaded for a user.
     * @param userRoleBasedForms - Form Name and ID for the forms pertaining to the user
     * @param latestFormListFromServer - Response received from Form List Download Network Call
     * @param subject
     * @return HashMap<String, FormDetails> Hash map with keys corresponding to the Form ID and Value being Form Details for the forms specific to the user.
     */
    void getDownloadableAssessmentsList(HashMap<String, String> userRoleBasedForms, HashMap<String, ServerFormDetails> latestFormListFromServer, String subject, FormsMapCallback callback);

    /**
     * This method triggers download of specific ODK Forms for the user,
     * @param dataFormDownloadResultCallback Callback for the download process result.
     * @param formsToBeDownloaded HashMap<String, FormDetails> Hash map with keys corresponding to the Form ID and Value being Form Details for the forms specific to the user.
     */
    void downloadODKForms(DataFormDownloadResultCallback dataFormDownloadResultCallback, HashMap<String, ServerFormDetails> formsToBeDownloaded, boolean isODKAggregate);

    /**
     *
     * @param formsString Convert JSON Array of Form ID and Form Name to a HashMap
     * @return  ArrayList<FormStructure> with key = FormID, value = FormName
     */
    ArrayList<FormStructure> downloadFormList(String formsString);

    /**
     * Initialise ODK Module properties, after settings.json have been successfuly embedded.
     */
    void initialiseODKProps();

    /**
     * Apply ODK Settings picked up from settings.json file
     * @param context Context Instance
     * @param resId Resource ID for settings.json
     */
    void applyODKCollectSettings(Context context, int resId);

    /**
     * View a specific form to edit.
     * @param context Context Instance
     * @param formIdentifier Form Name which you want to launch
     */
    void launchSpecificDataForm(Context context, String formIdentifier, FormProcessListener listener);

    /**
     * View a specific form to edit.
     * @param context Context Instance
     * @param formIdentifier Form Name which you want to launch
     * @param odkFormQuesLength
     * @param subject
     */
    void launchSpecificDataFormFromAssessment(Context context, String formIdentifier, int odkFormQuesLength, String subject, FormProcessListener listener);


    /**
     * Get FormID for a Form whose title you know of.
     * @param formIdentifier Form Name for which you want get the ID of.
     * @return int Form's ID
     */
    void fetchSpecificFormID(String formIdentifier, IGetOnSpecificFormIdCallBack listener);

    /**
     * Launches UI with all the forms saved but not sent by the user.
     * @param context Context Instance
     * @param className Invoking class name
     */
    void launchViewUnsubmittedFormView(Context context, String className, HashMap<String, Object> toolbarModificationObject);

    /**
     * Launches UI with all the forms saved and sent by the user.
     * @param context Context Instance
     * @param toolbarModificationObject The contents of Hash Map are as follows * and are used to modify the UI of toolbar of this View.
     * navigationIconDisplay - Boolean (true or false) navigationIconResId -
     * Integer (Resource ID for Navigation(Back) Icon in toolbar)
     * title - String (Title of toolbar) goBackOnNavIconPress - * Boolean (enables or disables back icon)
     */
    void launchViewSubmittedFormsView(Context context, HashMap<String, Object> toolbarModificationObject);

    /**
     * Launches UI with all the forms for a user, user can select an option and fill accordingly.
     * @param context Context Instance
     * @param toolbarModificationObject The contents of Hash Map are as follows * and are used to modify the UI of toolbar of this View.
     * navigationIconDisplay - Boolean (true or false) navigationIconResId -
     * Integer (Resource ID for Navigation(Back) Icon in toolbar)
     * title - String (Title of toolbar) goBackOnNavIconPress - * Boolean (enables or disables back icon)
     */
    void launchFormChooserView(Context context, HashMap<String, Object> toolbarModificationObject);

    /**
     * Prefills tag wise for a form.
     * @param formIdentifier Form Name for which you want to pre-fill tags
     * @param tag Tags you want to pre-fill the value of
     * @param tagValue Value to be pre-filled corresponding to the Form Identifier
     */
    void updateFormBasedOnIdentifier(String formIdentifier, String tag, String tagValue);
    /**
     * The method is used to get the name of media directories wrt. all forms downloaded, which contained the file which is to be edited as per the received data.
     *
     * @param referenceFileName - {{@link String}} Name of the file which is to be checked for presence in Form Media directories.
     * @return {{@link ArrayList <String>}} List of names of media directories for all the forms containing to be modified reference File.
     */
    ArrayList<String> fetchMediaDirs(String referenceFileName);

    /**
     * Build a CSV located in list of media directories associated with ODK Forms
     * This is the abstracted method that will be called by the downloading class to read the form sample ODKs and edit them accordingly. Returns a listener telling
     * if the operation is done or not and also the type of error.
     *
     *  @param csvBuildStatusListener - {{@link CSVBuildStatusListener}} CSV Build Operation Listener
     * @param mediaDirectoriesNames  - {{@link ArrayList<String>}} List of the forms' names for which the operation is to be done. (Given that same format of CSV is needed for all the forms), just the path is different.
     * @param inputData              - {{@link JSONArray}} Data to be inserted into the final CSVs, it ideally should contain the data for the keys to be entered.
     * @param mediaFileName          -{{@link String}} Media File to be updated
     */
    void buildCSV(CSVBuildStatusListener csvBuildStatusListener, ArrayList<String> mediaDirectoriesNames, JSONArray inputData, String mediaFileName);

    void observeStorageMigration(Context context);

    boolean isScopedStorageUsed();

    boolean allowClick(String name);

    int getBottomDialogTheme();

    void enableUsingScopedStorage();

    String getFormsPath();

    String getRootPath();

    void sendAnalyticsAdoptionEvent(String s, boolean b);

    void setServerUrl(String odkServerUrl);

    void checkOdkFormIdAvailableInDb(String formId, ICheckFormIdExistInDbCallBack param);
}