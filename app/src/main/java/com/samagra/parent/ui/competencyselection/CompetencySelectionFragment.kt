/*
* this screen is used to select and assessed one competency at a time
* This screen represents suchi abhyas and Nipun shuchi flows of - Teacher, Mentor, Diet Mentor
*
* */

package com.samagra.parent.ui.competencyselection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.benasher44.uuid.uuid4
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.CommonUtilities
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseFragment
import com.samagra.commons.constants.Constants
import com.samagra.commons.getLastCharAsInt
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.models.submitresultsdata.ResultsVisitData
import com.samagra.commons.posthog.COMPETENCY_SELECTION_SCREEN
import com.samagra.commons.posthog.LogEventsHelper
import com.samagra.commons.posthog.NL_APP_COMPETENCY_SELECTION
import com.samagra.commons.posthog.NL_COMPETENCY_SELECTION
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.*
import com.samagra.parent.databinding.FragmentCompetencySelectionBinding
import com.samagra.parent.helper.RealmStoreHelper
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.SpinnerFieldWidget
import com.samagra.parent.ui.competencyselection.readonlycompetency.INDIVIDUAL_RESULT_REQUEST_CODE
import com.samagra.parent.ui.detailselection.ItemSelectionListener
import com.samagra.parent.ui.detailselection.setVisible
import com.samagra.parent.ui.finalresults.FinalResultsActivity
import com.samagra.parent.ui.setTextOnUI
import com.samagra.parent.ui.withArgs
import com.samagra.workflowengine.WorkflowProperty
import com.samagra.workflowengine.workflow.WorkflowManager
import com.samagra.workflowengine.workflow.WorkflowUtils
import com.samagra.workflowengine.workflow.model.WorkflowConfig
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import com.samagra.workflowengine.workflow.model.stateresult.StateResult
import kotlinx.coroutines.*
import org.odk.collect.android.utilities.SnackbarUtils
import timber.log.Timber
import java.util.*

class CompetencySelectionFragment :
    BaseFragment<FragmentCompetencySelectionBinding, CompetencySelectionVM>() {

    private val gson by lazy { Gson() }

    private var nextStudent: Boolean = false
    private var totalTime: Long = 0
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var currentStudentCount: Int = 1
    private var studentCompletedAssessment: Int = 0
    private lateinit var workflowConfig: WorkflowConfig
    private lateinit var workflowManager: WorkflowManager
    private var selectedStudentCount: String = ""
    private var subjectName: String = ""
    private val filteredCompetencyList = mutableListOf<CompetencyModel>()
    private var gradeValue: Int = 0
    private lateinit var subject: String
    private lateinit var grade: String
    private lateinit var competenciesDataList: ArrayList<CompetencyModel>
    private var schoolsData: SchoolsData? = SchoolsData()
    private var selectedCompetency: CompetencyModel? = null
    private var finalResult: ArrayList<StudentsAssessmentData> = ArrayList()
    private var resultsToPostList: ArrayList<StudentsAssessmentData> = ArrayList()
    private var resultMap: LinkedHashMap<String, ArrayList<StudentsAssessmentData>>? = null
    private var studentIdentifier = ""
    private var sessionTimeMap: HashMap<String, Long>? = null

    @LayoutRes
    override fun layoutId() = R.layout.fragment_competency_selection

    private val resultListener: WorkflowManager.WorkflowResultListener =
        object : WorkflowManager.WorkflowResultListener {

            override fun onComplete(stateResults: List<StateResult>) {
                if (stateResults.isNotEmpty()) {
                    onWorkflowManagerComplete(stateResults)
                } else {
                    Timber.e("competencyFragment Results list is empty! unable to proceed workflow")
                    SnackbarUtils.showShortSnackbar(
                        binding.llView,
                        getString(R.string.error_generic_message)
                    )
                }
            }

            override fun onError(s: String) {
                SnackbarUtils.showShortSnackbar(binding.llView, s)
            }
        }

    private fun onWorkflowManagerComplete(stateResults: List<StateResult>) {
        val assessmentStateResult = stateResults[0] as AssessmentStateResult
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
        val studentsAssessmentData: StudentsAssessmentData = if (stateResults.size > 1) {
            val assessmentStateResultCombined =
                stateResults[1] as AssessmentStateResult
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
        resultsToPostList.add(studentsAssessmentData)
        resultMap?.put(studentIdentifier, resultsToPostList)
        if (prefs.selectedUser.equals(AppConstants.USER_PARENT, true).not()) {
            prefs.putString(CommonConstants.ASSESSMENT_RESULTS_TEMP, gson.toJson(resultMap))
        }
        studentCompletedAssessment++
        startFlowWithSingleCompetency(
            shouldStartNewFlow = false,
            shouldOpenFinalResults = false,
            assessmentStateResult = assessmentStateResult
        )
    }

    private fun startFlowWithSingleCompetency(
        shouldStartNewFlow: Boolean = true,
        shouldOpenFinalResults: Boolean = false,
        assessmentStateResult: AssessmentStateResult? = null
    ) {
        resultsToPostList = ArrayList()
        if (shouldStartNewFlow) {
            nextStudent = true
            createStudentUUID()
            currentStudentCount++
            showProgressBar()
            openWorkFlow()
        } else if (shouldOpenFinalResults) {
            handleInsertionAndFinalRedirections()
        } else {
            handleIndividualsRedirection(assessmentStateResult)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // new student assess
        if (resultCode == NIPUN_ABHYAS_INDIVIDUAL_RESULT_REQUEST_CODE) {
            val showFinalResults =
                data?.getBooleanExtra(AppConstants.SHOW_FINAL_RESULTS, false)
            showFinalResults?.let { isShowFinalResult ->
                // if show final results then not start the assessment for next student
                startFlowWithSingleCompetency(
                    shouldStartNewFlow = !isShowFinalResult,
                    shouldOpenFinalResults = isShowFinalResult
                )
            }
        }
/*

        startFlowWithSingleCompetency(true, false)
        //show final results
        startFlowWithSingleCompetency(false, true)
*/
    }

    private fun handleInsertionAndFinalRedirections() {
        val flattenAllResultsList = flattenAllResultsOfMap(resultMap)
        if (prefs.selectedUser != AppConstants.USER_PARENT) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                insertResultsOnDb(flattenAllResultsList)
                redirectToFinalResultsScreen()
            }
        } else {
            throw IllegalArgumentException("Some problem occurred with Parent Competency flow!")
        }
    }

    private suspend fun insertResultsOnDb(flattenAllResultsList: ArrayList<ResultsVisitData>) {
        prefs.removeKey(CommonConstants.ASSESSMENT_RESULTS_TEMP)
        coroutineScope {
            if (flattenAllResultsList.isNotEmpty()) {
                val async: Deferred<Boolean> = async(Dispatchers.IO) {
                    flattenAllResultsList[0].udise_code?.let {
                        RealmStoreHelper.updateSchoolsVisitStatus(
                            it.toLong()
                        )
                    }
                    RealmStoreHelper.insertFinalResultsList(flattenAllResultsList)
                }
                async.await()
            } else {
                Timber.e(
                    "Flatten results list is empty so not able to insert data in DB" + " and mark school status as visit!"
                )
            }
        }
    }

    private fun createStudentUUID() {
        if (nextStudent) {
            nextStudent = false
            studentIdentifier = uuid4().toString()
        }
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

    private fun flattenAllResultsOfMap(resultMap: LinkedHashMap<String, ArrayList<StudentsAssessmentData>>?): ArrayList<ResultsVisitData> {
        val flattenResultsList = ArrayList<ResultsVisitData>()
        val gson = Gson()
        var sessionTimeOfStudent: Long
        putSessionTimeOfOneStudentInMap()
        val flowUUID = CommonUtilities.createUUID()
        val submissionTimeStamp = Date().time
        resultMap?.forEach { (studentSessionID, allResultsList) ->
            for (moduleResultItem in allResultsList) {
                sessionTimeOfStudent = if (sessionTimeMap?.containsKey(studentSessionID) == true) {
                    sessionTimeMap?.get(studentSessionID) ?: 0
                } else {
                    0
                }
                val moduleResultList = ArrayList<StudentsAssessmentData>()
                moduleResultList.add(moduleResultItem)
                val finalResultJsonString = gson.toJson(moduleResultList)
                Timber.d("student results " + finalResultJsonString)
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
        return StudentsAssessmentData(
            type,
            assessmentStateResult1,
            assessmentStateResult2
        )
    }

    private fun redirectToFinalResultsScreen() {
        val intent = Intent(activity, FinalResultsActivity::class.java)
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, finalResult)
        intent.putExtra("tt", totalTime)
        intent.putExtra("studentCompleteAssessment", studentCompletedAssessment)
        activity?.startActivity(intent)
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
        val viewModelProviderFactory =
            ViewModelProviderFactory(
                appCompatActivity!!.application,
                repository,
                dataSyncRepo
            )
        return ViewModelProvider(
            activity!!,
            viewModelProviderFactory
        )[CompetencySelectionVM::class.java]
    }

    override fun getBindingVariable() = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = CommonsPrefsHelperImpl(activity as Context, "prefs")
        getDataFromArgs()
        setupUI()
        checkIfTemporaryResultsArePresent()
        resultMap = LinkedHashMap()
        sessionTimeMap = LinkedHashMap()
        setObservers()
        viewModel.getCompetenciesData(prefs)
//        setSpinnerWithStudentCount()
        setClickListeners()
    }

    /*
    * Temporary results will prevent data loss if between the one assessment of any student if app kills by OS.
    * */
    private fun checkIfTemporaryResultsArePresent() {
        val tempResults: String? = prefs.getString(CommonConstants.ASSESSMENT_RESULTS_TEMP, "")
        tempResults?.let {
            if (it != "") {
                LogEventsHelper.addTelemetryEventOnAppProcessKill(
                    grade,
                    subject,
                    prefs.assessmentType,
                    context,
                    COMPETENCY_SELECTION_SCREEN,
                    NL_APP_COMPETENCY_SELECTION
                )
                insertTempResultsInDb(it)
            }
        }
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

    private fun setObservers() {
        with(viewModel) {
            observe(competenciesList, ::handleCompetencyData)
        }
    }

    private fun handleCompetencyData(competencyList: ArrayList<CompetencyModel>?) {
        competencyList?.let {
            competenciesDataList = it
            initWorkflowManager()
            workflowConfig.flowConfigs =
                WorkflowUtils.getWorkflowConfigForCompetencies(competencyList, prefs)
            setCompetencyAdapter()
//            setSelectedGradeSubjectAdapter()
        }
    }

    private fun setClickListeners() {
        binding.mtlBtnStartFlow.setOnClickListener {
            startAssessmentFlow()
        }
    }

    private fun startAssessmentFlow() {
        selectedCompetency?.let {
            context?.let { ctx ->
                LogEventsHelper.setPostHogEventOnStartFlow(
                    context = ctx,
                    assessmentType = prefs.assessmentType,
                    competencyId = it.cId.toString(),
                    screen = COMPETENCY_SELECTION_SCREEN,
                    pId = NL_APP_COMPETENCY_SELECTION,
                    ePageId = NL_COMPETENCY_SELECTION
                )
            }
            proceedFlow()
        } ?: kotlin.run {
            SnackbarUtils.showShortSnackbar(
                binding.llView,
                getString(R.string.please_select_no_of_students_or_competency)
            )
        }
    }

    private fun proceedFlow() {
        showProgressBar()
        studentCompletedAssessment = 0
        nextStudent = true
        createStudentUUID()
        openWorkFlow()
    }

    private fun openWorkFlow() {
        LogEventsHelper.addEventOnStartWorkFlow(
            selectedCompetency?.cId.toString(),
            grade,
            subject,
            currentStudentCount,
            prefs.assessmentType,
            context,
            COMPETENCY_SELECTION_SCREEN,
            NL_APP_COMPETENCY_SELECTION
        )

        try {
            val modifiedConfig =
                Gson().toJson(workflowConfig, WorkflowConfig::class.java)
            workflowManager.loadConfig(modifiedConfig)
            workflowManager.addProperty(
                WorkflowProperty.CURRENT_STUDENT_COUNT,
                currentStudentCount
            )
            workflowManager.addProperty(
                WorkflowProperty.SELECTED_COMPETENCY,
                selectedCompetency?.learningOutcome
            )
            workflowManager.addProperty(
                WorkflowProperty.COMPETENCY_ID,
                selectedCompetency?.cId.toString()
            )
            workflowManager.addProperty(
                WorkflowProperty.SCHOOL_DATA,
                schoolsData
            )
            workflowManager.addProperty(
                WorkflowProperty.CHAPTER_MAPPING_LIST,
                RealmStoreHelper.getChapterMapping()
            )
            workflowManager.startWorkflow(
                activity,
                gradeValue,
                subject,
                resultListener
            )
            hideProgressBar()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setSpinnerWithStudentCount() {
        val list = ArrayList<String>()
        for (i in 1..5) {
            list.add(i.toString())
        }
        val spinner: SpinnerFieldWidget = setSpinnerLists(list)
//        setSpinnerListener(spinner)

    }

    private fun setSpinnerListener(spinner: SpinnerFieldWidget) {
        spinner.setSelectionCallback { item, _ ->
            selectedStudentCount = item
        }
    }


    private fun setCompetencyAdapter() {
        gradeValue = getLastCharAsInt(grade)
        Timber.d("selected grade $gradeValue")

        when (subject) {
            "Hindi", "hindi" -> {
                subjectName = "Hindi"
            }
            "math", "Maths", "Math", "maths" -> {
                subjectName = "Math"
            }
        }

        competenciesDataList.forEachIndexed { _, competencyModel ->
            val subject = MetaDataExtensions.getSubjectFromId(
                competencyModel.subjectId,
                prefs.subjectsListJson
            )
            if (competencyModel.grade == gradeValue && subject == subjectName && !competencyModel.learningOutcome.contains(
                    "Nipun Lakshya"
                )
            ) {
                filteredCompetencyList.add(competencyModel)
            }
        }
        val competencyAdapter = CompetencyAdapter(sortCompetencyList())
        binding.rvCompetency.adapter = competencyAdapter
        binding.rvCompetency.layoutManager = LinearLayoutManager(activity)

        competencyAdapter.setItemSelectionListener(object : ItemSelectionListener<CompetencyModel> {
            override fun onSelectionChange(pos: Int, item: CompetencyModel) {
                selectedCompetency = item
            }
        })
    }

    private fun sortCompetencyList(): List<CompetencyModel> {
        try {
            val sortedWeekDataList = filteredCompetencyList.sortedWith { weekData1, weekData2 ->
                val numericValue1 =
                    weekData1.learningOutcome.substringBefore(":").trim().substringAfter(" ")
                        .toInt()
                val numericValue2 =
                    weekData2.learningOutcome.substringBefore(":").trim().substringAfter(" ")
                        .toInt()
                numericValue1.compareTo(numericValue2)
            }
            Timber.d("sorted competency list : ${gson.toJson(sortedWeekDataList)}")
            return sortedWeekDataList
        } catch (e: Exception) {
            Timber.e(e)
            return filteredCompetencyList
        }
    }

    private fun getDataFromArgs() {
        if (arguments?.containsKey(AppConstants.INTENT_SCHOOL_DATA) == true) {
            schoolsData = arguments?.getSerializable(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
        grade = arguments?.getString(AppConstants.INTENT_SELECTED_GRADE) as String
        subject = arguments?.getString(AppConstants.INTENT_SELECTED_SUBJECT) as String
    }

    private fun setupUI() {
        binding.rvSelectedItems.setVisible(false)
        binding.tvDetailsInfo.setVisible(false)
        binding.incTitles.tvVisit.visibility = View.GONE
        binding.incTitles.tvSchoolName.text = getString(R.string.competency)
        binding.llSpn.visibility = View.GONE
        binding.tvNoOfStudentTitle.visibility = View.GONE
        selectedStudentCount = "1"
        when (prefs.selectedUser) {
            AppConstants.USER_PARENT -> {
                setHeaderGradeInfo()
                showGradeAndSubject()
            }
            AppConstants.USER_TEACHER -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_SUCHI -> {
                        setHeaderUI()
                        setHeaderGradeInfo()
                        showGradeAndSubject()
                    }
                    AppConstants.SUCHI_ABHYAS -> {
                        setHeaderUI()
                        binding.schoolInfo.tvTime.visibility = View.GONE
                        showGradeAndSubject()
                    }
                }
            }
            AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                binding.schoolInfo.tvTime.visibility = View.GONE
                binding.schoolInfo.name.text =
                    String.format(
                        getString(R.string.school_name_top_banner),
                        schoolsData?.schoolName
                    )
                binding.schoolInfo.udise.text =
                    String.format(getString(R.string.udise_top_banner), schoolsData?.udise)
                showGradeAndSubject()
            }
        }
    }

    private fun setHeaderGradeInfo() {
        binding.schoolInfo.root.visibility = View.VISIBLE
        binding.schoolInfo.block.setVisible(false)
        binding.schoolInfo.tvTime.setVisible(false)
        binding.schoolInfo.name.setVisible(false)
        binding.schoolInfo.udise.setVisible(false)
    }

    private fun showGradeAndSubject() {
        binding.schoolInfo.address.visibility = View.VISIBLE
        binding.schoolInfo.address.text =
            "${getString(R.string.class_hindi)} ${getLastCharAsInt(grade)} - ${getHindiSubjectName()}"
    }

    private fun getHindiSubjectName(): String {
        return if (subject.equals("Hindi", true)) {
            getString(R.string.bhasha)
        } else {
            getString(R.string.math)
        }
    }

    private fun setHeaderUI() {
        binding.schoolInfo.name.setTextOnUI(
            setHeaderUiText(
                R.string.school_name_top_banner,
                prefs.mentorDetailsData?.schoolName ?: ""
            )
        )
        binding.schoolInfo.udise.setTextOnUI(
            setHeaderUiText(
                R.string.udise_top_banner,
                prefs.mentorDetailsData?.udise.toString()
            )
        )
    }

    private fun setSpinnerLists(
        list: ArrayList<String>
    ): SpinnerFieldWidget {
        val spinner = SpinnerFieldWidget(appCompatActivity as Context)
        binding.llSpn.addView(spinner)
        spinner.setListData(
            list.distinct().toTypedArray(),
            getString(R.string.students),
            false,
            0
        )
        return spinner
    }

    private fun initWorkflowManager() {
        workflowManager = WorkflowManager.getInstance()
        val workflowConfigRemote =
            getConfigSettingsFromRemoteConfig()
        workflowConfig = Gson().fromJson(workflowConfigRemote, WorkflowConfig::class.java)
    }

    private fun getConfigSettingsFromRemoteConfig(): String {
        return RemoteConfigUtils.getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.ASSESSMENT_WORKFLOW_CONFIG)
//        return AppConstants.flowConfig
    }

    private fun setHeaderUiText(resValue: Int, text: String): String {
        return String.format(getString(resValue), text)
    }

    private fun handleIndividualsRedirection(assessmentStateResult: AssessmentStateResult?) {
        val intent = Intent(context, SuchiAbhyasIndividualResultActivity::class.java)
        intent.putExtra(
            AppConstants.INTENT_SCHOOL_DATA,
            schoolsData
        )
        intent.putExtra(
            AppConstants.INTENT_COMPETENCY_NAME,
            selectedCompetency?.learningOutcome
        )
        intent.putExtra(AppConstants.INTENT_STUDENT_COUNT, currentStudentCount)
        intent.putExtra(AppConstants.ODK_START_TIME, assessmentStateResult?.moduleResult?.startTime)
        intent.putExtra(AppConstants.INTENT_ODK_RESULT, assessmentStateResult?.odkResultsData)
        intent.putExtra(AppConstants.INTENT_BOLO_RESULT, assessmentStateResult)
        intent.putExtra(AppConstants.INTENT_STUDENT_COUNT, finalResult.size)
        intent.putExtra(AppConstants.INTENT_SELECTED_SUBJECT, subject)
        intent.putExtra(AppConstants.INTENT_SELECTED_GRADE, assessmentStateResult?.grade)
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, finalResult)
        startActivityForResult(intent, INDIVIDUAL_RESULT_REQUEST_CODE)
    }

    companion object {
        fun newInstance(
            schoolsData: SchoolsData?,
            grade: String,
            subject: String
        ): CompetencySelectionFragment = CompetencySelectionFragment().withArgs {
            if (schoolsData != null) {
                putSerializable(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
            }
            putString(AppConstants.INTENT_SELECTED_GRADE, grade)
            putString(AppConstants.INTENT_SELECTED_SUBJECT, subject)
        }
    }
}