/*
* Helper is used to call META DATA api
* that will help to fetch
* #Meta data : actors, assessment_types, subjects, designations,
* #Competency mapping ; competencies to be assessed,
* #Workflow_ref_ids ; ids Mapped to the Competencies
* */

package com.samagra.parent.helper

import com.data.db.DbHelper
import com.data.db.models.entity.Actor
import com.data.db.models.entity.AssessmentType
import com.data.db.models.entity.Competency
import com.data.db.models.entity.Designation
import com.data.db.models.entity.ReferenceIds
import com.google.gson.Gson
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.ancillaryscreens.data.model.RetrofitService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.models.FormStructure
import com.samagra.commons.models.chaptersdata.ChapterMapping
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.commons.models.metadata.MetaDataRemoteResponse
import com.samagra.commons.models.metadata.Subjects
import com.samagra.commons.models.metadata.WorkflowRefIds
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.getBearerAuthToken
import io.realm.RealmList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit

object MetaDataHelper {

    private val apiService by lazy { generateApiService() }

    private val gson by lazy { Gson() }

    private val metadataDao by lazy { DbHelper.db.getMetadataDao() }

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

    private suspend fun parseAndStoreWorkflowReferenceIdData(
        workflowRefIdListRemote: ArrayList<WorkflowRefIds>?,
        subjects: ArrayList<Subjects>?,
        prefs: CommonsPrefsHelperImpl
    ) {
        workflowRefIdListRemote?.let { list ->
            val workflowRefIdsList: MutableList<ReferenceIds> = mutableListOf()

            withContext(Dispatchers.Default){
                list.forEach { item ->
                    workflowRefIdsList.add(
                        ReferenceIds(
                            competencyId = item.competencyId!!,
                            grade = item.grade!!,
                            isActive = item.isActive,
                            refIds = item.refIds?.toMutableList() ?: mutableListOf(),
                            subjectId = item.subjectId!!,
                            type = item.type!!,
                            assessmentTypeId = item.assessmentTypeId
                        )
                    )
                }
            }

            withContext(Dispatchers.IO){
                metadataDao.insertReferenceIds(workflowRefIdsList)
            }
        }

        val odkFormsSet = mutableSetOf<FormStructure>()
        val chapterMappingListRealm = ArrayList<ChapterMapping>()

        withContext(Dispatchers.Default){
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
        }

        withContext(Dispatchers.IO){
            RealmStoreHelper.deleteChapterMapping()
            RealmStoreHelper.insertChapterMapping(chapterMappingListRealm)
            prefs.updateFormConfiguredListText(gson.toJson(odkFormsSet))
            DataSyncRepository().checkODKFormsUpdates(
                subject = "-",
                networkConnected = true,
                prefs = prefs
            )
        }
    }

    suspend fun parseAndStoreCompetencyData(
        competencyList: ArrayList<CompetencyModel>?,
        prefs: CommonsPrefsHelperImpl
    ) {
        withContext(Dispatchers.IO) {
            prefs.saveCompetencyData(gson.toJson(competencyList))
        }

        competencyList?.let { competencyModelList ->
            // Convert CompetencyModel objects to Competency objects using map and forEach
            val competenciesList = withContext(Dispatchers.Default) {
                competencyModelList.map { competencyModel ->
                    Competency(
                        id = competencyModel.cId ?: 0,
                        subjectId = competencyModel.subjectId ?: 0,
                        grade = competencyModel.grade ?: 0,
                        learningOutcome = competencyModel.learningOutcome ?: "",
                        flowState = competencyModel.flowState ?: 0,
                        month = competencyModel.month ?: ""
                    )
                }
            }
            withContext(Dispatchers.IO) {
                metadataDao.insertCompetency(competenciesList)
            }
        }
    }

    suspend fun parseAndStoreMetaData(
        response: MetaDataRemoteResponse,
        prefs: CommonsPrefsHelperImpl
    ) {
        withContext(Dispatchers.Default) {
            response.actors?.let { actors ->
                withContext(Dispatchers.IO) {
                    prefs.saveActorsList(MetaDataExtensions.convertActorsToJson(actors))
                }

                val actorsList = actors.map { actor ->
                    Actor(id = actor.id, name = actor.name)
                }

                withContext(Dispatchers.IO) {
                    metadataDao.insertActors(actorsList)
                }
            }

            response.assessmentTypes?.let { assessmentTypes ->
                withContext(Dispatchers.IO) {
                    prefs.saveAssessmentTypesList(MetaDataExtensions.convertAssessmentTypesToJson(assessmentTypes))
                }

                val assessmentsList = assessmentTypes.map { assessmentType ->
                    AssessmentType(id = assessmentType.id, name = assessmentType.name)
                }

                withContext(Dispatchers.IO) {
                    metadataDao.insertAssessmentTypes(assessmentsList)
                }
            }

            response.subjects?.let { subjects ->
                withContext(Dispatchers.IO) {
                    prefs.saveSubjectsList(MetaDataExtensions.convertSubjectsToJson(subjects))
                }

                val subjectsList = subjects.map { subject ->
                    com.data.db.models.entity.Subjects(id = subject.id, name = subject.name)
                }

                withContext(Dispatchers.IO) {
                    metadataDao.insertSubjects(subjectsList)
                }
            }

            response.designations?.let { designations ->
                withContext(Dispatchers.IO) {
                    prefs.saveDesignationsList(MetaDataExtensions.convertDesignationsToJson(designations))
                }

                val designationsList = designations.map { designation ->
                    Designation(id = designation.id, name = designation.name)
                }

                withContext(Dispatchers.IO) {
                    metadataDao.insertDesignations(designationsList)
                }
            }
        }
    }

    private fun generateApiService(): RetrofitService? {
        return Network.getClient(
            ClientType.RETROFIT,
            RetrofitService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )
    }

    suspend fun setMetaDataResponse(
        response: MetaDataRemoteResponse?,
        prefs: CommonsPrefsHelperImpl
    ): Boolean {
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