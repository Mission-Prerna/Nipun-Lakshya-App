package com.samagra.parent.helper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.ancillaryscreens.data.model.AssessmentService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.models.submitresultsdata.ResultsVisitData
import com.samagra.commons.models.submitresultsdata.StudentResults
import com.samagra.commons.models.submitresultsdata.SubmitResultsModel
import com.samagra.commons.models.surveydata.AssessmentSurveyModel
import com.samagra.commons.models.surveydata.SurveyModel
import com.samagra.commons.models.surveydata.SurveyResultsModel
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.BuildConfig
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData
import com.samagra.parent.ui.getBearerAuthToken
import timber.log.Timber

class SyncingHelper {

    fun syncSurveys(prefs: CommonsPrefsHelperImpl): Boolean {
        val surveyResultsList = RealmStoreHelper.getSurveys()
        return syncSurveys(prefs, surveyResultsList)
    }
    fun syncSurveys(prefs: CommonsPrefsHelperImpl, surveyResultsList : List<AssessmentSurveyModel>): Boolean {
        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            return false
        }
        Timber.i("Survey Results " + surveyResultsList.size)
        if (surveyResultsList.isNotEmpty()) {
            val surveys = mutableListOf<SurveyModel>()
            val service = generateApiService()
            surveyResultsList.forEach {
                val actorId = MetaDataExtensions.getActorId(
                    it.actor, prefs.actorsListJson
                )
                val subjectId = MetaDataExtensions.getSubjectId(
                    it.subject, prefs.subjectsListJson
                )
                val resultsData = Gson().fromJson(
                    it.results, SurveyResultsModel::class.java
                )
                val surveyModel = SurveyModel(
                    it.submissionTimeStamp,
                    it.grade,
                    actorId,
                    subjectId,
                    it.schoolUdise,
                    BuildConfig.VERSION_CODE,
                    resultsData.results
                )
                surveys.add(surveyModel)
            }
            val response = service.insertSurvey(prefs.getBearerAuthToken(), surveys).execute()
            if (response.isSuccessful) {
                Timber.i("Survey Results Success")
                RealmStoreHelper.deleteSurveyResults()
                return true
            }
            Timber.i("Survey Results Failure")
        } else {
            return true
        }
        return false
    }

    fun syncAssessments(prefs: CommonsPrefsHelperImpl): Boolean {
        val resultSubmissionList = getAssessmentSubmissions(prefs)
        return syncAssessments(prefs, resultSubmissionList)
    }

    fun syncAssessments(prefs: CommonsPrefsHelperImpl, resultSubmissionList : ArrayList<SubmitResultsModel>): Boolean {
        Timber.i("Submissions List " + resultSubmissionList.size)
        return try{
            if (resultSubmissionList.size > 0) {
                postAssessmentSubmissions(resultSubmissionList, prefs)
            } else {
                true
            }
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }


    fun getAssessmentSubmissions(prefs: CommonsPrefsHelperImpl): ArrayList<SubmitResultsModel> {
        val resultSubmissionList = arrayListOf<SubmitResultsModel>()
        val assessmentsMap = mutableMapOf<String?, MutableList<ResultsVisitData>>()
        val resultsFromDb = RealmStoreHelper.getAssessmentResults()
        Timber.d("getAssessmentSubmissions: ${resultsFromDb.size}")
        if (resultsFromDb.isNotEmpty()) {
            resultsFromDb.forEach { resultsVisitData ->
                var results = assessmentsMap[resultsVisitData.flowUUID]
                if (results == null) {
                    results = mutableListOf()
                }
                results.add(resultsVisitData)
                assessmentsMap[resultsVisitData.flowUUID] = results
            }

            Timber.d("getAssessmentSubmissions: map size: ${assessmentsMap.size}")
            assessmentsMap.forEach { entry ->
                val studentResults = arrayListOf<StudentResults>()
                val specificResults = entry.value
                // fetch ids from the meta data
                val setOfStudentSession = HashSet<String>()
                specificResults.forEach { resultsVisitData ->
                    setOfStudentSession.add(resultsVisitData.studentSession ?: "")
                }
                val actorId = MetaDataExtensions.getActorId(
                    specificResults[0].actor, prefs.actorsListJson
                )
                val subjectId = MetaDataExtensions.getSubjectId(
                    specificResults[0].subject, prefs.subjectsListJson
                )
                val assessmentTypeId = MetaDataExtensions.getAssessmentTypeId(
                    specificResults[0].assessment_type, prefs.assessmentTypesListJson
                )
                // One assessment can hold multiple students.
                // Create StudentResults array and pass array to SubmitResultsModel (assessment data) request.
                specificResults.forEach { resultsVisitData ->
                    val resultsData = Gson().fromJson<List<StudentsAssessmentData>>(
                        resultsVisitData.module_result,
                        object : TypeToken<List<StudentsAssessmentData?>?>() {}.type
                    )

                    // list contains only one value
                    val results = resultsData[0].studentResults
                    val moduleResult = results.moduleResult
                    val studentResultsData = StudentResults(
                        results.studentName,
                        results.competencyId.toInt(),
                        moduleResult.module,
                        moduleResult.endTime,
                        moduleResult.isPassed,
                        moduleResult.startTime,
                        moduleResult.statement,
                        moduleResult.achievement,
                        moduleResult.totalQuestions,
                        moduleResult.successCriteria,
                        moduleResult.sessionCompleted,
                        NetworkStateManager.instance?.networkConnectivityStatus,
                        results.workflowRefId,
                        UtilityFunctions.getTimeInSeconds(resultsVisitData.total_time_taken),
                        resultsVisitData.studentSession,
                        if (results.odkResultsData != null) {
                            results.odkResultsData.results
                        } else {
                            arrayListOf()
                        }
                    )
                    studentResults.add(studentResultsData)
                }
                val resultsData = Gson().fromJson<List<StudentsAssessmentData>>(
                    specificResults[0].module_result,
                    object : TypeToken<List<StudentsAssessmentData?>?>() {}.type
                )
                var udiseCode = specificResults[0].udise_code
                if (udiseCode == "null" || udiseCode == "") {
                    udiseCode = "0"
                }
                val submitResultsModel = SubmitResultsModel(
                    specificResults[0].submissionTimeStamp,
                    specificResults[0].grade,
                    subjectId,
                    specificResults[0].mentor_id,
                    actorId,
                    resultsData[0].studentResults.schoolsData.blockId,
                    assessmentTypeId,
                    udiseCode?.toLong(),
                    setOfStudentSession.size,
                    studentResults,
                    BuildConfig.VERSION_CODE,
                    entry.key
                )
                resultSubmissionList.add(submitResultsModel)
            }
        }
        return resultSubmissionList
    }

    private fun postAssessmentSubmissions(
        resultSubmissionList: ArrayList<SubmitResultsModel>,
        prefs: CommonsPrefsHelperImpl
    ): Boolean {
        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            return false
        }
        var isSuccess = true;
        resultSubmissionList.chunked(
            RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getString(RemoteConfigUtils.RESULTS_INSERTION_CHUNK_SIZE).toInt()
        ) {
            val flowUUIDs = arrayListOf<String?>()
            it.forEach { model ->
                flowUUIDs.add(model.flowUUID)
            }
            Timber.i("flowUUID list to be deleted! $flowUUIDs")
            val response = generateApiService().insertVisitsResultsSync(
                prefs.getBearerAuthToken(), it
            ).execute()
            if (response.isSuccessful) {
                RealmStoreHelper.deleteVisitResults(flowUUIDs)
                Timber
                    .i("Assessment Response Success : New Realm Size ${RealmStoreHelper.getAssessmentResults()}")
//                Timber.tag("Morziz-Worker").i("Assessment Response Success")
            } else {
                // Currently not stopping loop if API fails
                isSuccess = false
                Timber
                    .i("Assessment Response Failure" + response.message())
                Timber.i("Assessment Response Failure" + response.code())
                Timber
                    .i("Assessment Response Failure" + response.errorBody()?.string())
            }


        }
        return isSuccess
    }

    private fun generateApiService(): AssessmentService {
        return Network.getClient(
            ClientType.RETROFIT,
            AssessmentService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )!!
    }
}