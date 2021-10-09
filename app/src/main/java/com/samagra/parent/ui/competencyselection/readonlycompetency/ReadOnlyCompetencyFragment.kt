/*
* class is called in "Nipun-lashya" flow
* */
package com.samagra.parent.ui.competencyselection.readonlycompetency

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.benasher44.uuid.uuid4
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.CommonUtilities
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseFragment
import com.samagra.commons.constants.Constants
import com.samagra.commons.getLastCharAsInt
import com.samagra.commons.getPercentage
import com.samagra.commons.models.chaptersdata.ChapterMapping
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.models.submitresultsdata.ResultsVisitData
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.PostHogManager.capture
import com.samagra.commons.posthog.PostHogManager.createContext
import com.samagra.commons.posthog.PostHogManager.createProperties
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.getNipunCriteria
import com.samagra.commons.utils.playMusic
import com.samagra.parent.*
import com.samagra.parent.databinding.FragmentReadOnlyCompetencyBinding
import com.samagra.parent.helper.RealmStoreHelper
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.competencyselection.*
import com.samagra.parent.ui.finalresults.FinalResultsNlActivity
import com.samagra.parent.ui.individualresultnl.IndividualNlResultActivity
import com.samagra.parent.ui.newresultafterdietmentor.ResultNewAfterDietMentorActivity
import com.samagra.parent.ui.withArgs
import com.samagra.workflowengine.WorkflowProperty
import com.samagra.workflowengine.workflow.WorkflowManager
import com.samagra.workflowengine.workflow.WorkflowUtils
import com.samagra.workflowengine.workflow.model.WorkflowConfig
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import com.samagra.workflowengine.workflow.model.stateresult.StateResult
import kotlinx.coroutines.*
import org.odk.collect.android.utilities.SnackbarUtils
import org.odk.collect.android.utilities.ToastUtils
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val INDIVIDUAL_RESULT_REQUEST_CODE: Int = 9
const val SURVEY_SCREEN_REQUEST_CODE: Int = 8

class ReadOnlyCompetencyFragment :
    BaseFragment<FragmentReadOnlyCompetencyBinding, CompetencySelectionVM>() {

    private val gson by lazy { Gson() }

    private lateinit var chapterMappingMap: HashMap<Int, ChapterMapping>
    private lateinit var filteredChapterMappingList: ArrayList<ChapterMapping>
    private var stateLedFinalResultsList: ArrayList<StateLedResultModel> = ArrayList()
    private lateinit var studentNipunMap: LinkedHashMap<String, StateLedResultModel>
    private var nextStudent: Boolean = false
    private var totalTime: Long = 0
    private val prefs by lazy { initPrefs() }
    private var currentStudentCount: Int = 1
    private var currentCompetencyIndex: Int = 0
    private var studentCompletedAssessment: Int = 0
    private var totalWorkflowSessions: Int = 0
    private lateinit var workflowConfig: WorkflowConfig
    private lateinit var workflowManager: WorkflowManager
    private var selectedStudentCount: String = ""
    private var subjectName: String = ""
    private var resultMap: LinkedHashMap<String, ArrayList<StudentsAssessmentData>>? = null
    private var sessionTimeMap: HashMap<String, Long>? = null

    //    private var resultMap: HashMap<String, String>? = null
    private val filteredCompetencyList: ArrayList<CompetencyModel> = ArrayList()
    private var gradeValue: Int = 0
    private lateinit var subject: String
    private lateinit var grade: String
    private lateinit var competenciesDataList: ArrayList<CompetencyModel>
    private var schoolsData: SchoolsData? = SchoolsData()
    private var finalResult: ArrayList<StudentsAssessmentData> = ArrayList()

    //    private var resultsToPostList: ArrayList<StudentsAssessmentData> = ArrayList()
    private lateinit var resultsPerStudent: ArrayList<StudentsAssessmentData>

    /*
    *LIst to get individual results
    *  */
    private lateinit var finalResultIndividualList: ArrayList<StudentsAssessmentData>
    private var studentIdentifier = ""

    @LayoutRes
    override fun layoutId() = R.layout.fragment_read_only_competency

    private val resultListener: WorkflowManager.WorkflowResultListener =
        object : WorkflowManager.WorkflowResultListener {

            override fun onComplete(stateResults: List<StateResult>) {
//                Log.e("-->>", "results after workFlowManager done size. ${stateResults.size}")
                if (stateResults.isNotEmpty()) {
                    onWorkflowManagerComplete(stateResults)
                } else {
                    Timber.e("ReadOnlycompetencyFragment Results list is empty! unable to proceed workflow")
                    SnackbarUtils.showShortSnackbar(
                        binding.llView,
                        getString(R.string.error_generic_message)
                    )
                    WorkflowManager.setWorkflowManagerAsNull()
                    viewModel.getCompetenciesData(prefs)
                }
            }

            override fun onError(s: String) {
                SnackbarUtils.showShortSnackbar(binding.llView, s)
            }
        }

    private fun startWorkflowWithMultipleCompetencies(
        assessmentStateResult: StudentsAssessmentData?,
        shouldStartNewFlow: Boolean = true,
        shouldOpenFinalResults: Boolean = false
    ) {
        checkIfLastIndividualResult(assessmentStateResult)
        if (shouldStartNewFlow) {
            createStudentUUID()
        }
        /*
        * run recursion for current competency. First result will be null.
        * */
        if (currentCompetencyIndex < filteredCompetencyList.size) {
            putResultsInMap(assessmentStateResult)
            openWorkFlow(filteredCompetencyList[currentCompetencyIndex])
        } else {
            redirectToIndividualNlResultScreen(finalResultIndividualList)
        }
        if (shouldOpenFinalResults) {
            endFlowRedirectToFinalResults()
        }
    }

    private fun onWorkflowManagerComplete(stateResults: List<StateResult>) {
        val assessmentStateResult: AssessmentStateResult = stateResults[0] as AssessmentStateResult
        // if combined result gets then list size is two, add check and fetch both objects
        // and set type as combined to set adapter UI.
//        val timeResults = stateResults as List<AssessmentStateResult>
        for (results in stateResults) {
            val time = results as AssessmentStateResult
            val timeTaken = UtilityFunctions.getTimeDifferenceMilis(
                time.moduleResult.startTime,
                time.moduleResult.endTime
            )
            totalTime += timeTaken
        }
        val studentsAssessmentData = if (stateResults.size > 1) {
            val assessmentStateResultCombined = stateResults[1] as AssessmentStateResult
            if (currentStudentCount == 1) {
                addDummyItem()
            }
            // combined final results
            createStudentAssessmentDataObject(
                AppConstants.TYPE_COMBINED,
                assessmentStateResult,
                assessmentStateResultCombined
            )
        } else {
            // bolo and odk final results.
            createStudentAssessmentDataObject(
                assessmentStateResult.moduleResult.module,
                assessmentStateResult,
                null
            )
        }
        finalResult.add(studentsAssessmentData)
        finalResultIndividualList.add(studentsAssessmentData)
        if (studentsAssessmentData.studentResults.moduleResult.sessionCompleted && studentsAssessmentData.studentResultsOdk == null) {
            studentCompletedAssessment++
        } else if (studentsAssessmentData.studentResults.moduleResult.sessionCompleted && studentsAssessmentData.studentResultsOdk?.moduleResult?.sessionCompleted == true) {
            studentCompletedAssessment++
        } else {
//            Log.e("-->>", "no need to add in final results list anything")
        }
        startWorkflowWithMultipleCompetencies(
            studentsAssessmentData,
            shouldStartNewFlow = false,
            shouldOpenFinalResults = false
        )
    }

    private fun insertTempResultsInDb(resultMapString: String) {
        val type =
            object : TypeToken<LinkedHashMap<String, ArrayList<StudentsAssessmentData>>>() {}.type
        val map = gson.fromJson<LinkedHashMap<String, ArrayList<StudentsAssessmentData>>>(
            resultMapString,
            type
        )
        val flattenAllResultsList = flattenAllResultsOfMap(map)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            insertResultsOnDb(flattenAllResultsList)
        }
    }

    private fun endFlowRedirectToFinalResults() {
        studentNipunMap = LinkedHashMap()
        val flattenAllResultsList = flattenAllResultsOfMap(resultMap)
//        Log.e("-->>", "result list after flatten $flattenAllResultsList")
        val nipunStudentWithCompetencyMap = HashMap<CompetencyDatum, Int>()
        val completeNipunMap = HashMap<String, Int>()
        flattenAllResultsList.forEach { oneLoop ->
            // key cId value
            var completeNipunCount = completeNipunMap[oneLoop.studentSession ?: "0"]
            if (completeNipunCount == null) {
                completeNipunCount = 0
            }
            val type: Type = object : TypeToken<List<StudentsAssessmentData>>() {}.type
            val arrayItems =
                gson.fromJson<ArrayList<StudentsAssessmentData>>(oneLoop.module_result, type)
            val studentResults = arrayItems[0].studentResults
            val competencyDatum =
                CompetencyDatum(studentResults.competencyId.toInt(), studentResults.competency)
            var count: Int? = nipunStudentWithCompetencyMap[competencyDatum]
            if (count == null) {
                count = 0
            }
            /*
            * check if student is NIPUN
            * */
            if (checkIfStudentNipun(arrayItems[0].viewType, studentResults)) {
                count++
                completeNipunCount++
                completeNipunMap[oneLoop.studentSession ?: "0"] = completeNipunCount
            }
            nipunStudentWithCompetencyMap[competencyDatum] = count
        }
        Timber.e("result complete nipun map $completeNipunMap")
//        Log.e("-->>", "result hashMap map $hashMap")
        if (studentCompletedAssessment != totalWorkflowSessions) {
            sendCountMismatchEvent()
        }

        handleInsertionAndRedirections(
            flattenAllResultsList,
            nipunStudentWithCompetencyMap,
            completeNipunMap
        )
    }

    private fun sendCountMismatchEvent() {
        val list = ArrayList<Cdata>()
        list.add(Cdata("studentCompletedAssessment", "" + studentCompletedAssessment))
        list.add(Cdata("totalWorkflowSessions", "" + totalWorkflowSessions))
        val mentorDetailsFromPrefs = prefs.mentorDetailsData
        mentorDetailsFromPrefs?.let {
            list.add(Cdata("userId", "" + it.id))
        }
        val properties = createProperties(
            page = COMPETENCY_SELECTION_SCREEN,
            eventType = EVENT_TYPE_SCREEN_VIEW,
            eid = EID_IMPRESSION,
            context = createContext(APP_ID, DATA_MIS_MATCH, list),
            eData = null,
            objectData = null,
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        )
        capture(requireContext(), EVENT_DATA_MIS_MATCH, properties)
    }

    private fun handleInsertionAndRedirections(
        flattenAllResultsList: ArrayList<ResultsVisitData>,
        nipunStudentWithCompetencyMap: HashMap<CompetencyDatum, Int>,
        completeNipunMap: HashMap<String, Int>
    ) {
        when (prefs.selectedUser) {
            AppConstants.USER_EXAMINER -> {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    insertResultsOnDb(flattenAllResultsList)
                    redirectToFinalResultsScreenTeacher(
                        nipunStudentWithCompetencyMap,
                        completeNipunMap
                    )
                }
            }
            AppConstants.USER_TEACHER -> {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    insertResultsOnDb(flattenAllResultsList)
                    redirectToFinalResultsScreenTeacher(
                        nipunStudentWithCompetencyMap,
                        completeNipunMap
                    )
                }
            }
            AppConstants.USER_MENTOR -> {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    insertResultsOnDb(flattenAllResultsList)
                    redirectToFinalResultsScreenTeacher(
                        nipunStudentWithCompetencyMap,
                        completeNipunMap
                    )
                }
            }
            Constants.USER_DIET_MENTOR -> {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    insertResultsOnDb(flattenAllResultsList)
                    if (prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT) {
                        redirectToFinalResultsScreenTeacher(
                            nipunStudentWithCompetencyMap,
                            completeNipunMap
                        )
                    } else {
                        redirectToFinalResultsScreenTeacher(
                            nipunStudentWithCompetencyMap,
                            completeNipunMap
                        )
                    }
                }
            }
            AppConstants.USER_PARENT -> {
                redirectToFinalResultsScreenTeacher(nipunStudentWithCompetencyMap, completeNipunMap)
            }
        }
    }

    private suspend fun insertResultsOnDb(
        flattenAllResultsList: ArrayList<ResultsVisitData>
    ) {
        prefs.removeKey(CommonConstants.ASSESSMENT_RESULTS_TEMP)
        coroutineScope {
            if (flattenAllResultsList.isNotEmpty()) {
                val async = async(Dispatchers.IO) {
                    flattenAllResultsList[0].udise_code?.let {
                        RealmStoreHelper.updateSchoolsVisitStatus(
                            it.toLong()
                        )
                    }
                    RealmStoreHelper.insertFinalResultsList(flattenAllResultsList)
                }
                async.await()
            }
        }
    }

    private fun checkIfStudentNipun(
        viewType: String,
        studentResults: AssessmentStateResult
    ): Boolean {
        if (viewType == ODK) {
            val nipunCriteria = AppConstants.ODK_CRITERIA_KEY.getNipunCriteria(
                studentResults.grade,
                studentResults.subject
            )
            val odkResultsData = studentResults.odkResultsData
            odkResultsData?.let {
                val percentage = getPercentage(
                    odkResultsData.totalMarks.toInt(),
                    odkResultsData.totalQuestions
                )
                return percentage >= nipunCriteria
            }
            return false
        } else {
            val nipunCriteria = AppConstants.READ_ALONG_CRITERIA_KEY.getNipunCriteria(
                studentResults.grade,
                studentResults.subject
            )
            val achievement = studentResults.moduleResult.achievement
            return (achievement ?: 0) >= nipunCriteria
        }
    }

    private fun redirectToIndividualNlResultScreen(finalResult: ArrayList<StudentsAssessmentData>) {
        if (finalResult.isNotEmpty()) {
            if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER, true)
                || prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT
            ) {
                redirectToFinalResultsScreenNew()
            } else {
                val intent = Intent(context, IndividualNlResultActivity::class.java)
                intent.putExtra(
                    AppConstants.INTENT_SCHOOL_DATA,
                    finalResult[0].studentResults.schoolsData
                )
                intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, finalResult)
                startActivityForResult(intent, INDIVIDUAL_RESULT_REQUEST_CODE)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INDIVIDUAL_RESULT_REQUEST_CODE) {
            val showFinalResults =
                data?.getBooleanExtra(AppConstants.SHOW_FINAL_RESULTS, false)
            showFinalResults?.let {
                startAssessmentWithNextStudent(
                    shouldStartNewFlow = !it,
                    shouldOpenFinalResults = it
                )
            }
        } else if (requestCode == SURVEY_SCREEN_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    startAssessmentFlow()
                }
                Activity.RESULT_CANCELED -> {
                    ToastUtils.showShortToast(getString(R.string.fill_survey_first))
                }
                else -> {
                    startAssessmentFlow()
                }
            }
        }
    }

    private fun flattenAllResultsOfMap(resultMap: LinkedHashMap<String, ArrayList<StudentsAssessmentData>>?): ArrayList<ResultsVisitData> {
        val flattenResultsList = ArrayList<ResultsVisitData>()
        var sessionTimeOfStudent: Long
        putSessionTimeOfOneStudentInMap()
        val flowUUID = CommonUtilities.createUUID()
        val submissionTimeStamp = Date().time
//        Log.e("-->>", "result map before flatten ${resultMap.toString()}")
        resultMap?.forEach { (studentSessionID, allResultsList) ->
            for (moduleResultItem in allResultsList) {
                if (this::studentNipunMap.isInitialized) {
                    createMapForMoolyavanResult(studentSessionID, moduleResultItem)
                }
                sessionTimeOfStudent = if (sessionTimeMap?.containsKey(studentSessionID) == true) {
                    sessionTimeMap?.get(studentSessionID) ?: 0
                } else {
                    0
                }
                val moduleResultList = ArrayList<StudentsAssessmentData>()
                moduleResultList.add(moduleResultItem)
                val finalResultJsonString = gson.toJson(moduleResultList)
                val studentResults = moduleResultItem.studentResults
                val resultsVisitData = ResultsVisitData(
                    submissionTimeStamp,
                    prefs.mentorDetailsData?.id,
                    flowUUID,
                    studentResults.grade,
                    studentResults.subject,
                    true,
                    finalResultJsonString,
                    moduleResultList.size,
                    studentResults.schoolsData.udise.toString(),
                    UtilityFunctions.getTimeString(sessionTimeOfStudent),
                    prefs.selectedUser,
                    getBlock(studentResults.schoolsData.block),
                    studentSessionID,
                    prefs.assessmentType
                )
                flattenResultsList.add(resultsVisitData)
            }
        }
        return flattenResultsList
    }

    private fun createMapForMoolyavanResult(
        studentSessionIDKey: String,
        item: StudentsAssessmentData
    ) {
        val isStudentNipun = checkIfStudentNipun(item.viewType, item.studentResults)
        val mapValue = studentNipunMap[studentSessionIDKey]
        if (mapValue != null && !mapValue.isNipun) {
            return
        }

        studentNipunMap[studentSessionIDKey] = StateLedResultModel(
            item.studentResults.studentName,
            isStudentNipun, item.studentResults.moduleResult.sessionCompleted
        )
    }

    private fun putSessionTimeOfOneStudentInMap() {
        var totalTimeOfOneStudentAssessment: Long = 0
        resultMap?.forEach { (studentSessionID, allResultsList) ->
            for (moduleResultItem in allResultsList) {
                val moduleResult = moduleResultItem.studentResults.moduleResult
                val timeTaken = UtilityFunctions.getTimeDifferenceMilis(
                    moduleResult.startTime,
                    moduleResult.endTime
                )
                totalTimeOfOneStudentAssessment += timeTaken
            }
            sessionTimeMap?.put(studentSessionID, totalTimeOfOneStudentAssessment)
        }
    }

    private fun startAssessmentWithNextStudent(
        shouldStartNewFlow: Boolean = true,
        shouldOpenFinalResults: Boolean = false
    ) {
        nextStudent = true
        currentStudentCount++
        resultsPerStudent = ArrayList()
        finalResultIndividualList = ArrayList()
        if (!shouldOpenFinalResults) {
            currentCompetencyIndex = 0
        }
        startWorkflowWithMultipleCompetencies(null, shouldStartNewFlow, shouldOpenFinalResults)
    }

    /*
    * Handle last result from the workflow manager for the last competency of every student
    * */
    private fun checkIfLastIndividualResult(assessmentStateResult: StudentsAssessmentData?) {
        if (studentIdentifier.isNotEmpty() && currentCompetencyIndex + 1 > filteredCompetencyList.size) {
            putResultsInMap(assessmentStateResult)
            if (prefs.selectedUser.equals(AppConstants.USER_PARENT, true).not()) {
                prefs.putString(CommonConstants.ASSESSMENT_RESULTS_TEMP, gson.toJson(resultMap))
            }
        }
    }

    /*
    * Create student session that is unique for every student.
    * Calls first time and then after taking assessments with given competencies (if next student is selected)
    * */
    private fun createStudentUUID() {
        if (nextStudent) {
            nextStudent = false
            studentIdentifier = uuid4().toString()
            Timber.e("New Student identifier $studentIdentifier")
        }
    }

    /*
    * get result from the workflow manager and store it in map with student session along with {one student's result list}
    * */
    private fun putResultsInMap(assessmentStateResult: StudentsAssessmentData?) {
        assessmentStateResult?.let {
            resultsPerStudent.add(it)
            resultMap?.put(studentIdentifier, resultsPerStudent)
            if (prefs.selectedUser.equals(AppConstants.USER_PARENT, true).not()) {
                prefs.putString(CommonConstants.ASSESSMENT_RESULTS_TEMP, gson.toJson(resultMap))
            }
        }
    }

    /*
    * Add dummy Data to show headings card as first data in results screen.
    * Add object only one time.
    * */
    private fun addDummyItem() {
        val dummyStateResult = AssessmentStateResult()
        dummyStateResult.studentName = AppConstants.COMBINED_STUDENT
        val dummyObject = createStudentAssessmentDataObject(
            AppConstants.TYPE_COMBINED,
            AssessmentStateResult(),
            dummyStateResult
        )
        finalResult.add(0, dummyObject)
    }

    private fun createStudentAssessmentDataObject(
        type: String,
        assessmentStateResult1: AssessmentStateResult,
        assessmentStateResult2: AssessmentStateResult?
    ): StudentsAssessmentData {
        return StudentsAssessmentData(type, assessmentStateResult1, assessmentStateResult2)
    }

    private fun redirectToFinalResultsScreen(
        hashMap: HashMap<CompetencyDatum, Int>,
        completeNipunMap: HashMap<String, Int>
    ) {
        val intent = Intent(activity, FinalResultsNlActivity::class.java)
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, finalResult)
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_MAP, hashMap)
        intent.putExtra(AppConstants.INTENT_STUDENT_COUNT, resultMap?.size)
        intent.putExtra(AppConstants.INTENT_COMPLETE_NIPUN_MAP, completeNipunMap)
        intent.putExtra("tt", totalTime)
        intent.putExtra("studentCompleteAssessment", studentCompletedAssessment)
        activity?.startActivity(intent)
    }

    private fun redirectToFinalResultsScreenNew() {
        val intent = Intent(activity, AssessmentCompleteActivity::class.java)
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, finalResult)
        intent.putExtra(AppConstants.INTENT_STUDENT_COUNT, resultMap?.size)
        intent.putExtra("tt", totalTime)
        intent.putExtra("studentCompleteAssessment", studentCompletedAssessment)
        startActivityForResult(intent, INDIVIDUAL_RESULT_REQUEST_CODE)
    }

    private fun redirectToFinalResultsScreenTeacher(
        hashMap: HashMap<CompetencyDatum, Int>,
        completeNipunMap: HashMap<String, Int>
    ) {
        val intent = Intent(activity, ResultNewAfterDietMentorActivity::class.java)
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, finalResult)
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_MAP, hashMap)
        stateLedFinalResultsList.clear()
        studentNipunMap.forEach { entry ->
            stateLedFinalResultsList.add(entry.value)
        }
        intent.putExtra(AppConstants.INTENT_FINAL_STATE_LED_RESULT_LIST, stateLedFinalResultsList)
        intent.putExtra(AppConstants.INTENT_STUDENT_COUNT, resultMap?.size)
        intent.putExtra(AppConstants.INTENT_COMPLETE_NIPUN_MAP, completeNipunMap)
        intent.putExtra("tt", totalTime)
        intent.putExtra("studentCompleteAssessment", studentCompletedAssessment)
        context?.startActivity(intent)
    }

    private fun getBlock(block: String?): String? {
        return if (prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
            block
        } else {
            null
        }
    }

    override fun getBaseViewModel(): CompetencySelectionVM {
        val repository = CompetencySelectionRepository()
        val dataSyncRepo = DataSyncRepository()
        val viewModelProviderFactory = ViewModelProviderFactory(
            appCompatActivity!!.application,
            repository,
            dataSyncRepo
        )
        return ViewModelProvider(this, viewModelProviderFactory)[CompetencySelectionVM::class.java]
    }

    override fun getBindingVariable() = BR.viewModel

    private fun initPrefs() = CommonsPrefsHelperImpl(activity as Context, "prefs")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getDataFromArgs()
        //  setupUI()
        //TODO by neeraj new check
        setUpUINew()
        checkIfTemporaryResultsArePresent()
        resultMap = LinkedHashMap()
        resultsPerStudent = ArrayList()
        sessionTimeMap = HashMap()
        finalResultIndividualList = ArrayList()
        setObservers()
        Glide.with(context!!).load(R.drawable.ic_flying_bird).into(binding.animeMascot)
        viewModel.getCompetenciesData(prefs)
        setClickListeners()
        context?.playMusic(R.raw.start_assessment_audio)
        super.onViewCreated(view, savedInstanceState)
    }

    /*
    * Temporary results will prevent data loss if between the one assessment of any student if app kills by OS.
    * */
    private fun checkIfTemporaryResultsArePresent() {
        val tempResults: String? = prefs.getString(CommonConstants.ASSESSMENT_RESULTS_TEMP, "")
        tempResults?.let {
            if (it != "") {
                LogEventsHelper.addTelemetryEventOnAppProcessKill(
                    grade = grade,
                    subject = subject,
                    assessmentType = prefs.assessmentType,
                    context = context,
                    screen = READ_ONLY_COMPETENCY_SCREEN,
                    pId = NL_APP_READ_ONLY_COMPETENCY
                )
                insertTempResultsInDb(it)
            }
        }
    }

    private fun setObservers() {
        with(viewModel) {
            observe(competenciesList, ::handleCompetencyData)
        }
    }

    private fun handleCompetencyData(competencyList: ArrayList<CompetencyModel>?) {
        competencyList?.let {
            initWorkflowManager()
//            it.add(CompetencyModel(0, false, 4, 1, "Nipun Lakshya Math 1", "", 101, 1))
//            it.add(CompetencyModel(0, false, 5, 2, "Nipun Lakshya Hindi 3", "", 102, 1))
//            it.add(CompetencyModel(0, false, 6, 1, "Nipun Lakshya Math 2", "", 103, 1))
//            it.add(CompetencyModel(0, false, 7, 2, "Nipun Lakshya Hindi 4", "", 104, 1))
//            it.add(CompetencyModel(0, false, 8, 2, "Nipun Lakshya Hindi 5", "", 105, 1))
            workflowConfig.flowConfigs =
                WorkflowUtils.getWorkflowConfigForCompetencies(competencyList, prefs)
            competenciesDataList = it
            setCompetencyAdapter()
        }
        prepareChapterMappingList()
    }

    private fun prepareChapterMappingList() {
        filteredChapterMappingList = ArrayList()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val chapterMappingList: ArrayList<ChapterMapping> =
                withContext(Dispatchers.IO) {
                    getFilteredChapterMapping(filteredChapterMappingList)
                }
            filteredChapterMappingList = chapterMappingList
        }
    }

    private suspend fun getFilteredChapterMapping(arrayList: ArrayList<ChapterMapping>): ArrayList<ChapterMapping> {
        chapterMappingMap = HashMap()
        val chapterMapping = RealmStoreHelper.getChapterMapping()
        filteredCompetencyList.forEach { competencyModel ->
            chapterMapping.forEach { refIdsMapping ->
                if (competencyModel.cId == refIdsMapping.competencyId.toInt()) {
                    val assessmentType = MetaDataExtensions.getAssessmentTypeFromId(
                        chapterMappingMap[competencyModel.cId]?.assessmentTypeId ?: 0,
                        prefs.assessmentTypesListJson
                    )
                    if (prefs.assessmentType == Constants.STATE_LED_ASSESSMENT) {
                        if (!chapterMappingMap.containsKey(competencyModel.cId)
                            || assessmentType != Constants.STATE_LED_ASSESSMENT
                        ) {
                            chapterMappingMap[competencyModel.cId] = refIdsMapping
                        }
                    } else {
                        if (!chapterMappingMap.containsKey(competencyModel.cId)
                            && assessmentType != Constants.STATE_LED_ASSESSMENT
                        ) {
                            chapterMappingMap[competencyModel.cId] = refIdsMapping
                        }
                    }
                }
            }
        }
        Timber.e("mapmap ${gson.toJson(chapterMappingMap)}")
        chapterMappingMap.values.forEach {
            arrayList.add(it)
        }
        return arrayList
    }

    private fun setClickListeners() {
        binding.mtlBtnStartAssessment.setOnClickListener {
            if (filteredCompetencyList.isNotEmpty() && filteredChapterMappingList.isNotEmpty()) {
                gotoNewScreen()
            } else {
                ToastUtils.showShortToast(getString(R.string.warning_no_competency_data))
            }
        }
        /*binding.animeMascot.setOnClickListener {
            context?.playMusic(R.raw.hello_chalo_seekhte_hain)
        }*/
    }

    private fun gotoNewScreen() {
        if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER, true)) {
            val intent = Intent(context, AssessmentSurveyActivity::class.java)
            intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
            intent.putExtra(AppConstants.INTENT_SELECTED_GRADE, gradeValue)
            intent.putExtra(AppConstants.INTENT_SELECTED_SUBJECT, subject)
//        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, finalResult)
            startActivityForResult(intent, SURVEY_SCREEN_REQUEST_CODE)
        } else {
            startAssessmentFlow()
        }
    }

    private fun startAssessmentFlow() {
        val listOfCompetencyIds: List<Int> = filteredCompetencyList.map {
            it.id
        }
        context?.let {
            LogEventsHelper.setPostHogEventOnStartFlow(
                context = it,
                competencyId = listOfCompetencyIds.toString(),
                assessmentType = prefs.assessmentType,
                screen = READ_ONLY_COMPETENCY_SCREEN,
                pId = NL_APP_READ_ONLY_COMPETENCY,
                ePageId = NL_READ_ONLY_COMPETENCY
            )
        }
        studentCompletedAssessment = 0
        nextStudent = true
        currentStudentCount = 1
        currentCompetencyIndex = 0
        startWorkflowWithMultipleCompetencies(
            null,
            shouldStartNewFlow = true,
            shouldOpenFinalResults = false
        )
    }

    private fun openWorkFlow(competencyData: CompetencyModel) {
        LogEventsHelper.addEventOnStartWorkFlow(
            competencyData.cId.toString(),
            grade,
            subject,
            currentStudentCount,
            prefs.assessmentType,
            context,
            READ_ONLY_COMPETENCY_SCREEN,
            NL_APP_READ_ONLY_COMPETENCY
        )
        currentCompetencyIndex++
        val modifiedConfig = gson.toJson(workflowConfig, WorkflowConfig::class.java)
        workflowManager.loadConfig(modifiedConfig)
        workflowManager.addProperty(WorkflowProperty.CURRENT_STUDENT_COUNT, currentStudentCount)
        workflowManager.addProperty(
            WorkflowProperty.SELECTED_COMPETENCY,
            competencyData.learningOutcome
        )
        workflowManager.addProperty(WorkflowProperty.COMPETENCY_ID, competencyData.cId.toString())
        workflowManager.addProperty(WorkflowProperty.SCHOOL_DATA, schoolsData)
        workflowManager.addProperty(WorkflowProperty.ASSESSMENT_TYPE, prefs.assessmentType)
        workflowManager.addProperty(
            WorkflowProperty.CHAPTER_MAPPING_LIST,
            filteredChapterMappingList
        )
        val subject = MetaDataExtensions.getSubjectFromId(
            competencyData.subjectId,
            prefs.subjectsListJson
        )
        Timber.e("workflow start with subject $subject")
        totalWorkflowSessions++
        workflowManager.startWorkflow(activity, gradeValue, subject, resultListener)
        hideProgressBar()
    }

    private fun setCompetencyAdapter() {
        filteredCompetencyList.clear()
        if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER, true)
            || prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT
            || prefs.selectedUser.equals(
                AppConstants.USER_TEACHER,
                true
            ) || prefs.assessmentType == AppConstants.NIPUN_ABHYAS
        ) {
            competenciesDataList.forEachIndexed { _, competencyModel ->
                if (competencyModel.grade == gradeValue) {
                    getNipunCompetencies(competencyModel)
                }
            }
        } else {
            competenciesDataList.forEachIndexed { _, competencyModel ->
                val subject = MetaDataExtensions.getSubjectFromId(
                    competencyModel.subjectId,
                    prefs.subjectsListJson
                )
                Timber.e("CSF Subject name from Id $subject")
                if (competencyModel.grade == gradeValue && subject == subjectName) {
                    getNipunCompetencies(competencyModel)
                }
                if (competencyModel.grade == gradeValue && "Hindi-Math" == subjectName) {
                    getNipunCompetencies(competencyModel)
                }
            }
        }
        val adapter = ReadOnlyCompetencyAdapter(filteredCompetencyList)
        binding.rvReadonlyCompetency.adapter = adapter
        binding.rvReadonlyCompetency.layoutManager = LinearLayoutManager(activity)
    }

    private fun getNipunCompetencies(competencyModel: CompetencyModel) {
        if (competencyModel.learningOutcome.contains("Nipun Lakshya")) {
            filteredCompetencyList.add(competencyModel)
        }
    }

    private fun getSubjectName() {
        when (subject) {
            "Hindi", "hindi" -> {
                subjectName = "Hindi"
            }
            "Maths", "Math", "maths", "math" -> {
                subjectName = "Math"
            }
            "Hindi-Math" -> {
                subjectName = "Hindi-Math"
            }
        }
    }

    private fun getGradeAsInt() {
        gradeValue= getLastCharAsInt(grade)
        Timber.d("selected grade $gradeValue")
    }

    private fun getDataFromArgs() {
        if (arguments?.containsKey(AppConstants.INTENT_SCHOOL_DATA) == true) {
            schoolsData = arguments?.getSerializable(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
        selectedStudentCount = "1"
        grade = arguments?.getString(AppConstants.INTENT_SELECTED_GRADE) as String
        getGradeAsInt()
        subject = arguments?.getString(AppConstants.INTENT_SELECTED_SUBJECT) as String
        getSubjectName()
    }

    private fun setUpUINew() {
        binding.schoolInfo.root.visibility = View.GONE
    }

    private fun initWorkflowManager() {
        workflowManager = WorkflowManager.getInstance()
        val workflowConfigRemote = getConfigSettingsFromRemoteConfig()
        workflowConfig = gson.fromJson(workflowConfigRemote, WorkflowConfig::class.java)
    }

    private fun getConfigSettingsFromRemoteConfig(): String {
        return RemoteConfigUtils.getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.ASSESSMENT_WORKFLOW_CONFIG)
//        return AppConstants.flowConfig
    }

    companion object {
        fun newInstance(
            schoolsData: SchoolsData?,
            grade: String,
            subject: String
        ): ReadOnlyCompetencyFragment = ReadOnlyCompetencyFragment().withArgs {
            if (schoolsData != null) {
                putSerializable(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
            }
            putString(AppConstants.INTENT_SELECTED_GRADE, grade)
            putString(AppConstants.INTENT_SELECTED_SUBJECT, subject)
        }
    }
}

