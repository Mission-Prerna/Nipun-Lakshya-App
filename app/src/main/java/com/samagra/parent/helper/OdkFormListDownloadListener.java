package com.samagra.parent.helper;

import android.content.Context;

import com.samagra.commons.models.FormStructure;
import com.samagra.commons.utils.FormDownloadStatus;
import com.samagra.grove.logging.Grove;
import com.samagra.parent.ui.student_learning.studenthome.OdkResponseListener;

import org.odk.collect.android.contracts.FormListDownloadResultCallback;
import org.odk.collect.android.contracts.IFormManagementContract;
import org.odk.collect.android.formmanagement.ServerFormDetails;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("rawtypes")
public class OdkFormListDownloadListener implements FormListDownloadResultCallback {
    private final OdkResponseListener listener;
    Context context;
    IFormManagementContract iFormManagementContract;
    ArrayList<FormStructure> filteredFormList;


    public OdkFormListDownloadListener(IFormManagementContract iFormManagementContract,
                                       ArrayList<FormStructure> filteredFormList, OdkResponseListener listener) {
        this.iFormManagementContract = iFormManagementContract;
        this.filteredFormList = filteredFormList;
        this.listener = listener;
    }

    @Override
    public void onSuccessfulFormListDownload(HashMap<String, ServerFormDetails> latestFormListFromServer) {
        Grove.d("FormList download complete %s, is the form list size", latestFormListFromServer.size());

        HashMap<String, String> configFormList = generateFormMap(filteredFormList);
        // Download Forms if updates available or if forms not downloaded. Delete forms if not applied for the role.
        iFormManagementContract.
                getDownloadableAssessmentsList(configFormList, latestFormListFromServer, filteredFormList.get(0).getSubject(), newAssessmentsToBeDownloadedMap -> {
                    FormDownloadStatus formsDownloadStatus;

//        listener.onUpdateLoaderStatus("3");
//        Log.e("-->>", "ccccc home form load code running from here!");
        /*if (context != null) {
            if (context instanceof OdkInstructionActivity) {
                ((OdkInstructionActivity) context).loaderStatus("3");
            } else {
                ((SubjectInstructionScreen) context).loaderStatus("3");
            }
        }*/
                    if (newAssessmentsToBeDownloadedMap.size() > 0) {
                        Grove.d("Number of forms to be downloaded are %d", newAssessmentsToBeDownloadedMap.size());
                        formsDownloadStatus = FormDownloadStatus.DOWNLOADING;
                    } else {
                        Grove.d("No new forms to be downloaded");
                        formsDownloadStatus = FormDownloadStatus.SUCCESS;
                        listener.renderLayoutVisible("No new forms to be downloaded", 1);
            /*if (context != null) {
                if (context instanceof OdkInstructionActivity) {
                    ((OdkInstructionActivity) context).renderLayoutVisible();
                } else {
                    ((SubjectInstructionScreen) context).renderLayoutVisible();
                }
            }*/
                    }
                    if (formsDownloadStatus == FormDownloadStatus.DOWNLOADING) {
                        listener.startFormDownloading(newAssessmentsToBeDownloadedMap);
                        if (context != null) {
                            /*if (context instanceof OdkInstructionActivity) {
                             *//*FormManagementCommunicator.getContract().downloadODKForms(
                           new StudentFormDownloadListenerNew(),
                            newAssessmentsToBeDownloadedMap,
                            true
                    );*//*
                    ((OdkInstructionActivity) context).startFormDownloading(newAssessmentsToBeDownloadedMap);
                } else {
                    subjectInstructionsPresenter.startFormDownloading(newAssessmentsToBeDownloadedMap,context);
                }*/
                        }
                    }
                });
    }

    private HashMap<String, String> generateFormMap(ArrayList<FormStructure> filteredFormList) {
        HashMap<String,String> map = new HashMap<>();
        for(FormStructure form :filteredFormList){
            map.put(form.getFormID(),form.getFormName());
        }
        return map;
    }

    @Override
    public void onFailureFormListDownload(boolean isAPIFailure) {
        if (isAPIFailure) {
            listener.onFailure(null);
            Grove.e("There has been an error in downloading the forms from ODK Server");
        }
listener.renderLayoutVisible("from form download failure",0);
    }
}
