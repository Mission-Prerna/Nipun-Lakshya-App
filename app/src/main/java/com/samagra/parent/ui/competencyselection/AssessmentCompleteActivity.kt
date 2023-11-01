package com.samagra.parent.ui.competencyselection

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.ActivityAssessmentCompleteBinding
import com.samagra.parent.ui.competencyselection.readonlycompetency.INDIVIDUAL_RESULT_REQUEST_CODE
import com.samagra.parent.ui.competencyselection.readonlycompetency.StateLedResultModel
import com.samagra.parent.ui.finalresults.FinalResultsActivity
import com.samagra.parent.ui.finalresults.FinalResultsNlActivity
import com.samagra.parent.ui.newresultafterdietmentor.ResultNewAfterDietMentorActivity
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult

class AssessmentCompleteActivity : AppCompatActivity() {

    private lateinit var completeNipunMap: java.util.HashMap<String, Int>
    private var noOfStudent: Int = 0
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var studentsAssessmentCompleted: Int = 0
    private var totalTime: Long = 0
    private lateinit var resultsList: java.util.ArrayList<StudentsAssessmentData>
    private lateinit var resultMap: HashMap<CompetencyDatum, Int>
    private lateinit var stateLedResultList: java.util.ArrayList<StateLedResultModel>
    private lateinit var binding: ActivityAssessmentCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assessment_complete)
        initPrefs()
        getDataFromIntent()
        setupToolbar()
        //todo by neeraj
        setupUi()
        setListeners()
    }

    private fun initPrefs() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setPostHogEventSubmitResult() {
        val results = resultsList[resultsList.size - 1].studentResults
        val cDataList = ArrayList<Cdata>()
        if (results.currentStudentCount != null) {
            cDataList.add(Cdata("no_of_students_assessed", results.currentStudentCount.toString()))
        }
        if (studentsAssessmentCompleted != null) {
            cDataList.add(Cdata("students_completed_assessment", studentsAssessmentCompleted.toString()))
        }
        if (resultsList[resultsList.size - 1].studentResultsOdk != null) {
            cDataList.add(Cdata("module", "combined"))
        } else {
            if (resultsList[resultsList.size - 1].studentResults.moduleResult != null) {
                cDataList.add(Cdata("module", resultsList[resultsList.size - 1].studentResults.moduleResult.module))
            }
        }
        val properties = PostHogManager.createProperties(
            ASSESSMENT_COMPLETE_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_COMPLETE_ASSESSMENT, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_CLICK),
            Object.Builder().id(SUBMIT_RESULT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, EVENT_SUBMIT_RESULT, properties)
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        when (prefs.selectedUser) {
            AppConstants.USER_EXAMINER -> {
                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
            }
            Constants.USER_DIET_MENTOR -> {
                when (prefs.saveSelectStateLedAssessment) {
                    AppConstants.DIET_MENTOR_SPOT_ASSESSMENT -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.spot_assessment_)
                    }
                    AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.assessment)
                    }
                    else -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.spot_assessment_)
                    }
                }
            }
            else -> {
                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
            }
        }
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun setListeners() {
        binding.mtlBtnNext.setOnClickListener {
            gotoFinalResult()
        }
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.mtlBtn1.setOnClickListener {
            LogEventsHelper.addEventOnNextStudentSelection(
                prefs.assessmentType,
                this,
                ASSESSMENT_COMPLETE_SCREEN
            )
            redirectToFinalResultScreen(false)
        }
        binding.mtlBtnNext.setOnClickListener {
            redirectToFinalResultScreen(true)
        }
    }

    private fun redirectToFinalResultScreen(showResults: Boolean) {
        val intentWithData = Intent()
        intentWithData.putExtra(AppConstants.SHOW_FINAL_RESULTS, showResults)
        setResult(INDIVIDUAL_RESULT_REQUEST_CODE, intentWithData)
        finish()
    }

    private fun gotoFinalResult() {
        setPostHogEventSubmitResult()
        val intent: Intent = if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER, true)
            || (prefs.selectedUser.equals(
                Constants.USER_DIET_MENTOR,
                true
            ) && prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT)
        ) {
            Intent(this, ResultNewAfterDietMentorActivity::class.java)
        } else {
            if (prefs.assessmentType == AppConstants.NIPUN_LAKSHYA) {
                Intent(this, FinalResultsNlActivity::class.java)
            } else {
                Intent(this, FinalResultsActivity::class.java)
            }
        }
        intent.putExtra(AppConstants.INTENT_FINAL_RESULT_LIST, resultsList)
        if (::resultMap.isInitialized && resultMap != null) {
            intent.putExtra(AppConstants.INTENT_FINAL_RESULT_MAP, resultMap)
        }
        if (::stateLedResultList.isInitialized && stateLedResultList != null) {
            intent.putExtra(AppConstants.INTENT_FINAL_STATE_LED_RESULT_LIST, stateLedResultList)
        }
        if (::completeNipunMap.isInitialized && completeNipunMap != null) {
            intent.putExtra(AppConstants.INTENT_COMPLETE_NIPUN_MAP, completeNipunMap)
        }
        intent.putExtra(AppConstants.INTENT_STUDENT_COUNT, noOfStudent)
        intent.putExtra("tt", totalTime)
        startActivity(intent)
    }

    private fun setupUi() {
        val results = resultsList[resultsList.size - 1].studentResults
        when (prefs.selectedUser) {
            Constants.USER_DIET_MENTOR -> {
                if (prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT) {
                    setHeaderUi(results)
                    setRemarksOnMentor(results)
                    setTextTakeScreenShotUi(results)
                    setFooterUi()
                    handleStateLedVisibility()
                    binding.profileFooter.visibility=View.VISIBLE
                }
            }
            AppConstants.USER_EXAMINER -> {
                setHeaderUi(results)
                setRemarksOnMentor(results)
                setTextTakeScreenShotUi(results)
                setFooterUi()
                handleStateLedVisibility()
                binding.profileFooter.visibility=View.VISIBLE
            }
        }
    }

    private fun setFooterUi() {
        binding.mtlBtnNext.visibility = View.VISIBLE
    }

    private fun handleStateLedVisibility(){
        binding.schoolInfo.root.visibility = View.GONE
        binding.tvRemarks.visibility = View.VISIBLE
        binding.tvScreenshot.visibility = View.GONE
    }

    private fun setRemarksOnMentor(results: AssessmentStateResult) {
        binding.tvRemarks.visibility = View.VISIBLE
        if (results.currentStudentCount > 1) {
            binding.tvRemarks.text = String.format(getString(R.string.completed_assessment_with_students), results.currentStudentCount)
        } else {
            binding.tvRemarks.text = String.format(getString(R.string.completed_assessment_with_one_student), results.currentStudentCount)
        }
    }

    private fun setHeaderUiText(resValue: Int, text: String): String {
        return String.format(getString(resValue), text)
    }

    private fun setHeaderUi(results: AssessmentStateResult) {
        with(binding.schoolInfo) {
            root.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            udise.visibility = View.VISIBLE
            tvTime.visibility = View.GONE
            address.visibility = View.GONE
            when (prefs.selectedUser) {
                Constants.USER_DIET_MENTOR -> {
                    when (prefs.saveSelectStateLedAssessment) {
                        AppConstants.DIET_MENTOR_SPOT_ASSESSMENT -> {
                            name.text = setHeaderUiText(R.string.school_name_top_banner, results.schoolsData?.schoolName
                                ?: "")
                            udise.text = setHeaderUiText(R.string.udise_top_banner,
                                results.schoolsData?.udise.toString()
                            )
                        }
                        AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT -> {
                            setSchoolInfoForExaminerAndStateLedAssessment(results)
                        }
                    }
                }
                AppConstants.USER_EXAMINER -> {
                    setSchoolInfoForExaminerAndStateLedAssessment(results)
                }
                else -> {
                    name.text = setHeaderUiText(R.string.school_name_top_banner, results.schoolsData?.schoolName
                            ?: "")
                    udise.text = setHeaderUiText(R.string.udise_top_banner,
                        results.schoolsData?.udise.toString()
                    )
                }
            }
        }
    }

    private fun setSchoolInfoForExaminerAndStateLedAssessment(results: AssessmentStateResult) {
        binding.schoolInfo.block.visibility = View.VISIBLE
        binding.schoolInfo.tvTime.visibility = View.VISIBLE
        binding.schoolInfo.udise.visibility = View.GONE
        binding.schoolInfo.name.text = setHeaderUiText(R.string.district_name_top_banner, results.schoolsData?.district
                ?: "")
        binding.schoolInfo.tvTime.text = setHeaderUiText(R.string.udise_top_banner,
            results.schoolsData?.udise.toString()
        )
        binding.schoolInfo.block.text = setHeaderUiText(R.string.block_name_top_banner, results.schoolsData?.block
                ?: "")
    }

    private fun setTextTakeScreenShotUi(results: AssessmentStateResult) {
        binding.tvScreenshot.visibility = View.VISIBLE
        binding.tvScreenshot.text = String.format(getString(R.string.take_screenshot_of_next_screen_share_with_teacher), results.currentStudentCount)
    }

    private fun getDataFromIntent() {
        resultsList = intent.getSerializableExtra(AppConstants.INTENT_FINAL_RESULT_LIST) as ArrayList<StudentsAssessmentData>
        if (intent.hasExtra(AppConstants.INTENT_FINAL_RESULT_MAP)) {
            resultMap = intent.getSerializableExtra(AppConstants.INTENT_FINAL_RESULT_MAP) as HashMap<CompetencyDatum, Int>
        }
        if (intent.hasExtra(AppConstants.INTENT_FINAL_STATE_LED_RESULT_LIST)) {
            stateLedResultList = intent.getSerializableExtra(AppConstants.INTENT_FINAL_STATE_LED_RESULT_LIST) as java.util.ArrayList<StateLedResultModel>
        }
        if (intent.hasExtra(AppConstants.INTENT_COMPLETE_NIPUN_MAP)) {
            completeNipunMap = intent.getSerializableExtra(AppConstants.INTENT_COMPLETE_NIPUN_MAP) as HashMap<String, Int>
        }
        if (intent.hasExtra(AppConstants.INTENT_STUDENT_COUNT)) {
            noOfStudent = intent.getIntExtra(AppConstants.INTENT_STUDENT_COUNT, 0)
        }
        totalTime = intent.getLongExtra("tt", 0)
        studentsAssessmentCompleted = intent.getIntExtra("studentCompleteAssessment", 0)
    }

    override fun onBackPressed() {
        if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER,true)
            || prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT
        ) {
            redirectToFinalResultScreen(true)
        } else {
            super.onBackPressed()
            gotoFinalResult()
        }
    }
}
