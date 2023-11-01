package com.samagra.parent.ui.finalresults

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.utils.loadGif
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.ActivityFinalResultsNlBinding
import com.samagra.parent.ui.assessmenthome.AssessmentHomeActivity
import com.samagra.parent.ui.competencyselection.CompetencyDatum
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import com.samagra.parent.ui.setTextOnUI

class FinalResultsNlActivity : AppCompatActivity() {
    private var nipunCount: Int = 0
    private lateinit var completeNipunMap: java.util.HashMap<String, Int>
    private var noOfStudent: Int = 0
    private lateinit var resultMap: java.util.HashMap<CompetencyDatum, Int>
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var totalTime: Long = 0
    private var endTime: Long = 0
    private lateinit var resultsList: ArrayList<StudentsAssessmentData>
    private lateinit var binding: ActivityFinalResultsNlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_final_results_nl)
        setupToolbar()
        initPreferences()
        getDataFromIntent()
        setupUi()
        setAdapter()
        setListeners()
        setPostHogEventSelectFinalResultTimeSpent()
    }

    private fun setPostHogEventSelectFinalResultTimeSpent() {
        val totalTimeTaken: String = UtilityFunctions.getTimeString(totalTime)
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("totalTimeSpent", totalTimeTaken))
        val properties = PostHogManager.createProperties(
            FINAL_SCORECARD_SCREEN,
            EVENT_TYPE_SCREEN_VIEW,
            EID_IMPRESSION,
            PostHogManager.createContext(APP_ID, NL_APP_FINAL_RESULT, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_VIEW),
            null,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, EVENT_FINAL_SCORE_CARD_VIEW, properties)
//        Log.e(POST_HOG_LOG_TAG, "Final results screen NL $properties $EVENT_FINAL_SCORE_CARD_VIEW")
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.spot_assessment_)
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun setListeners() {
        binding.mtlBtnNlSubmit.setOnClickListener {
            LogEventsHelper.setSubmitResultEvent(this, nipunStatus = nipunCount.toString())
            gotoHome()
        }
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            gotoHome()
        }
    }

    private fun redirectToGradeSelectionScreen() {
        val intent = Intent(this, DetailsSelectionActivity::class.java)
//        if (prefs.selectedUser== AppConstants.USER_PARENT)
        intent.putExtra(
            AppConstants.INTENT_SCHOOL_DATA,
            resultsList[resultsList.size - 1].studentResults.schoolsData
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun gotoHome() {
        val intent: Intent = if (prefs.selectedUser.equals(AppConstants.USER_PARENT, true)) {
            Intent(this, DetailsSelectionActivity::class.java)
        } else if (prefs.selectedUser.equals(AppConstants.USER_MENTOR, true)
            || prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true)
        ) {
            Intent(this, AssessmentHomeActivity::class.java)
        } else {
            Intent(this, DetailsSelectionActivity::class.java)
        }
        if (prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            /*intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            */
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(
                AppConstants.INTENT_SCHOOL_DATA,
                resultsList[resultsList.size - 1].studentResults.schoolsData
            )
        } else if (prefs.selectedUser.equals(AppConstants.USER_PARENT, true)
            || prefs.selectedUser.equals(AppConstants.USER_MENTOR)
            || prefs.selectedUser.equals(Constants.USER_DIET_MENTOR)
        ) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun initPreferences() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setupUi() {
        setNipunText()
        binding.tvTitleNipun.text = getString(R.string.final_ninun_lakshya_result_title)
        binding.tvTitleCompetency.text = getString(R.string.nipun_vidyarthi)
        when (prefs.assessmentType) {
            AppConstants.NIPUN_ABHYAS -> {
                binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
                when (prefs.selectedUser) {
                    AppConstants.USER_PARENT -> {
                        binding.schoolInfo.root.visibility = View.GONE
                    }
                    AppConstants.USER_TEACHER -> {
                        setHeaderUi()
                        setTime()
                    }
                    AppConstants.USER_MENTOR -> {
                        setHeaderUi()
                        setTime()
                        }
                }
            }
            AppConstants.NIPUN_LAKSHYA -> {
                when (prefs.selectedUser) {
                    AppConstants.USER_PARENT -> {
                        binding.schoolInfo.root.visibility = View.GONE
                    }
                    AppConstants.USER_TEACHER -> {
                        setHeaderUi()
                        setTime()
                    }
                    AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR-> {
                        setHeaderUi()
                        setTime()
                    }
                }
            }
            AppConstants.NIPUN_SUCHI -> {
                when (prefs.selectedUser) {
                    AppConstants.USER_PARENT -> {
                        binding.schoolInfo.root.visibility = View.GONE
                    }
                    AppConstants.USER_TEACHER -> {
                        binding.schoolInfo.root.visibility = View.GONE
                    }
                    AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                        setHeaderUi()
                        setTime()
                    }
                }
                setAdapter()
            }
        }
    }

    private fun setNipunText() {
        this.loadGif(
            view = binding.ivMascot,
            gifResource = R.drawable.ic_flying_bird,
            placeHolder = R.drawable.ic_flying_bird
        )
        if (!completeNipunMap.isNullOrEmpty()) {
            completeNipunMap.forEach {
                if (it.value == resultMap.size) {
                    nipunCount++
                }
            }
        }
        if (nipunCount > 0) {
            binding.tvRemarks.text =
                "$noOfStudent विद्यार्थियों में से $nipunCount विद्यार्थियों ने NIPUN Lakshya हासिल कर Liye है"
        } else {
            // no one is nipun
            binding.tvRemarks.text =
                "$noOfStudent विद्यार्थियों में से $nipunCount विद्यार्थियों ने NIPUN Lakshya हासिल कर Liye है"
            }
    }

    private fun setTime() {
        binding.schoolInfo.tvTime.visibility = View.VISIBLE
        val totalTimeTaken: String = UtilityFunctions.getTimeString(totalTime)
        binding.schoolInfo.tvTime.text =
            String.format(getString(R.string.total_time_taken), totalTimeTaken)
    }

    private fun setHeaderUiText(resValue: Int, text: String): String {
        return String.format(getString(resValue), text)
    }

    private fun setHeaderUi() {
        val schoolsData = resultsList[resultsList.size - 1].studentResults.schoolsData
        with(binding.schoolInfo) {
            root.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            udise.visibility = View.VISIBLE
            address.visibility = View.GONE
            when (prefs.selectedUser) {
                AppConstants.USER_TEACHER -> {
                    name.setTextOnUI(
                        setHeaderUiText(
                            R.string.school_name_top_banner,
                            prefs.mentorDetailsData?.schoolName ?: ""
                        )
                    )
                    udise.setTextOnUI(
                        setHeaderUiText(
                            R.string.udise_top_banner,
                            prefs.mentorDetailsData?.udise.toString()
                        )
                    )
                }
                else -> {
                    name.text =
                        setHeaderUiText(
                            R.string.school_name_top_banner,
                            schoolsData.schoolName
                                ?: ""
                        )
                    udise.text =
                        setHeaderUiText(
                            R.string.udise_top_banner,
                            schoolsData?.udise.toString()
                        )
                }
            }
        }
    }

    private fun setAdapter() {
        val toList: List<Pair<CompetencyDatum, Int>> = resultMap.toList()
        val adapter = FinalResultsNlAdapter(toList, noOfStudent)
        val layoutManager = LinearLayoutManager(this)
        binding.rvFinalResult.layoutManager = layoutManager
        binding.rvFinalResult.adapter = adapter
    }

    private fun getDataFromIntent() {
        resultsList =
            intent.getSerializableExtra(AppConstants.INTENT_FINAL_RESULT_LIST) as ArrayList<StudentsAssessmentData>
        totalTime = intent.getLongExtra("tt", 0)
        if (intent.hasExtra(AppConstants.INTENT_FINAL_RESULT_MAP)) {
            resultMap =
                intent.getSerializableExtra(AppConstants.INTENT_FINAL_RESULT_MAP) as HashMap<CompetencyDatum, Int>
        }
        if (intent.hasExtra(AppConstants.INTENT_COMPLETE_NIPUN_MAP)) {
            completeNipunMap =
                intent.getSerializableExtra(AppConstants.INTENT_COMPLETE_NIPUN_MAP) as HashMap<String, Int>
        }
        if (intent.hasExtra(AppConstants.INTENT_STUDENT_COUNT)) {
            noOfStudent = intent.getIntExtra(AppConstants.INTENT_STUDENT_COUNT, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        gotoHome()
    }
}