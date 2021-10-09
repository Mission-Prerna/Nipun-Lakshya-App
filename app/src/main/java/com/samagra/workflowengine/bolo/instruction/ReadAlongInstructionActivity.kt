package com.samagra.workflowengine.bolo.instruction

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.assets.uielements.CustomMessageDialog
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.KeyConstants
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.travel.BroadcastAction
import com.samagra.commons.travel.BroadcastActionSingleton
import com.samagra.commons.travel.BroadcastEvents
import com.samagra.commons.utils.CommonConstants.BOLO
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.getNipunCriteria
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.ActivityReadAlongInstructionBinding
import com.samagra.parent.ui.competencyselection.readonlycompetency.ResultsHelper
import com.samagra.workflowengine.bolo.ReadAlongProperties
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import com.samagra.workflowengine.workflow.model.stateresult.ModuleResult
import timber.log.Timber
import java.util.*

open class ReadAlongInstructionActivity : AppCompatActivity() {

    private lateinit var bookId: String
    private lateinit var bookIdList: MutableList<String>
    private lateinit var props: ReadAlongProperties
    private val prefs by lazy { initPrefs() }
    private lateinit var startTime: Date
    private val LAUNCH_GOOGLE_BOLO = 1
    private lateinit var binding: ActivityReadAlongInstructionBinding

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = Date()
        binding = ActivityReadAlongInstructionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupToolbar()
        getDataFromIntent()
        setPostHogEventSelectRACompetency()
        if (props.subject == "Hindi") {
            binding.subjectInstructionText.text =
                "छात्रों के लिए अनुदेश:\n\n" + "अब आपको कुछ शब्द या कहानियां दिखाई जाएंगी\n" + "अपनी क्षमता के अनुसार उन्हें पढ़ने का प्रयास करें"
        } else {
            binding.subjectInstructionText.text =
                "छात्रों के लिए अनुदेश:\n\n" + "अब आपको गणित के कुछ प्रश्न दिखेंगे\n" + "अपनी क्षमता के अनुसार संख्याएं को हिंदी पढ़ने का प्रयास करें"
        }
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            backButtonPressed()
        }
        binding.subjectInstructionStudentGoToSubject.setOnClickListener {
            onGoToSubjectClicked()
        }
    }

    private fun getDataFromIntent() {
        props = intent.getSerializableExtra(AppConstants.INTENT_RA_PROPERTIES) as ReadAlongProperties
        bookIdList = props.bookIdList
        getRandomBookId()
    }

    private fun initPrefs() = CommonsPrefsHelperImpl(this, "prefs")

    override fun onBackPressed() {
        super.onBackPressed()
        backButtonPressed()
    }

    private fun backButtonPressed() {
        setBroadCastFailure()
    }

    private fun setBroadCastFailure() {
        BroadcastActionSingleton.getInstance().liveAppAction.value =
            BroadcastAction(ResultsHelper.createDummyResults(startTime, bookId), BroadcastEvents.READ_ALONG_FAILURE)
        finish()
    }

    private fun setPostHogEventSelectRACompetency() {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("module", BOLO))
        cDataList.add(Cdata("competencyId",props.competencyId))
        cDataList.add(Cdata("bookId",bookId))
        val properties = PostHogManager.createProperties(
            RA_INSTRUCTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_RA_INSTRUCTION, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_CLICK),
            Object.Builder().id(BOLO_START_ASSESSMENT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, EVENT_RA_COMPETENCY_SELECTION, properties)
//        Log.e(POST_HOG_LOG_TAG, "RA INS screen $properties $EVENT_RA_COMPETENCY_SELECTION")
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
            AppConstants.USER_PARENT -> {
                binding.includeToolbar.toolbar.setTitle(R.string.instruction)

            }
            AppConstants.USER_EXAMINER -> {
                binding.includeToolbar.toolbar.setTitle(R.string.instruction)

            }
            AppConstants.USER_MENTOR -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_ABHYAS -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
                    }
                    else -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.instruction)
                    }
                }

            }
        }
        binding.includeToolbar.toolbar.setTitle(R.string.instruction)
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
                Timber.d( "Correct word and time bolo result : $correctWords, $totalTime")
                LogEventsHelper.addEventOnBoloResultCallback(
                    totalTime,
                    correctWords,
                    checkFluencyIfNeed(totalTime, correctWords),
                    this,
                    RA_INSTRUCTION_SCREEN,
                    props.isCheckFluency
                )
                processResult(
                    checkFluencyIfNeed(totalTime, correctWords)
                )
            } else if (resultCode == RESULT_CANCELED) {
                showMessageDialog(getString(R.string.assessment_canceled))
            } else if (resultCode == KeyConstants.BOOK_ID_NOT_FOUND) {
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

    private fun checkFluencyIfNeed(totalTime: Long, correctWords: Int): Int {
        return if (props.isCheckFluency) {
            var totalTimeNew = totalTime
            if (totalTimeNew < 1000) totalTimeNew = 1000
            val l = totalTimeNew / 1000
            val wordsPerMinute = (correctWords * 60) / l
            wordsPerMinute.toInt()
        } else {
            correctWords
        }
    }

    private fun processResult(resultWordCount: Int) {
        // requiredWordCount is getting from the workflow config as successCriteria. for now use nipun criteria.
//        val requiredWordCount =
//            if (props != null) props.requiredWords else 0
        val nipunCriteria = AppConstants.READ_ALONG_CRITERIA_KEY.getNipunCriteria(props.grade, props.subject)
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
        BroadcastActionSingleton.getInstance().liveAppAction.value = BroadcastAction(
            assessmentResult,
            BroadcastEvents.READ_ALONG_SUCCESS
        )
        finish()
    }
}