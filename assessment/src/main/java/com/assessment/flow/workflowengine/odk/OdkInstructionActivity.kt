package com.assessment.flow.workflowengine.odk

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.assessment.R
import com.assessment.databinding.ActivityOdkSetupBinding
import com.assessment.flow.assessment.AssessmentFlowActivity
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ACTIVITY_FOR_RESULT
import com.assessment.flow.assessment.AssessmentFlowActivity.Companion.ASSESSMENT_RESULT
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.flow.workflowengine.spinner.SpinnerFieldWidget
import com.data.models.stateresult.AssessmentStateResult
import com.samagra.commons.basemvvm.NonViewModelBaseActivity
import com.samagra.commons.models.OdkResultData
import com.samagra.commons.models.Results
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.samagra.odk.collect.extension.utilities.ODKProvider
import org.json.JSONObject
import org.odk.collect.android.events.FormEventBus
import org.odk.collect.android.events.FormStateEvent
import timber.log.Timber
import java.util.Date

class OdkInstructionActivity : NonViewModelBaseActivity<ActivityOdkSetupBinding>() {
    private lateinit var props: OdkProperties
    private val formIdsList: ArrayList<String> = ArrayList()
    private var endTime: Long = 0
    private lateinit var startTime: Date
    private var formId: String = ""
    private lateinit var viewModel: OdkInstructionViewModel
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    @LayoutRes
    override fun layoutRes() = R.layout.activity_odk_setup

    override fun getViewBinding(layoutInflater: LayoutInflater) =
        DataBindingUtil.setContentView<ActivityOdkSetupBinding>(this, R.layout.activity_odk_setup)

    override fun onLoadData() {
        viewModel = ViewModelProvider(this)[OdkInstructionViewModel::class.java]
        getDataFromIntent()
        setupUI()
        getFormIdList()
        startTime = Date()
        setupToolbar()
        setListeners()
        viewModel.setPostHogEventSelectOdkCompetency(this, props, formId)
        renderLayoutLoader()
        launchFormsOdkFlow()
    }

    private fun getFormIdList() {
        // TODO : use actual Ids
        val odkFormId = arrayListOf("g3m_npl_w14_1", "g1h_npl_w5_3") // for testing purpose, can be replaced with other form ids
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
            runOnUiThread { showProgressBar() }
            setFormEventListener()
            ODKProvider.getFormsInteractor().openForm(formId, this)
        } else {
            Toast.makeText(this, getString(R.string.try_again_later), Toast.LENGTH_SHORT).show()
            Timber.d("Odk Instruction screen, launch with form id $formId")
            Timber.i("OdkWorkflow: odk formID is empty on line 23")
        }
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

    private fun checkIfResultsSuccess(assessmentResult: AssessmentStateResult?) {
        if (assessmentResult != null) {
            sendOdkDataToCallingActivity(assessmentResult)
        } else {
            sendOdkDataToCallingActivity(viewModel.setFailureResult())
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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun setFormEventListener() {
        compositeDisposable.add(
            FormEventBus.getState().subscribe { event ->
                when (event) {
                    is FormStateEvent.OnFormOpened ->
                        if (event.formId == formId) {
                            runOnUiThread { hideProgressBar() }
                        }
                    is FormStateEvent.OnFormOpenFailed ->
                        if (event.formId == formId) {
                            runOnUiThread {
                                hideProgressBar()
                                Toast.makeText(baseContext, event.errorMessage, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    is FormStateEvent.OnFormDownloadFailed ->
                        if (event.formId == formId) {
                            runOnUiThread {
                                hideProgressBar()
                                Toast.makeText(baseContext, event.errorMessage ?: "Some error occurred", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    is FormStateEvent.OnFormSubmitted ->
                        if (event.formId == formId) {
                            val formResultJsonString = event.jsonData
                            try {
                                val formResult = JSONObject(formResultJsonString).getJSONObject("data")
                                val totalMarks = formResult.getString("total_marks")
                                val results = arrayListOf<Results>()
                                for (key in formResult.keys()) {
                                    if (key.endsWith("_ans")) {
                                        results.add(Results(key, formResult.getString(key)))
                                    }
                                }
                                val odkResultsModel = OdkResultData(results.size, totalMarks, results)
                                val assessmentResult = viewModel.processResult(odkResultsModel, props)
                                checkIfResultsSuccess(assessmentResult)
                            }
                            catch (e: Exception) {
                                e.printStackTrace()
                                runOnUiThread {
                                    hideProgressBar()
                                    Toast.makeText(baseContext, "Form Submission Failed!", Toast.LENGTH_LONG)
                                        .show()
                                }
                            }
                        }
                    is FormStateEvent.OnFormAbandoned ->
                        if (event.formId == formId) {
                            checkIfResultsSuccess(null)
                        }
                    else -> {}
                }
            }
        )
    }
}