package com.samagra.workflowengine.odk

import android.content.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.di.FormManagementCommunicator
import com.samagra.commons.basemvvm.NonViewModelBaseActivity
import com.samagra.commons.getPercentage
import com.samagra.commons.models.OdkResultData
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.travel.BroadcastAction
import com.samagra.commons.travel.BroadcastActionSingleton
import com.samagra.commons.travel.BroadcastEvents
import com.samagra.commons.travel.OdkConstant
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.getNipunCriteria
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivityOdkInstructionBinding
import com.samagra.parent.ui.SpinnerFieldWidget
import com.samagra.workflowengine.odk.OdkConstants.EMPTY_STRING
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import com.samagra.workflowengine.workflow.model.stateresult.ModuleResult
import org.odk.collect.android.listeners.FormProcessListener
import org.odk.collect.android.utilities.ToastUtils
import timber.log.Timber
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class OdkInstructionActivity : NonViewModelBaseActivity<ActivityOdkInstructionBinding>() {
    private lateinit var props: OdkProperties
    private val formIdsList: ArrayList<String> = ArrayList()
    private var endTime: Long = 0
    private lateinit var startTime: Date
    private var boloResult: AssessmentStateResult? = null
    private var odkResult: OdkResultData? = null
    private var showResultScreen: Boolean = false
    private lateinit var odkResultBroadcast: ODKResultBroadCast
    private var formId: String = ""
    private lateinit var prefs: CommonsPrefsHelperImpl

    @LayoutRes
    override fun layoutRes() = R.layout.activity_odk_instruction

    override fun getViewBinding(layoutInflater: LayoutInflater) =
        ActivityOdkInstructionBinding.inflate(layoutInflater)

    override fun onLoadData() {
        initPrefs()
        getDataFromIntent()
        setupUI()
        getFormIdList()
        initAndRegisterBroadcastForODKResult()
        startTime = Date()
        setupToolbar()
        setListeners()
        setPostHogEventSelectOdkCompetency()
        renderLayoutLoader()
        launchFormsOdkFlow()
    }

    private fun getFormIdList() {
        val odkFormId = UtilityFunctions.getOdkFormId()
        formIdsList.addAll(odkFormId)
        val spinner: SpinnerFieldWidget = setSpinnerLists(formIdsList)
        setSpinnerListener(spinner)
    }

    private fun setSpinnerListener(spinner: SpinnerFieldWidget) {
        spinner.setSelectionCallback { item, _ ->
            formId = item
            Timber.e( "ODK Instruction screen formId from spinner : $formId")
        }
    }

    private fun setSpinnerLists(list: ArrayList<String>): SpinnerFieldWidget {
        val spinner = SpinnerFieldWidget(this)
        binding.llSpn.addView(spinner)
        spinner.setListData(list.distinct().toTypedArray(), getString(R.string.students), false, 0)
        return spinner
    }

    private fun setPostHogEventSelectOdkCompetency() {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("module", ODK))
        cDataList.add(Cdata("competencyId", props.competencyId))
        cDataList.add(Cdata("formId", formId))
        val properties = PostHogManager.createProperties(
            ODK_INSTRUCTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_ODK_INSTRUCTION, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_CLICK),
            Object.Builder().id(ODK_START_ASSESSMENT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, EVENT_ODK_COMPETENCY_SELECTION, properties)
    }

    private fun setListeners() {
        binding.subjectInstructionStudentGoToSubject.setOnClickListener {
            launchFormsOdkFlow()
        }

        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            backPressHandling()
        }
    }

    private fun launchFormsOdkFlow() {
        if (binding.etTestIds.text.toString().isNotEmpty()) {
            formId = binding.etTestIds.text.toString()
        }

        if (!formId.equals(EMPTY_STRING, ignoreCase = true)) {
            FormManagementCommunicator.getContract()
                .launchSpecificDataFormFromAssessment(
                    this,
                    formId,
                    prefs.odkFormQuesLength,
                    props.subject,
                    object : FormProcessListener {
                        override fun onProcessingStart() {
                            runOnUiThread {
                                showProgressBar()
                            }
                        }

                        override fun onProcessed() {
                            runOnUiThread { hideProgressBar() }
                        }

                        override fun onCancelled(e: Exception) {
                            runOnUiThread {
                                hideProgressBar()
                                Toast.makeText(baseContext, e.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                )
        } else {
            ToastUtils.showShortToast("Try Again Later!")
            Timber.d( "Odk Instruction screen, launch with form id $formId")
            Timber.i("OdkWorkflow: odk formID is empty on line 23")
        }
    }

    private fun backPressHandling() {
        val fragment: ODKResultFragment? =
            supportFragmentManager.findFragmentByTag("odkResultFragment") as ODKResultFragment?
        if (fragment != null && fragment.isVisible) {
            fragment.onBackPressed()
        } else {
            BroadcastActionSingleton.getInstance().liveAppAction.value =
                BroadcastAction(setFailureResult(), BroadcastEvents.ODK_FAILURE)
            finish()
        }
    }

    private fun setFailureResult(): AssessmentStateResult {
        endTime = UtilityFunctions.getTimeMilis()
        val dummyResultObject = AssessmentStateResult()
        val moduleResult = ModuleResult()
        moduleResult.isNetworkActive =
            NetworkStateManager.instance?.networkConnectivityStatus ?: false
        moduleResult.module = ODK
        moduleResult.achievement = 0
        moduleResult.isPassed = false // false case
        moduleResult.totalQuestions = 0
        moduleResult.successCriteria = 0
        moduleResult.sessionCompleted = false
        moduleResult.appVersionCode = UtilityFunctions.getVersionCode()
        moduleResult.startTime = startTime.time
        moduleResult.endTime = endTime
        dummyResultObject.moduleResult = moduleResult
        return dummyResultObject
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backPressHandling()
    }

    private fun initPrefs() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setupUI() {
        binding.subjectInstructionText.visibility = View.GONE
    }

    private fun setupToolbar() {
        prefs = CommonsPrefsHelperImpl(this, "")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        when (prefs.selectedUser) {
            AppConstants.USER_EXAMINER -> {
                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
            }
            AppConstants.USER_MENTOR -> {
                when (prefs.saveSelectStateLedAssessment) {
                    AppConstants.DIET_MENTOR_SPOT_ASSESSMENT -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.assessment)
                    }
                    AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.assessment)
                    }
                    else -> {
                        when (prefs.assessmentType) {
                            AppConstants.NIPUN_ABHYAS -> {
                                binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
                            }
                            else -> {
                                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
                            }
                        }
                    }
                }
            }
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
                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
            }
        }

        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)

    }

    private fun initAndRegisterBroadcastForODKResult() {
        odkResultBroadcast = ODKResultBroadCast()
        IntentFilter().apply {
            addAction(OdkConstant.ACTION_ODK_RESULT)
            LocalBroadcastManager.getInstance(this@OdkInstructionActivity)
                .registerReceiver(odkResultBroadcast, this)
        }
    }

    private fun getDataFromIntent() {
        props =
            intent.getSerializableExtra(AppConstants.INTENT_ODK_PROPERTIES) as OdkProperties
        formId = props.formID
    }

    private fun renderLayoutLoader() {
        with(binding) {
            assessmentInstructionScroller.visibility = View.GONE
            assessmentProgressbarPredict.visibility = View.VISIBLE
            assessmentTextloader.visibility = View.VISIBLE
            assessmentTextloader1.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        if (showResultScreen && odkResult != null) {
            fragment()
        }
    }

    inner class ODKResultBroadCast : BroadcastReceiver() {
        private var assessmentResult: AssessmentStateResult? = null

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra(OdkConstant.ODK_RESULT)) {
                odkResult = intent.getSerializableExtra(OdkConstant.ODK_RESULT) as OdkResultData
                assessmentResult = processResult(odkResult)
            }
            checkIfResultsSuccess(assessmentResult)
            finish()
        }

        private fun checkIfResultsSuccess(assessmentResult: AssessmentStateResult?) {
            if (assessmentResult != null) {
                sendOdkBroadCastEvent(assessmentResult)
            } else {
                sendOdkBroadCastEvent(setFailureResult())
            }
        }
    }

    private fun processResult(resultData: OdkResultData?): AssessmentStateResult {
        val assessmentResult = AssessmentStateResult()
        val nipunCriteria =
            AppConstants.ODK_CRITERIA_KEY.getNipunCriteria(props.grade, props.subject)
        val module = ModuleResult(ODK, nipunCriteria)
        module.totalQuestions = resultData?.totalQuestions ?: 0
        module.achievement = resultData?.totalMarks?.toInt()
        val percentage = getPercentage(
            resultData?.totalMarks?.toInt() ?: 0,
            resultData?.totalQuestions ?: 0
        )
        module.isPassed = percentage >= nipunCriteria
        module.isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus ?: false
        assessmentResult.odkResultsData = resultData
        module.sessionCompleted = true
        module.appVersionCode = UtilityFunctions.getVersionCode()
        endTime = UtilityFunctions.getTimeMilis()
        module.startTime = startTime.time
        module.endTime = endTime
        module.statement = "ODK flow"
        assessmentResult.moduleResult = module
        return assessmentResult
    }

    private fun sendOdkBroadCastEvent(assessmentResult: AssessmentStateResult) {
        BroadcastActionSingleton.getInstance().liveAppAction.value = BroadcastAction(
            assessmentResult,
            if (assessmentResult.moduleResult.sessionCompleted) BroadcastEvents.ODK_SUCCESS else BroadcastEvents.ODK_FAILURE
        )
        finish()
    }

    private fun fragment() {
        val fragment = ODKResultFragment.newInstance(
            props.schoolData,
            props.studentCount,
            odkResult,
            props.competencyName,
            boloResult,
            startTime.time,
            props.grade,
            props.subject
        )
        supportFragmentManager.beginTransaction()
            .add(binding.mobileFragmentContainer.id, fragment, "odkResultFragment").commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this@OdkInstructionActivity)
            .unregisterReceiver(odkResultBroadcast)

    }
}