/*
* Helper is used to call META DATA api
* that will help to fetch
* #Meta data : actors, assessment_types, subjects, designations,
* #Competency mapping ; competencies to be assessed,
* #Workflow_ref_ids ; ids Mapped to the Competencies
* */

package com.samagra.parent.helper

import com.google.gson.Gson
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.ancillaryscreens.data.model.RetrofitService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.models.FormStructure
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.commons.models.metadata.MetaDataRemoteResponse
import com.samagra.commons.models.metadata.Subjects
import com.samagra.commons.models.metadata.WorkflowRefIds
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.getBearerAuthToken
import com.samagra.commons.models.chaptersdata.ChapterMapping
import io.realm.RealmList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

object MetaDataHelper {

    private val apiService by lazy { generateApiService() }

    private val gson by lazy { Gson() }

    suspend fun fetchMetaData(
        prefs: CommonsPrefsHelperImpl,
        enforce: Boolean = false
    ): StateFlow<Boolean?> {
        Timber.d("fetchMetaData: enforced: $enforce")
        val remoteResponseStatus: MutableStateFlow<Boolean?> = MutableStateFlow(null)

        if (!enforce) {
            val hoursFromPreviousFetch = TimeUnit.MILLISECONDS.toHours(
                Date().time - prefs.previousMetadataFetch
            )
            Timber.d("fetchMetaData hours from previous fetch: $hoursFromPreviousFetch")
            val minHourDiffForFetch = RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getDouble(RemoteConfigUtils.METADATA_FETCH_DELTA_IN_HOURS)
            Timber.d("fetchMetaData min diff for fetch: $minHourDiffForFetch")
            if (hoursFromPreviousFetch < minHourDiffForFetch) {
                remoteResponseStatus.emit(false)
                return remoteResponseStatus
            }
        }

        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            Timber.d("fetchMetaData: no network")
            remoteResponseStatus.emit(false)
        }

        try {
            val response = apiService?.fetchMetaData(apiKey = prefs.getBearerAuthToken())
            remoteResponseStatus.emit(setMetaDataResponse(response, prefs))
        } catch (t: Exception) {
            Timber.e(t, "fetchMetaData error: %s", t.message)
            remoteResponseStatus.emit(false)
        }
        return remoteResponseStatus
    }

    private fun parseAndStoreWorkflowReferenceIdData(
        workflowRefIdListRemote: ArrayList<WorkflowRefIds>?,
        subjects: ArrayList<Subjects>?,
        prefs: CommonsPrefsHelperImpl
    ) {
        val odkFormsSet = mutableSetOf<FormStructure>()
        val chapterMappingListRealm = ArrayList<ChapterMapping>()
        workflowRefIdListRemote?.forEach { workFlowRefId ->
            val chapterMappingData = ChapterMapping()
            chapterMappingData.grade = workFlowRefId.grade ?: 0
            chapterMappingData.subjectId = workFlowRefId.subjectId ?: 0
            chapterMappingData.competencyId = workFlowRefId.competencyId.toString()
            chapterMappingData.type = workFlowRefId.type
            val realmList = RealmList<String>()
            val refIdsList = workFlowRefId.refIds
            refIdsList?.forEach { refId ->
                realmList.add(refId)
            }
            chapterMappingData.refIds = realmList
            chapterMappingData.assessmentTypeId = workFlowRefId.assessmentTypeId ?: 0
            chapterMappingData.isActive = workFlowRefId.isActive ?: true
            chapterMappingListRealm.add(chapterMappingData)
            if (chapterMappingData.type.equals(CommonConstants.ODK)) {
                val subject = subjects?.firstOrNull { subject ->
                    subject.id == workFlowRefId.subjectId
                }?.name ?: ""
                val formName = "Grade " + workFlowRefId.grade + " - " + subject
                workFlowRefId.refIds?.forEach { refId ->
                    odkFormsSet.add(FormStructure(refId, formName, subject))
                }
            }
        }
        RealmStoreHelper.deleteChapterMapping()
        RealmStoreHelper.insertChapterMapping(chapterMappingListRealm)
        prefs.updateFormConfiguredListText(gson.toJson(odkFormsSet))
        DataSyncRepository().checkODKFormsUpdates(
            subject = "-",
            networkConnected = true,
            prefs = prefs
        )
    }

    fun parseAndStoreCompetencyData(
        competencyList: ArrayList<CompetencyModel>?,
        prefs: CommonsPrefsHelperImpl
    ) {
        prefs.saveCompetencyData(gson.toJson(competencyList))
    }


    fun parseAndStoreMetaData(
        response: MetaDataRemoteResponse,
        prefs: CommonsPrefsHelperImpl
    ) {
        response.actors?.let {
            prefs.saveActorsList(MetaDataExtensions.convertActorsToJson(it))
        }
        response.assessmentTypes?.let {
            prefs.saveAssessmentTypesList(MetaDataExtensions.convertAssessmentTypesToJson(it))
        }
        response.subjects?.let {
            prefs.saveSubjectsList(MetaDataExtensions.convertSubjectsToJson(it))
        }
        response.designations?.let {
            prefs.saveDesignationsList(MetaDataExtensions.convertDesignationsToJson(it))
        }
    }

    private fun generateApiService(): RetrofitService? {
        return Network.getClient(
            ClientType.RETROFIT,
            RetrofitService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )
    }

    fun setMetaDataResponse(response: MetaDataRemoteResponse?, prefs: CommonsPrefsHelperImpl): Boolean {
        response?.let {
            Timber.d("fetchMetaData: response got")
            parseAndStoreMetaData(
                response = it,
                prefs = prefs
            )
            parseAndStoreCompetencyData(
                competencyList = it.competencyMapping,
                prefs = prefs
            )
            parseAndStoreWorkflowReferenceIdData(
                workflowRefIdListRemote = it.workflowRefIds,
                subjects = it.subjects,
                prefs = prefs
            )
            prefs.previousMetadataFetch = System.currentTimeMillis()
            return true
        }
        return false
    }
}