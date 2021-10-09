package com.samagra.workflowengine.bolo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.travel.BroadcastAction
import com.samagra.commons.travel.BroadcastActionSingleton
import com.samagra.commons.travel.BroadcastEvents
import com.samagra.commons.utils.CommonConstants.BOLO
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.getNipunCriteria
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.FragmentSubjectScoreBoardBinding
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import com.samagra.workflowengine.workflow.model.stateresult.ModuleResult

class ScoreActivity : AppCompatActivity() {
    private var grade: Int = 0
    private var subject: String? = ""
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var competencyName: String = ""
    private var schoolData: SchoolsData? = null
    private var studentCount: Int = 0
    private var resultWordCount: Int? = null
    private var requiredWordCount: Int? = null
    private lateinit var studentName: String
    private var mBinding: FragmentSubjectScoreBoardBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.fragment_subject_score_board)
        setupToolbar()
        initPreferences()
        getDataFromIntent()
//        Log.e("-->>", " startTime : $startTime  endTime : $endTime  totalTime : $totalTimeTaken ")
        setupUi()

        mBinding!!.incSelectedCompetency.tvText.text = competencyName
        studentName = "Student $studentCount"
        processData()

    }

    private fun setupUi() {
        when (prefs.assessmentType) {
            AppConstants.NIPUN_SUCHI -> {
                when (prefs.selectedUser) {
                    AppConstants.USER_PARENT -> {
                        handleHeaderAndRemarksGone()
                    }
                    AppConstants.USER_TEACHER -> {
                        handleHeaderAndRemarksGone()
                    }
                    AppConstants.USER_MENTOR -> {
                        mBinding!!.schoolInfo.root.visibility = View.VISIBLE
                        val totalTimeTaken: String =
                            UtilityFunctions.getTotalTimeTaken(startTime, endTime)
                        mBinding!!.schoolInfo.tvTime.text =
                            String.format(getString(R.string.total_time_taken), totalTimeTaken)
                        mBinding!!.schoolInfo.name.text =
                            String.format(
                                getString(R.string.school_name_top_banner),
                                schoolData?.schoolName
                            )
                        mBinding!!.schoolInfo.udise.text =
                            String.format(getString(R.string.udise_top_banner), schoolData?.udise)
                        mBinding!!.schoolInfo.tvTime.visibility = View.VISIBLE
                        mBinding!!.schoolInfo.address.visibility = View.GONE

                        mBinding!!.remarksTv.visibility = View.VISIBLE
                        mBinding!!.v.visibility = View.VISIBLE

                    }
                }
            }
        }
    }

    private fun handleHeaderAndRemarksGone() {
        mBinding!!.schoolInfo.root.visibility = View.GONE
        mBinding!!.remarksTv.visibility = View.INVISIBLE
        mBinding!!.v.visibility = View.INVISIBLE
    }

    private fun getDataFromIntent() {
        //        studentName = intent.getStringExtra("student_name") as String
        requiredWordCount = intent.getIntExtra("required_words_count", 0)
        resultWordCount = intent.getIntExtra("result_words_count", 0)
        studentCount = intent.getIntExtra(AppConstants.INTENT_STUDENT_COUNT, 0)
        if (intent.hasExtra(AppConstants.INTENT_SCHOOL_DATA)) {
            schoolData = intent.getSerializableExtra(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
        grade = intent.getIntExtra(AppConstants.INTENT_SELECTED_GRADE, 0)
        subject = intent.getStringExtra(AppConstants.INTENT_SELECTED_SUBJECT)
        competencyName = intent.getSerializableExtra(AppConstants.INTENT_COMPETENCY_NAME) as String
        startTime = intent.getLongExtra(AppConstants.BOLO_START_TIME, 0)
        endTime = UtilityFunctions.getTimeMilis()
    }

    private fun initPreferences() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        mBinding!!.includeToolbar.toolbar.setTitle(R.string.assessment_ended_toolbar)
        mBinding!!.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun processData() {
        mBinding!!.scoreView.setResultData(
            resultWordCount!!, requiredWordCount!!
        )
        if (competencyName.contains("Nipun Lakshya")) {
            mBinding!!.tvSuccess.visibility = View.VISIBLE
            mBinding!!.ivBanner.visibility = View.VISIBLE
            val nipunCriteria =
                AppConstants.READ_ALONG_CRITERIA_KEY.getNipunCriteria(grade, subject ?: "")
            if ((resultWordCount ?: 0) >= nipunCriteria) {
                mBinding!!.tvSuccess.text =
                    "बधाई हो, आपके बालक/बालिका ने कक्षा ${grade}  गणित/भाषा के निपुण लक्ष्य को हासिल कर लिए है |"
            } else {
                mBinding!!.tvSuccess.text =
                    "अभी इस विद्यार्थी को और मेहनत व निरंतर अभ्यास करने की ज़रुरत है"
            }
        } else {
            mBinding!!.tvSuccess.visibility = View.INVISIBLE
            mBinding!!.ivBanner.visibility = View.INVISIBLE

        }
        /*mBinding!!.scoreView.setData(
            studentName,
            requiredWordCount!!,
            resultWordCount!!,
            ReadAlongManager.getInstance().props.isCheckFluency
        )*/

        mBinding!!.remarksTv.text = getString(R.string.test_next_student);
        mBinding!!.remarksTv.setTextColor(ContextCompat.getColor(this, R.color.blue_2e3192))

//        result = resultWordCount!! >= requiredWordCount!!
        /*if (result) {
            mBinding!!.remarksTv.text =
                String.format(getString(R.string.well_done_lets_proceed), studentName)
            mBinding!!.remarksTv.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            mBinding!!.remarksTv.setTextColor(ContextCompat.getColor(this, R.color.color_FDCC32))
            mBinding!!.remarksTv.text =
                String.format(getString(R.string.lets_practice_more), studentName)
        }*/
        mBinding!!.btnProceed.setOnClickListener {
            processResult()
            finish()
        }
        mBinding!!.includeToolbar.toolbar.setNavigationOnClickListener {
            processResult()
            finish()
        }
    }

    private fun processResult() {
        val nipunCriteria =
            AppConstants.READ_ALONG_CRITERIA_KEY.getNipunCriteria(grade, subject ?: "")
        val assessmentResult = AssessmentStateResult()
        val module = ModuleResult(BOLO, nipunCriteria)
        module.achievement = resultWordCount
        module.isPassed = nipunCriteria <= (resultWordCount ?: 0)
        module.isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus ?: false
        module.sessionCompleted = true
        module.appVersionCode = UtilityFunctions.getVersionCode()
        //class is not used
        /*if (ReadAlongManager.getInstance().props.isCheckFluency) {
            module.statement =
                String.format(getString(R.string.can_read_words_in_mins), requiredWordCount)
        } else {
            module.statement =
                String.format(getString(R.string.can_read_no_of_words), requiredWordCount)
        }*/
        module.startTime = startTime
        module.endTime = endTime
        assessmentResult.moduleResult = module
        BroadcastActionSingleton.getInstance().liveAppAction.value = BroadcastAction(
            assessmentResult,
            BroadcastEvents.READ_ALONG_SUCCESS
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        processResult()
    }
}