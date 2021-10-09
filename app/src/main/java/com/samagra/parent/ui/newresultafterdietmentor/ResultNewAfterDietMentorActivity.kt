/*
* Major work of this activity is to show student count and student nipun status
* This screen is used in Examiner, teacher and Mentor flows
* */

package com.samagra.parent.ui.newresultafterdietmentor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.constants.Constants
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.NipunHpEventHelper.subject
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.loadGif
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.ActivityResultNewAfterDietMentorBinding
import com.samagra.parent.ui.assessmenthome.AssessmentHomeActivity
import com.samagra.parent.ui.competencyselection.CompetencyDatum
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData
import com.samagra.parent.ui.competencyselection.readonlycompetency.StateLedResultModel
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import com.samagra.parent.ui.finalresults.CompetenciesAssessedAdapter
import com.samagra.parent.ui.setTextOnUI

class ResultNewAfterDietMentorActivity : AppCompatActivity() {
    private var totalTime: Long = 0
    private var noOfStudent: Int = 0
    private var nipunCount: Int = 0
    val prefs by lazy { initPreferences() }
    private lateinit var completeNipunMap: java.util.HashMap<String, Int>
    private lateinit var resultMap: java.util.HashMap<CompetencyDatum, Int>
    private lateinit var stateLedResultList: java.util.ArrayList<StateLedResultModel>
    private lateinit var resultsList: ArrayList<StudentsAssessmentData>
    private lateinit var binding: ActivityResultNewAfterDietMentorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_result_new_after_diet_mentor)
        setupToolbar()
        getDataFromIntent()
        setupUI()
        setResultsAdapter()
        setListeners()
        setPostHogEventSelectFinalResultTimeSpent()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        when (prefs.selectedUser) {
            Constants.USER_DIET_MENTOR, AppConstants.USER_EXAMINER -> {
                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
            }
            AppConstants.USER_MENTOR, AppConstants.USER_TEACHER -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_ABHYAS -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
                    }
                    AppConstants.SUCHI_ABHYAS -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.suchi_abhyas_text)
                        binding.schoolInfo.root.visibility = View.GONE
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
        binding.mtlBtnSetupAssessment.setOnClickListener {
            LogEventsHelper.setSubmitResultEvent(this, nipunStatus = nipunCount.toString())
            gotoHome()
        }
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            gotoHome()
        }
    }

    private fun redirectToGradeSelectionScreen() {
        val intent = Intent(this, DetailsSelectionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun gotoHome() {
        when (prefs.selectedUser) {
            AppConstants.USER_PARENT -> {
                redirectToGradeSelectionScreen()
            }
            AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR, AppConstants.USER_EXAMINER -> {
                val intentMentor = Intent(this, AssessmentHomeActivity::class.java)
                intentMentor.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intentMentor)
            }
            AppConstants.USER_TEACHER -> {
                val intentTeacher = Intent(this, AssessmentHomeActivity::class.java)
                intentTeacher.putExtra(
                    AppConstants.INTENT_SCHOOL_DATA,
                    resultsList[resultsList.size - 1].studentResults.schoolsData
                )
                intentTeacher.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intentTeacher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intentTeacher)
            }
            else -> {
                redirectToGradeSelectionScreen()
            }
        }
    }

    private fun initPreferences(): CommonsPrefsHelperImpl {
        return CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setupUI() {
        setBoloTextViewVisibility()
        setHeaderForDifferentRoles()
        setNipunText()
        setAssessedCompetenciesAdapter()
        setResultsAdapter()
        binding.mtlBtnSetupAssessment.text = getString(R.string.go_home)
    }

    private fun setHeaderForDifferentRoles() {
        when (prefs.selectedUser) {
            AppConstants.USER_PARENT -> {
                binding.schoolInfo.root.visibility = View.GONE
            }
            AppConstants.USER_TEACHER -> {
                setHeaderUi()
                setTime()
            }
            AppConstants.USER_EXAMINER -> {
                binding.tvTitle.visibility = View.GONE
                setHeaderUi()
            }
            AppConstants.USER_MENTOR -> {
                setHeaderUi()
                setTime()
            }
            Constants.USER_DIET_MENTOR -> {
                when (prefs.saveSelectStateLedAssessment) {
                    AppConstants.DIET_MENTOR_SPOT_ASSESSMENT -> {
                        setHeaderUi()
                        setTime()
                    }
                    AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT -> {
                        binding.tvTitle.visibility = View.GONE
                        setHeaderUi()
                    }
                }
            }
        }
    }

    private fun setBoloTextViewVisibility() {
        for (i in resultsList.indices) {
            if (resultsList[i].viewType == ODK || resultsList[i].viewType == "combined") {
                binding.tvOnlyBoloRead.visibility = View.GONE
                break
            }
        }
    }

    private fun setAssessedCompetenciesAdapter() {
        val assessedCompetenciesList = arrayListOf<CompetencyDatum>()
            val uniqueCompIdsMap = HashMap<String, Pair<String, String>>()
            for (item in resultsList.indices){
                with(resultsList[item].studentResults) {
                    uniqueCompIdsMap[competencyId] = Pair(competency, this.subject)
                }
            }
            for (items in uniqueCompIdsMap.keys){
                val competencyDatum = CompetencyDatum(
                    items.toInt(),
                    uniqueCompIdsMap[items]?.first?:"",
                    MetaDataExtensions.getSubjectId(uniqueCompIdsMap[items]?.second?:"", prefs.subjectsListJson)
                )
                assessedCompetenciesList.add(competencyDatum)
            }
        val adapter = CompetenciesAssessedAdapter(assessedCompetenciesList)
        binding.rvCompetenciesAssessed.adapter = adapter
        binding.rvCompetenciesAssessed.layoutManager = LinearLayoutManager(this)
    }

    private fun setNipunText() {
        this.loadGif(binding.ivMascot, R.drawable.ic_flying_bird, R.drawable.ic_flying_bird)
        if (this::completeNipunMap.isInitialized && completeNipunMap.isNotEmpty()) {
            completeNipunMap.forEach {
                if (it.value == resultMap.size) {
                    nipunCount++
                }
            }
        }
        if (nipunCount > 0) {
            binding.tvRemarks.text = "$nipunCount/$noOfStudent विद्यार्थी निपुण है"
            binding.tvRemarks.setTextColor(R.color.wrong_answer)
        } else {
            // no one is nipun
            binding.tvRemarks.text = "$nipunCount/$noOfStudent विद्यार्थी निपुण है"
            binding.tvRemarks.setTextColor(R.color.wrong_answer)
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
                AppConstants.USER_EXAMINER -> {
                    setSchoolInfoForExamierAndStateLedAssessment()
                }
                AppConstants.USER_MENTOR -> {
                    name.text = setHeaderUiText(
                        R.string.school_name_top_banner, schoolsData.schoolName
                            ?: ""
                    )
                    udise.text =
                        setHeaderUiText(R.string.udise_top_banner, schoolsData?.udise.toString())
                }
                Constants.USER_DIET_MENTOR -> {
                    when (prefs.saveSelectStateLedAssessment) {
                        AppConstants.DIET_MENTOR_SPOT_ASSESSMENT -> {
                            name.text = setHeaderUiText(
                                R.string.school_name_top_banner, schoolsData.schoolName
                                    ?: ""
                            )
                            udise.text = setHeaderUiText(
                                R.string.udise_top_banner,
                                schoolsData?.udise.toString()
                            )
                        }
                        AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT -> {
                            setSchoolInfoForExamierAndStateLedAssessment()
                        }
                    }

                }
                else -> {
                    name.text = setHeaderUiText(
                        R.string.school_name_top_banner, schoolsData.schoolName
                            ?: ""
                    )
                    udise.text =
                        setHeaderUiText(R.string.udise_top_banner, schoolsData?.udise.toString())
                }
            }
        }
    }

    private fun setSchoolInfoForExamierAndStateLedAssessment() {
        binding.schoolInfo.block.visibility = View.VISIBLE
        binding.schoolInfo.tvTime.visibility = View.VISIBLE
        binding.schoolInfo.udise.visibility = View.GONE
        binding.schoolInfo.name.text = setHeaderUiText(
            R.string.district_name_top_banner,
            resultsList[resultsList.size - 1].studentResults.schoolsData.district
                ?: ""
        )
        binding.schoolInfo.tvTime.text = setHeaderUiText(
            R.string.udise_top_banner,
            resultsList[resultsList.size - 1].studentResults.schoolsData.udise.toString()
        )
        binding.schoolInfo.block.text = setHeaderUiText(
            R.string.block_name_top_banner,
            resultsList[resultsList.size - 1].studentResults.schoolsData.block
                ?: ""
        )
    }

    private fun setResultsAdapter() {
        val adapter = ResultNewAfterDietMentorAdapter(stateLedResultList)
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
        if (intent.hasExtra(AppConstants.INTENT_FINAL_STATE_LED_RESULT_LIST)) {
            stateLedResultList =
                intent.getSerializableExtra(AppConstants.INTENT_FINAL_STATE_LED_RESULT_LIST) as java.util.ArrayList<StateLedResultModel>
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
    }

}