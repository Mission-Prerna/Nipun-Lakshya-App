package com.assessment.flow.workflowengine.bolo.instruction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.assessment.R
import com.assessment.databinding.ActivityReadAlongSetupBinding
import com.assessment.flow.assessment.AssessmentFlowActivity
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ACTIVITY_FOR_RESULT
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ASSESSMENT_RESULT
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.ResultsHelper
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.flow.workflowengine.bolo.BoloConstants
import com.assessment.flow.workflowengine.bolo.ReadAlongProperties
import com.data.models.stateresult.AssessmentStateResult
import com.data.models.stateresult.ModuleResult
import com.example.assets.uielements.CustomMessageDialog
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.BOLO_CANCELLED_BUTTON
import com.samagra.commons.posthog.BOLO_START_ASSESSMENT_BUTTON
import com.samagra.commons.posthog.EID_INTERACT
import com.samagra.commons.posthog.EVENT_RA_BOOK_NOT_FOUND
import com.samagra.commons.posthog.EVENT_RA_COMPETENCY_SELECTION
import com.samagra.commons.posthog.EVENT_RA_FAILED
import com.samagra.commons.posthog.EVENT_TYPE_USER_ACTION
import com.samagra.commons.posthog.LogEventsHelper
import com.samagra.commons.posthog.NL_APP_RA_INSTRUCTION
import com.samagra.commons.posthog.NL_SPOT_ASSESSMENT
import com.samagra.commons.posthog.OBJ_TYPE_UI_ELEMENT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.RA_INSTRUCTION_SCREEN
import com.samagra.commons.posthog.TYPE_CLICK
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.CommonConstants.BOLO
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.getNipunCriteria
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Date
import java.util.Random

@AndroidEntryPoint
open class ReadAlongInstructionActivity : AppCompatActivity() {

    private lateinit var bookId: String
    private lateinit var bookIdList: MutableList<String>
    private lateinit var props: ReadAlongProperties
    private lateinit var startTime: Date
    private val LAUNCH_GOOGLE_BOLO = 1
    private lateinit var binding: ActivityReadAlongSetupBinding
    private val viewModel: ReadAlongInstructionViewModel by viewModels()
    private val firebaseInstance by lazy { RemoteConfigUtils.getFirebaseRemoteConfigInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = Date()
        binding = ActivityReadAlongSetupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupToolbar()
        getDataFromIntent()
        viewModel.setPostHogEventSelectRACompetency(this, props, bookId)
        setObservers()

        viewModel.checkSubjectAndSetInstructionText(props)
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            setBroadCastFailure()
        }
        binding.subjectInstructionStudentGoToSubject.setOnClickListener {
            onGoToSubjectClicked()
        }
    }
    private fun setObservers() {
        viewModel.subjectInstructionText.observe(this, Observer {
            binding.subjectInstructionText.text = it
        })
    }

    private fun getDataFromIntent() {
        props =
            intent.getSerializableExtra(AppConstants.INTENT_RA_PROPERTIES) as ReadAlongProperties
        bookIdList = props.bookIdList
        getRandomBookId()
    }

    override fun onBackPressed() {
        setBroadCastFailure()
    }

    private fun setBroadCastFailure() {
        sendReadAlongDataToCallingActivity(ResultsHelper.createDummyResults(startTime, bookId))
    }

    private fun setPostHogEventBookNotFound() {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("module", BOLO))
        cDataList.add(Cdata("competencyId", props.competencyId))
        cDataList.add(Cdata("bookId", bookId))
        val properties = PostHogManager.createProperties(
            RA_INSTRUCTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_RA_INSTRUCTION, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_CLICK),
            null,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, EVENT_RA_BOOK_NOT_FOUND, properties)
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun moveToQuestions() {
        startTime = Date()
        val intent = Intent()
        intent.action = "com.google.android.apps.seekh.READBOOK"
        intent.putExtra("assessment_mode", true)
        intent.putExtra("intent_open_book_id", bookId)
        intent.putExtra(
            "book_partner_code",
            firebaseInstance.getString(RemoteConfigUtils.PARTNER_CODE)
        )
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        startActivityForResult(intent, LAUNCH_GOOGLE_BOLO)
    }

    private fun getRandomBookId() {
        val random = Random()
        val randomInt = random.nextInt(bookIdList.size)
        bookId = bookIdList[randomInt]
        bookIdList.removeAt(randomInt)
    }

    private fun onGoToSubjectClicked() {
        moveToQuestions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_GOOGLE_BOLO) {
            if (resultCode == RESULT_OK) {
                val correctWords = data?.getIntExtra("correct_words", 0) ?: 0
                val totalTime = data?.getLongExtra("total_time", 0) ?: 0
                Timber.d("Correct word and time bolo result : $correctWords, $totalTime")
                LogEventsHelper.addEventOnBoloResultCallback(
                    totalTime,
                    correctWords,
                    viewModel.checkFluencyIfNeed(props, totalTime, correctWords),
                    this,
                    RA_INSTRUCTION_SCREEN,
                    props.isCheckFluency
                )
                processResult(
                    viewModel.checkFluencyIfNeed(props, totalTime, correctWords)
                )
            } else if (resultCode == RESULT_CANCELED) {
                showMessageDialog(getString(R.string.assessment_canceled))
                setReadAlongCancelledEvent()
            } else if (resultCode == BoloConstants.BOOK_ID_NOT_FOUND) {
                setPostHogEventBookNotFound()
                if (this::bookIdList.isInitialized && bookIdList.isNotEmpty()) {
                    getRandomBookId()
                    moveToQuestions()
                } else {
                    showPromptAndExit()
                }
            } else {
                showPromptAndExit()
            }
        }
    }

    private fun showMessageDialog(msg: String) {
        val customDialog = CustomMessageDialog(
            this,
            ContextCompat.getDrawable(
                this,
                android.R.drawable.ic_dialog_info
            ),
            msg,
            null
        )
        customDialog.setOnFinishListener {
            setBroadCastFailure()
        }
        customDialog.show()
    }

    private fun showPromptAndExit() {
        showMessageDialog("कृपया निम्नलिखित की दोबारा जांच करें : \n\n* 'Read Along' ऐप की भाषा को अंग्रेजी से हिंदी में बदलें। ('Read Along' के पहले पेज पर बाईं ओर बटन दबाने पर यह विकल्प मिलेगा)\n* 'Read Along' ऐप पर आपने 'upprerna' पार्टनर कोड डालें। ('Read Along' के पहले पेज पर बाईं ओर बटन दबाने पर यह विकल्प मिलेगा)")
    }

    private fun sendReadAlongDataToCallingActivity(assessmentResult: AssessmentStateResult) {
        val resultIntent = Intent()
        resultIntent.putExtra(ASSESSMENT_RESULT, assessmentResult)
        resultIntent.putExtra(
            ACTIVITY_FOR_RESULT,
            AssessmentFlowActivity.READ_ALONG_INSTRUCTION_ACTIVITY
        )
        if (assessmentResult.moduleResult.sessionCompleted) {
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED, resultIntent)
        }
        finish()
    }

    private fun processResult(resultWordCount: Int) {
        val nipunCriteria =
            AppConstants.READ_ALONG_CRITERIA_KEY.getNipunCriteria(props.grade, props.subject)
        val assessmentResult = AssessmentStateResult()
        val module = ModuleResult(BOLO, nipunCriteria)
        module.achievement = resultWordCount
        module.isPassed = nipunCriteria <= resultWordCount
        module.isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus ?: false
        module.sessionCompleted = true
        module.appVersionCode = UtilityFunctions.getVersionCode()
        if (props.isCheckFluency) {
            module.statement =
                String.format(getString(R.string.can_read_words_in_mins), nipunCriteria)
        } else {
            module.statement =
                String.format(getString(R.string.can_read_no_of_words), nipunCriteria)
        }
        module.startTime = startTime.time
        assessmentResult.workflowRefId = bookId
        module.endTime = UtilityFunctions.getTimeMilis()
        assessmentResult.moduleResult = module
        sendReadAlongDataToCallingActivity(assessmentResult)
    }

    private fun setReadAlongCancelledEvent() {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("module", BOLO))
        cDataList.add(Cdata("competencyId", props.competencyId))
        cDataList.add(Cdata("bookId", bookId))
        cDataList.add(Cdata("grade", props.grade.toString()))
        if (props.schoolData != null) {
            cDataList.add(Cdata("block", props.schoolData.block))
            cDataList.add(Cdata("block_id", props.schoolData.blockId.toString()))
            cDataList.add(Cdata("district", props.schoolData.block))
            cDataList.add(Cdata("district_id", props.schoolData.districtId.toString()))
        }
        val properties = PostHogManager.createProperties(
            RA_INSTRUCTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_RA_INSTRUCTION, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_CLICK),
            Object.Builder().id(BOLO_CANCELLED_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, EVENT_RA_FAILED, properties)
    }
}