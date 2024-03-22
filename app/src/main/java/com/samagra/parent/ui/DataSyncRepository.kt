package com.samagra.parent.ui

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.BuildConfig
import com.samagra.commons.basemvvm.BaseRepository
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import com.samagra.parent.ui.student_learning.studenthome.OdkResponseListener
import io.samagra.odk.collect.extension.listeners.FileDownloadListener
import io.samagra.odk.collect.extension.utilities.ODKProvider
import org.json.JSONArray
import org.json.JSONException
import org.odk.collect.android.formmanagement.FormDownloadException
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.listeners.DownloadFormsTaskListener
import timber.log.Timber
import java.io.File

class DataSyncRepository : BaseRepository() {

    fun downloadWorkFlowConfigFromRemoteConfig(): String {
        return getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.ASSESSMENT_WORKFLOW_CONFIG)
    }

    fun fetchCompetencies(prefs: CommonsPrefsHelperImpl): ArrayList<CompetencyModel> {
        return try {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<CompetencyModel>>() {}.type
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
                ODKProvider.getNetworkStorageInteractor().downloadFormsZip(
                    if (BuildConfig.DEBUG) BuildConfig.STAGING_FORM_ZIP_URL
                    else getFirebaseRemoteConfigInstance().getString(RemoteConfigUtils.FORMS_ZIP_URL),
                    object : FileDownloadListener {
                        override fun onProgress(progress: Int) {
                            listener?.onUpdateLoaderStatus(progress)
                        }
                        override fun onComplete(downloadedFile: File) {
                            prefs.putString("zipHash", updatedZipHash)
                            checkIndividualFormUpdate(
                                subject = subject,
                                networkConnected = networkConnected,
                                prefs = prefs,
                                listener = listener
                            )
                        }
                        override fun onCancelled(exception: Exception) {
                            listener?.onFailure()
                        }
                    }
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
        val storedFormListString: String? = prefs.getString(UserConstants.FORMS_LIST_PREFS_KEY, null)
        if (!storedFormListString.isNullOrBlank()) {
            try {
                val jsonArrayList = JSONArray(storedFormListString)
                val formList: ArrayList<String> = ArrayList()
                for (i in 0 until jsonArrayList.length()) {
                    formList.add(jsonArrayList.getString(i))
                }
                ODKProvider.getFormsNetworkInteractor()
                    .downloadFormsFromIdList(
                        formList,
                        object : DownloadFormsTaskListener {
                            override fun formsDownloadingComplete(result: MutableMap<ServerFormDetails, FormDownloadException>?) {
                                listener?.renderLayoutVisible("all forms are downloaded. show UI", 1)
                            }
                            override fun progressUpdate(
                                currentFile: String?,
                                progress: Int,
                                total: Int
                            ) {
                                listener?.onUpdateLoaderStatus(progress)
                            }
                            override fun formsDownloadingCancelled() {
                                listener?.showFailureDownloadMessage()
                            }
                        }
                    )
            }
            catch (e: JSONException) {
                e.printStackTrace()
            }
        }
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