package org.odk.collect.android.contracts;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.samagra.commons.CommonUtilities;
import com.samagra.commons.models.FormStructure;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.OdkFormsDownloadInLocalResponseData;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormChooserListActivity;
import org.odk.collect.android.activities.InstanceChooserList;
import org.odk.collect.android.activities.InstanceUploaderListActivity;
import org.odk.collect.android.activities.StorageMigrationActivity;
import org.odk.collect.android.application.Collect1;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dao.helpers.ContentResolverHelper;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.FormProcessListener;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ApplicationResetter;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.ThemeUtils;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import timber.log.Timber;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FormManagementSectionInteractor implements IFormManagementContract {

    private String mOdkServerUrl = "";

    @Override
    public void resetPreviousODKForms(Context context, IResetActionListener iResetActionListener) {
        final List<Integer> resetActions = new ArrayList<>();
//        resetActions.add(ApplicationResetter.ResetAction.RESET_FORMS);
        resetActions.add(ApplicationResetter.ResetAction.RESET_INSTANCES);
        resetActions.add(ApplicationResetter.ResetAction.RESET_LAYERS);
        resetActions.add(ApplicationResetter.ResetAction.RESET_CACHE);
        resetActions.add(ApplicationResetter.ResetAction.RESET_OSM_DROID);
        new InstancesDao().deleteInstancesDatabase();
        new AsyncTask<Void, Void, List<Integer>>() {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected List<Integer> doInBackground(Void... voids) {
                return new ApplicationResetter().reset(Collect1.getInstance().getAppContext(), resetActions);
            }

            @Override
            protected void onPostExecute(List<Integer> failedResetActions) {
                iResetActionListener.onResetActionDone(failedResetActions);

            }
        }.execute();
    }

    @Override
    public void resetEverythingODK(Context context, IResetActionListener iResetActionListener) {
        final List<Integer> resetActions = new ArrayList<>();
        resetActions.add(ApplicationResetter.ResetAction.RESET_FORMS);
        resetActions.add(ApplicationResetter.ResetAction.RESET_INSTANCES);
//        resetActions.add(ApplicationResetter.ResetAction.RESET_PREFERENCES);
        resetActions.add(ApplicationResetter.ResetAction.RESET_LAYERS);
        resetActions.add(ApplicationResetter.ResetAction.RESET_CACHE);
        resetActions.add(ApplicationResetter.ResetAction.RESET_OSM_DROID);
        new AsyncTask<Void, Void, List<Integer>>() {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected List<Integer> doInBackground(Void... voids) {
                return new ApplicationResetter().reset(context, resetActions);
            }

            @Override
            protected void onPostExecute(List<Integer> failedResetActions) {
                iResetActionListener.onResetActionDone(failedResetActions);

            }
        }.execute();
    }

    @Override
    public void createODKDirectories() {
        new StorageInitializer().createOdkDirsOnStorage();
    }

    @Override
    public void resetODKForms(Context context, IResetActionListener al) {
        final List<Integer> resetActions = new ArrayList<>();
        resetActions.add(ApplicationResetter.ResetAction.RESET_FORMS);
        new AsyncTask<Void, Void, List<Integer>>() {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected List<Integer> doInBackground(Void... voids) {
                return new ApplicationResetter().reset(context, resetActions);
            }

            @Override
            protected void onPostExecute(List<Integer> failedResetActions) {
                al.onResetActionDone(failedResetActions);

            }
        }.execute();
    }

    @Override
    public ArrayList<FormStructure> downloadFormList(String formsString) {
//        Log.e("-->>", "download form string : " + formsString);
//        Log.e("-->>", "converting json string ODK forms forms structure list!");
        ArrayList<FormStructure> userRoleBasedForms = new ArrayList<FormStructure>();
        Timber.e("Role Mapping");
        if (!formsString.equals("")) {
            try {
                JSONArray jsonArray = new JSONArray(formsString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    //TODO: Check if form with the newest version is already there.
                    String formID = object.getString("FormID");
                    String formName = object.getString("FormName");
                    String Subject = object.getString("Subject");
                    FormStructure formStructure = new FormStructure(formID, formName, Subject);
                    userRoleBasedForms.add(formStructure);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        Log.e("-->>", "downloaded form LIst size : " + userRoleBasedForms.size());

        return userRoleBasedForms;
    }

    @Override
    public void initialiseODKProps() {
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void applyODKCollectSettings(Context context, int settingResId) {
        InputStream inputStream = context.getResources().openRawResource(settingResId);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String settings = writer.toString();
        if (settings != null) {
            settings = updateServerUrl(settings);
            if (Collect1.getInstance().getSettingsImporter().fromJSON(settings)) {
                Timber.d(Collect1.getInstance().getAppContext().getResources().getString(R.string.settings_successfully_loaded_file_notification));
            } else {
                Timber.d(Collect1.getInstance().getAppContext().getResources().getString(R.string.corrupt_settings_file_notification));
            }
        }


//        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(content, new ActionListener() {
//            @Override
//            public void onSuccess() {
//                initialiseODKProps();
//                ToastUtils.showLongToast("Successfully loaded settings");
//            }
//
//            @Override
//            public void onFailure(Exception exception) {
//                if (exception instanceof GeneralSharedPreferences.ValidationException) {
//                    ToastUtils.showLongToast("Failed to load settings");
//                } else {
//                    exception.printStackTrace();
//                }
//            }
//        });
    }

    private String updateServerUrl(String settings) {
        JSONObject obj;
        try {
            obj = new JSONObject(settings);
            JSONObject generalObj = obj.getJSONObject("general");
            generalObj.put("server_url", mOdkServerUrl);
            obj.put("general", generalObj);
            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return settings;
        }
    }

    @Override
    public void launchSpecificDataForm(Context context, String formIdentifier, FormProcessListener listener) {
        checkForm(formIdentifier, context, new FormProcessListener() {
            @Override
            public void onProcessingStart() {
                listener.onProcessingStart();
            }

            @Override
            public void onProcessed() {
                fetchSpecificFormID(formIdentifier, formID -> {
                    Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, formID);
                    Intent intent = new Intent(Intent.ACTION_EDIT, formUri);
                    intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                    intent.putExtra("formTitle", formIdentifier);
                    listener.onProcessed();
                    context.startActivity(intent);
                });
            }
            @Override
            public void onCancelled(@NonNull Exception e) {
                listener.onCancelled(e);
            }
        });
    }

    @Override
    public void launchSpecificDataFormFromAssessment(Context context, String formIdentifier, int odkFormQuesLength, String subject, FormProcessListener listener) {
        checkForm(formIdentifier, context, new FormProcessListener() {
            @Override
            public void onProcessingStart() {
                listener.onProcessingStart();
            }
            @Override
            public void onProcessed() {
                fetchSpecificFormID(formIdentifier, formID -> {
                    Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, formID);
                    Intent intent = new Intent(Intent.ACTION_EDIT, formUri);
                    intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                    intent.putExtra("fromAssessment", true);
                    intent.putExtra("odk_form_length", odkFormQuesLength);
                    intent.putExtra("subject", subject);
                    intent.putExtra("formTitle", formIdentifier);
                    listener.onProcessed();
                    context.startActivity(intent);
                });
            }
            @Override
            public void onCancelled(@NonNull Exception e) {
                listener.onCancelled(e);
            }
        });
    }


    @Override
    public void launchFormChooserView(Context context, HashMap<String, Object> toolbarModificationObject) {
        Intent i = new Intent(context, FormChooserListActivity.class);
        i.putExtra(Constants.KEY_CUSTOMIZE_TOOLBAR, toolbarModificationObject);
        i.putIntegerArrayListExtra(Constants.CUSTOM_TOOLBAR_ARRAYLIST_HIDE_IDS, null);
        context.startActivity(i);
    }

    private void prefillFormBasedOnTags(Document document, String tag, String tagValue) {
        try {
            if (document.getElementsByTagName(tag).item(0).getChildNodes().getLength() > 0)
                document.getElementsByTagName(tag).item(0).getChildNodes().item(0).setNodeValue(tagValue);
            else
                document.getElementsByTagName(tag).item(0).appendChild(document.createTextNode(tagValue));
        } catch (Exception e) {
            Timber.e("Unable to auto-fill: %s %s", tag, tagValue);
        }
    }

    @Override
    public void updateFormBasedOnIdentifier(String formIdentifier, String tag, String tagValue) {
        fetchSpecificFormID(formIdentifier, formID -> {
            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, formID);
            String fileName = ContentResolverHelper.getFormPath(formUri);
            FileOutputStream fos = null;
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = null;
                try {
                    document = builder.parse(new File(fileName));
                    document.getDocumentElement().normalize();
                } catch (Exception e) {
                    Timber.d(" Exception for form " + formIdentifier + " exception is " + e.getMessage());
                }
                if (document != null) {
                    prefillFormBasedOnTags(document, tag, tagValue);
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    fos = new FileOutputStream(new File(fileName));
                    StreamResult result = new StreamResult(fos);
                    transformer.transform(source, result);
                }
            } catch (ParserConfigurationException | IOException | TransformerException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public ArrayList<String> fetchMediaDirs(String referenceFileName) {
        return CSVHelper.fetchFormMediaDirectoriesWithMedia(referenceFileName);
    }

    @Override
    public void buildCSV(CSVBuildStatusListener csvBuildStatusListener, ArrayList<String> mediaDirectoriesNames, JSONArray inputData, String mediaFileName) {
        CSVHelper.buildCSVForODK(csvBuildStatusListener, mediaDirectoriesNames, inputData, mediaFileName);
    }

    @Override
    public void observeStorageMigration(Context context) {
        if (!Collect1.getInstance().getStorageStateProvider().isScopedStorageUsed()) {
            context.startActivity(new Intent(context, StorageMigrationActivity.class));
        }
    }

    @Override
    public boolean isScopedStorageUsed() {
        return Collect1.getInstance().getStorageStateProvider().isScopedStorageUsed();
    }

    @Override
    public boolean allowClick(String name) {
        return MultiClickGuard.allowClick(name);
    }

    @Override
    public int getBottomDialogTheme() {
        return new ThemeUtils(Collect1.getInstance().getAppContext()).getBottomDialogTheme();
    }

    @Override
    public void enableUsingScopedStorage() {
        Collect1.getInstance().getStorageStateProvider().enableUsingScopedStorage();
    }

    @Override
    public String getFormsPath() {
        return Collect1.getInstance().getStoragePathProvider().getDirPath(StorageSubdirectory.FORMS);
    }

    @Override
    public String getRootPath() {
        return Collect1.getInstance().getStoragePathProvider().getStorageRootDirPath();

    }

    @Override
    public void sendAnalyticsAdoptionEvent(String s, boolean b) {
        if (b) {
            Collect1.getInstance().getAnalytics().logEvent("app_installed_school_teacher", "install_info", s);
        } else {
            Collect1.getInstance().getAnalytics().logEvent("app_installed_mentor_monitor", "install_info", s);
        }
    }

    @Override
    public void setServerUrl(String odkServerUrl) {
        mOdkServerUrl = odkServerUrl ;
    }

    @Override
    public void checkOdkFormIdAvailableInDb(String formId, ICheckFormIdExistInDbCallBack listener) {
        getDownloadedFormsNamesFromDatabase(new FormsCallback() {
            boolean formIdFound = false;

            @Override
            public void onProceed(@NonNull List<Form> formsListFromDb) {
                Timber.d("FormId to be opened : " + formId + " FormsFromDatabase existing has size: " +
                        formsListFromDb.size());
                for (Form form : formsListFromDb) {
                    if (formId.equals(form.getJrFormId())) {
                        formIdFound = true;
                        break;
                    }
                }
                listener.onFormIdExistBoolean(formIdFound);
            }
        });
    }

    @Override
    public void launchViewSubmittedFormsView(Context context, HashMap<String, Object> toolbarModificationObject) {
        Intent i = new Intent(context, InstanceChooserList.class);
        i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                ApplicationConstants.FormModes.VIEW_SENT);
        i.putExtra(Constants.KEY_CUSTOMIZE_TOOLBAR, toolbarModificationObject);
        context.startActivity(i);
        HashMap<String, Object> extras = toolbarModificationObject;
//                UtilityFunctions.generateT/d Forms", true);
//        ODKDriver.launchInstanceUploaderListActivity(context, extras);
    }

    @Override
    public void launchViewUnsubmittedFormView(Context context, String className, HashMap<String, Object> toolbarModificationObject) {

        if (MultiClickGuard.allowClick(className)) {
            Intent i = new Intent(context, InstanceUploaderListActivity.class);
            i.putExtra(Constants.KEY_CUSTOMIZE_TOOLBAR, toolbarModificationObject);
            context.startActivity(i);
        }
    }

    @Override
    public void fetchSpecificFormID(String formIdentifier, IGetOnSpecificFormIdCallBack listener) {
        getDownloadedFormsNamesFromDatabase(formsListFromDb -> {
            HashMap<Integer, String> hashMap = new HashMap<>();
            for (int i = 0; i < formsListFromDb.size(); i++) {
                hashMap.put(Integer.valueOf(formsListFromDb.get(i).getId().toString()), formsListFromDb.get(i).getJrFormId());
            }
            for (Map.Entry<Integer, String> entry : hashMap.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(formIdentifier)){
                    listener.onGetFormId(entry.getKey());
                    return;
                }
            }
            listener.onGetFormId(0);
        });
    }


    private boolean shouldUpdate() {
        return true;
    }

    @Override
    public List<Form> getDownloadedFormsNamesFromDatabase() {
        FormsDao fd = new FormsDao();
        Cursor cursor = fd.getFormsCursor();
        return FormsDao.getFormsFromCursor(cursor);
    }

    @Override
    public void getDownloadedFormsNamesFromDatabase(FormsCallback callback) {
        new Thread(() -> {
            FormsDao fd = new FormsDao();
            Cursor cursor = fd.getFormsCursor();
            List<Form> formsFromCursor = FormsDao.getFormsFromCursor(cursor);
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onProceed(formsFromCursor));
            }
        }).start();
    }

    private List<Instance> getDownloadedInstancesNamesFromDatabase() {
        InstancesDao fd = new InstancesDao();
        Cursor cursor = fd.getInstancesCursor();
        return InstancesDao.getInstancesFromCursor(cursor);
    }

    @Override
    public void checkIfODKFormsMatch(String subject, ArrayList<FormStructure> filteredFormList, ICheckAllOdkFormsAvailableInDbCallBack listener) {
        getDownloadedFormsNamesFromDatabase(formsListFromDb -> {
            int formsFromDbCount = 0;
            Timber.d("formsListToBeDownloaded from Firebase has size : " + filteredFormList.size() + " FormsFromDatabase existing has size: " +
                    formsListFromDb.size());

            for (Form form : formsListFromDb) {
                for (FormStructure formStructure : filteredFormList) {
                    if (formStructure.getFormID().equals(form.getJrFormId())) formsFromDbCount += 1;
                }
            }
            listener.onCheckAllOdkFormsAvailableInDb(formsFromDbCount == filteredFormList.size());
        });
    }

    @Override
    public void startDownloadODKFormListTask(FormListDownloadResultCallback formListDownloadResultCallback) {
        DownloadFormListTask downloadFormListTask = new DownloadFormListTask(Collect1.getInstance().getDDon());
        downloadFormListTask.setDownloaderListener(value -> {
            if (value != null && !value.containsKey("dlerrormessage")) {
                formListDownloadResultCallback.onSuccessfulFormListDownload(value);
            } else {
                assert value != null;
                if (value.containsKey("dlerrormessage")) {
                    formListDownloadResultCallback.onFailureFormListDownload(true);
                } else {
                    formListDownloadResultCallback.onFailureFormListDownload(false);
                }
            }
        });
        downloadFormListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void getDownloadableAssessmentsList(HashMap<String, String> configFormList,
                                               HashMap<String, ServerFormDetails> latestFormListFromServer, String subject, FormsMapCallback callback) {
        getDownloadedFormsNamesFromDatabase(formsListFromDb -> {
            HashMap<String, ServerFormDetails> formsToBeDownloadedABC = new HashMap<>();
            Iterator it = latestFormListFromServer.entrySet().iterator();
            // Delete excess forms
            ArrayList<String> formsToBeDeleted = new ArrayList<>();
//        for (Form form : formsFromDB) {
//            if (!configFormList.containsKey(form.getJrFormId())) {
//                formsToBeDeleted.add(form.getMD5Hash());
//            }
//        }
            // Adding new forms
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                ServerFormDetails fd = (ServerFormDetails) pair.getValue();
                String formID = fd.getFormId();
                boolean foundFormInDB = false;
                if (configFormList.containsKey(fd.getFormId())) {
                    for (Form form : formsListFromDb) {
                        // Check if forms needs to be updated
                        if (form.getJrFormId().equals(fd.getFormId())) {
                            foundFormInDB = true;
                            boolean nullTest = false;
                            if (form.getJrVersion() == null && fd.getFormVersion() == null)
                                nullTest = true;
                            if (form.getJrVersion() == null && fd.getFormVersion() != null) {
                                formsToBeDownloadedABC.put(fd.getFormId(), fd);
                                formsToBeDeleted.add(form.getMD5Hash());
                            } else if (!nullTest && !form.getJrVersion().equals(fd.getFormVersion())) {
                                formsToBeDownloadedABC.put(fd.getFormId(), fd);
                                formsToBeDeleted.add(form.getMD5Hash());
                            }
                        }
                    }
                    if (!foundFormInDB) {
                        formsToBeDownloadedABC.put(fd.getFormId(), fd);
                    }
                }
            }
            if (formsToBeDeleted.size() > 0 && formsToBeDeleted.toArray() != null) {
                new FormsDao().deleteFormsFromMd5Hash(formsToBeDeleted.toArray(new String[0]));
            }
            callback.onProceed(formsToBeDownloadedABC);
        });
    }

    @Override
    public void downloadODKForms(DataFormDownloadResultCallback dataFormDownloadResultCallback,
                                 HashMap<String, ServerFormDetails> forms, boolean isODKAggregate) {
        ArrayList<ServerFormDetails> filesToDownload = new ArrayList<>();
        Iterator it = forms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String formID = ((ServerFormDetails) pair.getValue()).getFormId();
//                String fileName = Collect.FORMS_PATH + File.separator + formName + ".xml";
//            String serverURL = new WebCredentialsUtils().getServerUrlFromPreferences();

            //            String partURL = "/www/formXml?formId=";
            String downloadUrl = mOdkServerUrl + "/forms/" + formID + ".xml";
            String manifestUrl = mOdkServerUrl + "/forms/" + formID + "/manifest";
            ServerFormDetails fm = new ServerFormDetails(
                    ((ServerFormDetails) pair.getValue()).getFormName(),
                    downloadUrl,
                    manifestUrl,
                    formID,
                    ((ServerFormDetails) pair.getValue()).getFormVersion(),
                    ((ServerFormDetails) pair.getValue()).getHash(),
                    ((ServerFormDetails) pair.getValue()).getManifestFileHash(),
                    false,
                    false);
            filesToDownload.add(fm);
            it.remove();
        }
        DownloadFormsTask downloadFormsTask = new DownloadFormsTask();
        downloadFormsTask.setDownloaderListener(new DownloadFormsTaskListener() {
            @Override
            public void formsDownloadingComplete(HashMap<ServerFormDetails, String> result) {
                ArrayList<String> formIdsFailedList = new ArrayList<>();
                for (Map.Entry<ServerFormDetails, String> entry : result.entrySet()) {
                    formIdsFailedList.remove(entry.getKey().getFormId());
                }

                if (result != null) {
                    int successCount = 0;
                    int totalExpected = result.size();
                    for (Map.Entry<ServerFormDetails, String> entry : result.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().equals("Success"))
                            successCount += 1;
                        formIdsFailedList.remove(entry.getValue());
                    }

                    if (successCount == totalExpected) {
                        dataFormDownloadResultCallback.formsDownloadingSuccessful(result);
                    } else {
                        OdkFormsDownloadInLocalResponseData data = new OdkFormsDownloadInLocalResponseData(successCount, totalExpected, formIdsFailedList);
                        dataFormDownloadResultCallback.formsDownloadingFailure(data);
                    }
                } else {
                    OdkFormsDownloadInLocalResponseData data = new OdkFormsDownloadInLocalResponseData(-1, -1, formIdsFailedList);
                    // passing -1 if results are empty.
                    dataFormDownloadResultCallback.formsDownloadingFailure(data);
                }
            }

            @Override
            public void progressUpdate(String currentFile, int progress, int total) {
                Timber.tag("PROGRESS").d(String.valueOf(progress));
                dataFormDownloadResultCallback.progressUpdate(currentFile, progress, total);
            }

            @Override
            public void formsDownloadingCancelled() {
                dataFormDownloadResultCallback.formsDownloadingCancelled();
            }
        });


        downloadFormsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filesToDownload);
    }

    private void checkForm(String formId, Context context, FormProcessListener listener) {
        new Thread(() -> {
            if (!isDownloadRequired(formId)) {
                listener.onProcessed();
                return;
            }
            listener.onProcessingStart();
            if (!CommonUtilities.isNetworkAvailable(context)) {
                listener.onCancelled(new Exception("Network connection not available"));
                return;
            }
            startDownloadODKFormListTask(new FormListDownloadResultCallback() {
                @Override
                public void onSuccessfulFormListDownload(HashMap<String, ServerFormDetails> serverForms) {
                    ServerFormDetails requiredForm = serverForms.get(formId);
                    if (requiredForm != null) {
                        DownloadFormsTask downloadFormsTask = new DownloadFormsTask();
                        downloadFormsTask.setDownloaderListener(new DownloadFormsTaskListener() {
                            @Override
                            public void formsDownloadingComplete(HashMap<ServerFormDetails, String> result) {
                                listener.onProcessed();
                            }
                            @Override
                            public void progressUpdate(String currentFile, int progress, int total) {}
                            public void formsDownloadingCancelled() { listener.onCancelled(new Exception("Form could not be downloaded!")); }
                        });
                        ArrayList<ServerFormDetails> formsList = new ArrayList<>(Arrays.asList(requiredForm));
                        downloadFormsTask.execute(formsList);
                    } else {
                        listener.onCancelled(new Exception("Form is not present!"));
                    }
                }
                @Override
                public void onFailureFormListDownload(boolean isAPIFailure) {
                    listener.onCancelled(new Exception("Form cannot be downloaded!"));
                }
            });
        }).start();
    }

    private boolean isDownloadRequired(String formId) {
        FormsDao formsDao = new FormsDao();
        Cursor formsCursor = formsDao.getFormsCursorForFormId(formId);
        if (formsCursor == null || formsCursor.getCount() == 0) {
            return true;
        }
        List<Form> forms = FormsDao.getFormsFromCursor(formsCursor);
        if (forms.size() == 0) {
            return true;
        }
        //TODO : if there are multiple forms with forms id and different versions then they need to be sorted acc, to their version
//        Collections.sort(forms, (form1, form2) -> -form1.getJrVersion().compareTo(form2.getJrVersion()));
        Form requiredForm = forms.get(0);
        StoragePathProvider provider = new StoragePathProvider();
        String formsDir = provider.getDirPath(StorageSubdirectory.FORMS);
        File xmlFile = new File(formsDir + File.separator + requiredForm.getFormFilePath());
        File mediaFolder = new File(formsDir + File.separator + requiredForm.getFormMediaPath());
        if (!xmlFile.exists() || !mediaFolder.exists()) {
            try {
                formsDao.deleteFormsFromMd5Hash(requiredForm.getMD5Hash());
                xmlFile.delete();
                FileUtils.deleteDirectory(mediaFolder);
            } catch (IOException e) {
                Timber.e(e);
            }
            return true;
        }
        return false;
    }
}