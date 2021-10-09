package com.samagra.parent.ui

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.di.FormManagementCommunicator
import com.samagra.commons.BuildConfig
import com.samagra.commons.basemvvm.BaseRepository
import com.samagra.commons.models.FormStructure
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.commons.utils.FileDownloadListener
import com.samagra.commons.utils.FileDownloaderTask
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import com.samagra.parent.helper.OdkFormListDownloadListener
import com.samagra.parent.ui.student_learning.studenthome.OdkFormDownloadListener
import com.samagra.parent.ui.student_learning.studenthome.OdkResponseListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.odk.collect.android.OdkFormsDownloadInLocalResponseData
import org.odk.collect.android.contracts.ICheckAllOdkFormsAvailableInDbCallBack
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.listeners.ZipExtractListener
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ZipExtractorTask
import timber.log.Timber
import java.io.File

class DataSyncRepository : BaseRepository() {

    fun downloadWorkFlowConfigFromRemoteConfig(): String {
        return getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.ASSESSMENT_WORKFLOW_CONFIG)
//        return AppConstants.flowConfig

    }

    fun fetchCompetencies(prefs: CommonsPrefsHelperImpl): ArrayList<CompetencyModel> {
        return try {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<CompetencyModel>>() {}.type
            //            return gson.fromJson(AppConstants.COMPETENCY_JSON, type)
            gson.fromJson(prefs.competencyData, type)
        } catch (e: Exception) {
            Timber.e("Error fetching competencies $e")
            ArrayList()
        }
    }

    fun checkODKFormsUpdates(
        subject: String,
        networkConnected: Boolean,
        prefs: CommonsPrefsHelperImpl,
        listener: OdkResponseListener? = null
    ) {
        checkZipUpdate(subject, networkConnected, prefs, listener)
    }

    private fun checkZipUpdate(
        subject: String,
        networkConnected: Boolean,
        prefs: CommonsPrefsHelperImpl,
        listener: OdkResponseListener?
    ) {
        val localZipHash: String? = prefs.getString("zipHash", null)
        val updatedZipHash: String =
            getFirebaseRemoteConfigInstance().getString("ZIP_HASH")
        if (localZipHash == null || localZipHash != updatedZipHash) {
            if (networkConnected) {
                val tempZipFile = File.createTempFile(
                    "form_data",
                    ".zip",
                    File(StoragePathProvider().getDirPath(StorageSubdirectory.CACHE))
                )
                val formsDirectory = StoragePathProvider().scopedStorageRootDirPath + File.separator
                val fileDownloaderTask = FileDownloaderTask(tempZipFile.absolutePath)
                fileDownloaderTask.setListener(object : FileDownloadListener {
                    override fun onComplete() {
                        ZipExtractorTask(tempZipFile.absolutePath,
                            formsDirectory,
                            object : ZipExtractListener {
                                override fun onProgress(progress: Int) {
                                    listener?.onUpdateLoaderStatus(progress)
                                }

                                override fun onComplete(error: String?) {
                                    if (error == null) {
                                        prefs.putString("zipHash", updatedZipHash)
                                        checkIndividualFormUpdate(
                                            subject = subject,
                                            networkConnected = networkConnected,
                                            prefs = prefs,
                                            listener = listener
                                        )
                                    } else listener?.showFailureDownloadMessage()
                                }
                            }).execute()
                    }

                    override fun onProgress(progress: Int) {
                        listener?.onUpdateLoaderStatus(progress)
                    }

                    override fun onCancelled(exception: java.lang.Exception?) {
                        listener?.onFailure(null)
                    }

                })
                fileDownloaderTask.execute(
                    if (BuildConfig.DEBUG)
                        BuildConfig.STAGING_FORM_ZIP_URL
                    else getFirebaseRemoteConfigInstance().getString(RemoteConfigUtils.FORMS_ZIP_URL)
                )
            } else {
                listener?.renderLayoutVisible("no internet connection", 0)
            }
        } else {
            checkIndividualFormUpdate(
                subject = subject,
                networkConnected = networkConnected,
                prefs = prefs,
                listener = listener
            )
        }
    }

    private fun checkIndividualFormUpdate(
        subject: String,
        networkConnected: Boolean,
        prefs: CommonsPrefsHelperImpl,
        listener: OdkResponseListener?
    ) {
        CoroutineScope(Job()).launch {
            val filteredFormList: ArrayList<FormStructure> =
                FormManagementCommunicator.getContract().downloadFormList(
                    prefs.getString("assessment_form_list1", "")
                )

            FormManagementCommunicator.getContract().checkIfODKFormsMatch(subject,
                filteredFormList,
                object : ICheckAllOdkFormsAvailableInDbCallBack {
                    override fun onCheckAllOdkFormsAvailableInDb(isFormIdExist: Boolean) {
                        if (isFormIdExist) {
                            listener?.renderLayoutVisible("all forms are downloaded. show UI", 1)
                        } else {
                            downloadNonAvailableForms(
                                networkConnected, filteredFormList, listener
                            )
                        }
                    }
                })
        }
    }

    private fun downloadNonAvailableForms(
        networkConnected: Boolean,
        filteredFormList: ArrayList<FormStructure>,
        listener: OdkResponseListener?
    ) {
        if (networkConnected) {
            FormManagementCommunicator.getContract()
                .startDownloadODKFormListTask(
                    OdkFormListDownloadListener(
                        FormManagementCommunicator.getContract(),
                        filteredFormList,
                        object : OdkResponseListener {
                            override fun onFailure(data: OdkFormsDownloadInLocalResponseData?) {
                                listener?.onFailure(data)
                            }

                            override fun onUpdateLoaderStatus(value: Int) {
                                listener?.onUpdateLoaderStatus(value)
                            }

                            override fun renderLayoutVisible(
                                msg: String, status: Int
                            ) {
                                listener?.renderLayoutVisible(msg, status)
                            }

                            override fun showFailureDownloadMessage() {
                                listener?.showFailureDownloadMessage()
                            }

                            override fun startFormDownloading(
                                newAssessmentsToBeDownloaded: HashMap<String, ServerFormDetails>
                            ) {
//                                Log.e("-->>", "ccccc startFormDownloading specific home")
                                downloadFormsInAppStorage(newAssessmentsToBeDownloaded, listener)
                            }
                        })
                )
        } else {
            listener?.renderLayoutVisible("no internet connection", 0)
        }
    }

    private fun downloadFormsInAppStorage(
        newAssessmentsToBeDownloaded: HashMap<String, ServerFormDetails>,
        listener: OdkResponseListener?
    ) {
        FormManagementCommunicator.getContract().downloadODKForms(
            OdkFormDownloadListener(object : OdkResponseListener {
                override fun onFailure(data: OdkFormsDownloadInLocalResponseData?) {
                    listener?.onFailure(data)
                }

                override fun onUpdateLoaderStatus(value: Int) {
                    listener?.onUpdateLoaderStatus(value)
                }

                override fun renderLayoutVisible(
                    msg: String, status: Int
                ) {
                    listener?.renderLayoutVisible(
                        msg, status
                    )
                }

                override fun showFailureDownloadMessage() {
                    listener?.showFailureDownloadMessage()
                }

                override fun startFormDownloading(
                    newAssessmentsToBeDownloaded: HashMap<String, ServerFormDetails>
                ) {

                }
            }),
            newAssessmentsToBeDownloaded,
            true,
        )
    }

    fun downloadFormsLength(prefs: CommonsPrefsHelperImpl) {
        val quesLength = getFirebaseRemoteConfigInstance()
            .getLong(RemoteConfigUtils.ODK_FORM_QUES_LENGTH)
        prefs.saveODKFormQuesLength(quesLength.toInt())
    }

    fun getHelpFaqListFromFirebase(): String {
        return getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.HELP_FAQ_JSON)
    }

    fun getHelpFaqFormUrlFromFirebase(): String {
        return getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.HELP_FAQ_FORM_URL)
    }

    fun getDetailsSelectionInfoNote(): String {
        return getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.INFO_NOTES_DETAIL_SELECTION)

    }
}