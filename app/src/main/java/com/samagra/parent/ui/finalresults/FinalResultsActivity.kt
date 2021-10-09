package com.samagra.parent.ui.finalresults

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.loadGif
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.ActivityFinalResultsBinding
import com.samagra.parent.ui.assessmenthome.AssessmentHomeActivity
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import com.samagra.parent.ui.setTextOnUI
import timber.log.Timber

class FinalResultsActivity : AppCompatActivity() {
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var totalTime: Long = 0
    private var endTime: Long = 0
    private lateinit var resultsList: ArrayList<StudentsAssessmentData>
    private lateinit var binding: ActivityFinalResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_final_results)
        initPreferences()
        setupToolbar()
        getDataFromIntent()
        setupUi()
        setListeners()
        setPostHogEventSelectFinalResultTimeSpent()
    }

    private fun setPostHogEventSelectFinalResultTimeSpent() {
        val totalTimeTaken: String = UtilityFunctions.getTimeString(totalTime)
        try {
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
        } catch (e: Exception) {
            Timber.e("Event name : nl-finalscorecardscreen-timespent-view that " +
                    "helps getting total time is $totalTimeTaken and error is : $e")
        }
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        when (prefs.selectedUser) {
            AppConstants.USER_TEACHER -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_ABHYAS -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
                    }
                    AppConstants.SUCHI_ABHYAS -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.suchi_abhyas_text)
                    }
                }
            }
            else -> {
                binding.includeToolbar.toolbar.setTitle(R.string.spot_assessment_)
            }
        }
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun setListeners() {
        binding.mtlBtnNlSubmit.setOnClickListener {
            LogEventsHelper.setSubmitResultEvent(context = this)
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
        val intent: Intent = when (prefs.selectedUser) {
            AppConstants.USER_PARENT -> {
                Intent(this, DetailsSelectionActivity::class.java)
            }
            AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                Intent(this, AssessmentHomeActivity::class.java)
            }
            AppConstants.USER_TEACHER -> {
                Intent(this, AssessmentHomeActivity::class.java)
            }
            else -> {
                Intent(this, DetailsSelectionActivity::class.java)
            }
        }
        if (prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(
                AppConstants.INTENT_SCHOOL_DATA,
                resultsList[resultsList.size - 1].studentResults.schoolsData
            )
        } else if (prefs.selectedUser.equals(
                AppConstants.USER_PARENT,
                true
            ) || prefs.selectedUser.equals(
                AppConstants.USER_MENTOR,
                true
            ) || prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true)
        ) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun initPreferences() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setupUi() {
        for (i in resultsList.indices) {
            if (resultsList[i].viewType == ODK || resultsList[i].viewType == "combined") {
                binding.tvOnlyBoloRead.visibility = View.GONE
                break
            }
        }
        binding.incSelectedCompetency.tvText.text =
            resultsList[resultsList.size - 1].studentResults.competency

        when (prefs.selectedUser) {
            AppConstants.USER_PARENT -> {
                binding.schoolInfo.root.visibility = View.GONE
            }
            AppConstants.USER_TEACHER -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_LAKSHYA -> {
                        setHeaderUi()
                        setTime()
                    }
                    AppConstants.NIPUN_SUCHI -> {
                        binding.schoolInfo.root.visibility = View.GONE
                    }
                    //No need of Nipun abhyas
                    AppConstants.SUCHI_ABHYAS -> {
                        setHeaderUi()
                        setTime()
                    }
                }
            }
            AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_LAKSHYA -> {
                        setHeaderUi()
                        setTime()
                    }
                    AppConstants.NIPUN_SUCHI -> {
                        setHeaderUi()
                        setTime()
                    }
                    //No need of Nipun abhyas
                    AppConstants.SUCHI_ABHYAS -> {
                        setHeaderUi()
                        setTime()
                    }
                }
            }
        }
        setAdapter()
        binding.tvOnlyBoloRead.text = getString(R.string.words_read_correct)
        this.loadGif(binding.ivMascot, R.drawable.ic_flying_bird, R.drawable.ic_flying_bird)
    }

    private fun setGif(drawableResource: Int, placeHolder: Int) {
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
                            R.string.udise_top_banner, prefs.mentorDetailsData?.udise.toString()
                        )
                    )
                }
                else -> {
                    name.text = setHeaderUiText(
                        R.string.school_name_top_banner,
                        resultsList[resultsList.size - 1].studentResults.schoolsData.schoolName
                            ?: ""
                    )
                    udise.text = setHeaderUiText(
                        R.string.udise_top_banner,
                        resultsList[resultsList.size - 1].studentResults.schoolsData.udise.toString()
                    )
                }
            }
        }
    }

    private fun setAdapter() {
        val adapter = FinalResultsAdapter(resultsList)
        val layoutManager = LinearLayoutManager(this)
        binding.rvFinalResult.layoutManager = layoutManager
        binding.rvFinalResult.adapter = adapter
    }

    private fun getDataFromIntent() {
        resultsList =
            intent.getSerializableExtra(AppConstants.INTENT_FINAL_RESULT_LIST) as ArrayList<StudentsAssessmentData>
        totalTime = intent.getLongExtra("tt", 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        gotoHome()
    }
}


