package com.assessment.flow.workflowengine.odk

import android.app.Activity
import android.content.*
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.assessment.R
import com.assessment.databinding.ActivityOdkSetupBinding
import com.assessment.flow.assessment.AssessmentFlowActivity
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ACTIVITY_FOR_RESULT
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ASSESSMENT_RESULT
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.flow.workflowengine.spinner.SpinnerFieldWidget
import com.data.models.stateresult.AssessmentStateResult
import com.data.models.stateresult.ModuleResult
import com.samagra.commons.basemvvm.NonViewModelBaseActivity
import com.samagra.commons.getPercentage
import com.samagra.commons.models.OdkResultData
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.travel.OdkConstant
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.getNipunCriteria
import org.odk.collect.android.listeners.FormProcessListener
import org.odk.collect.android.utilities.ToastUtils
import timber.log.Timber
import java.util.*


private const val ODK_FORM_QUESTION_LENGTH = 10

class OdkInstructionActivity : NonViewModelBaseActivity<ActivityOdkSetupBinding>() {
    private lateinit var props: OdkProperties
    private val formIdsList: ArrayList<String> = ArrayList()
    private var endTime: Long = 0
    private lateinit var startTime: Date
    private var odkResult: OdkResultData? = null
    private var showResultScreen: Boolean = false
    private lateinit var odkResultBroadcast: ODKResultBroadCast
    private var formId: String = ""

    @LayoutRes
    override fun layoutRes() = R.layout.activity_odk_setup

    override fun getViewBinding(layoutInflater: LayoutInflater) =
        DataBindingUtil.setContentView<ActivityOdkSetupBinding>(this, R.layout.activity_odk_setup)

    override fun onLoadData() {
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
        // TODO : use actual Ids
        val odkFormId = arrayListOf("g3m_npl_w14_1", "g1h_npl_w5_3")
        formIdsList.addAll(odkFormId)
        val spinner: SpinnerFieldWidget = setSpinnerLists(formIdsList)
        setSpinnerListener(spinner)
    }

    private fun setSpinnerListener(spinner: SpinnerFieldWidget) {
        spinner.setSelectionCallback { item, _ ->
            formId = item
            Timber.e("ODK Instruction screen formId from spinner : $formId")
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
            checkIfResultsSuccess(null)
        }
    }

    private fun launchFormsOdkFlow() {
        if (binding.etTestIds.text.toString().isNotEmpty()) {
            formId = binding.etTestIds.text.toString()
        }
        if (formId.isNotBlank()) {
            AssessmentsFormCommunicator.getContract()
                .launchSpecificDataFormFromAssessment(
                    this,
                    formId,
                    ODK_FORM_QUESTION_LENGTH,
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
            ToastUtils.showShortToast(getString(R.string.try_again_later))
            Timber.d("Odk Instruction screen, launch with form id $formId")
            Timber.i("OdkWorkflow: odk formID is empty on line 23")
        }
    }

    private fun setFailureResult(): AssessmentStateResult {
        endTime = UtilityFunctions.getTimeMilis()
        val dummyResultObject =
            AssessmentStateResult()
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
        checkIfResultsSuccess(null)
    }

    private fun setupUI() {
        binding.subjectInstructionText.visibility = View.GONE
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
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

    inner class ODKResultBroadCast : BroadcastReceiver() {
        private var assessmentResult: AssessmentStateResult? = null

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra(OdkConstant.ODK_RESULT)) {
                odkResult = intent.getSerializableExtra(OdkConstant.ODK_RESULT) as OdkResultData
                assessmentResult = processResult(odkResult)
            }
            checkIfResultsSuccess(assessmentResult)
        }

    }

    private fun checkIfResultsSuccess(assessmentResult: AssessmentStateResult?) {
        if (assessmentResult != null) {
            sendOdkDataToCallingActivity(assessmentResult)
        } else {
            sendOdkDataToCallingActivity(setFailureResult())
        }
    }

    private fun sendOdkDataToCallingActivity(assessmentResult: AssessmentStateResult) {
        val resultIntent = Intent()
        resultIntent.putExtra(ASSESSMENT_RESULT, assessmentResult)
        resultIntent.putExtra(
            ACTIVITY_FOR_RESULT,
            AssessmentFlowActivity.ODK_INSTRUCTION_ACTIVITY
        )
        if (assessmentResult.moduleResult.sessionCompleted)
            setResult(Activity.RESULT_OK, resultIntent)
        else
            setResult(Activity.RESULT_CANCELED, resultIntent)
        finish()
    }

    private fun processResult(resultData: OdkResultData?): AssessmentStateResult {
        val assessmentResult =
            AssessmentStateResult()
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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this@OdkInstructionActivity)
            .unregisterReceiver(odkResultBroadcast)

    }
}