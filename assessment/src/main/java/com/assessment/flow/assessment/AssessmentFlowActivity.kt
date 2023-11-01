package com.assessment.flow.assessment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.assessment.R
import com.assessment.databinding.ActivityAssessmentFlowBinding
import com.assessment.flow.AssessmentConstants
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.flow.workflowengine.bolo.BoloConstants
import com.assessment.flow.workflowengine.bolo.ReadAlongProperties
import com.assessment.flow.workflowengine.bolo.instruction.ReadAlongInstructionActivity
import com.assessment.flow.workflowengine.bolo.readalong.ReadAlongDisclaimerActivity
import com.assessment.flow.workflowengine.odk.OdkInstructionActivity
import com.assessment.flow.workflowengine.odk.OdkProperties
import com.data.FlowType
import com.data.db.models.entity.School
import com.data.db.models.helper.AssessmentStateDetails
import com.data.models.stateresult.AssessmentStateResult
import com.data.models.stateresult.ModuleResult
import com.google.gson.Gson
import com.samagra.commons.CommonUtilities
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.utils.CommonConstants.BOLO
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.NetworkStateManager.Companion.instance
import com.samagra.commons.utils.isAppInstalled
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Date

@AndroidEntryPoint
class AssessmentFlowActivity : BaseActivity<ActivityAssessmentFlowBinding, AssessmentFlowVM>() {

    private var currentState: AssessmentStateDetails? = null
    private var odkProps: OdkProperties = OdkProperties()

    override fun layoutRes() = R.layout.activity_assessment_flow

    override fun getBaseViewModel(): AssessmentFlowVM {
        val viewModel: AssessmentFlowVM by viewModels()
        return viewModel
    }

    override fun getBindingVariable() = 0

    override fun onLoadData() {
        parseIntent()
        viewModel.initFlow()
        setObservers()
    }

    private fun parseIntent() {
        viewModel.grade = intent.getIntExtra(AssessmentConstants.KEY_GRADE, -1)
        viewModel.studentId = intent.getStringExtra(AssessmentConstants.KEY_STUDENT_ID) ?: ""
        if (intent.hasExtra(AssessmentConstants.KEY_SCHOOL_DATA)) {
            viewModel.schoolData =
                intent.getSerializableExtra(AssessmentConstants.KEY_SCHOOL_DATA) as School
        }
    }

    private fun setObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.eventsState.collect {
                when (it) {
                    is AssessmentFlowState.OnExit -> {
                        Timber.e(it.reason, "Assessment flow error.")
                        showToast(it.reason)
                        finish()
                    }

                    is AssessmentFlowState.Error -> {

                    }

                    is AssessmentFlowState.Loading -> {
                        if (!this@AssessmentFlowActivity.isFinishing) {
                            if (it.enabled) {
                                showProgressBar()
                            } else {
                                hideProgressBar()
                            }
                        }
                    }

                    is AssessmentFlowState.Next -> {
                        currentState = it.state
                        if (it.state.flowType == FlowType.BOLO) {
                            openReadAlongFlow(it.state)
                        } else {
                            openOdkFlow(it.state)
                        }
                    }

                    is AssessmentFlowState.OnResult -> {}
                    is AssessmentFlowState.Completed -> {
                        showToast(getString(R.string.assessment_completed))
                        viewModel.moveToResults(this@AssessmentFlowActivity)
                    }
                }
            }
        }

        viewModel.backgroundWorkCompleted.observe(this) { isCompleted ->
            if (isCompleted) {
                finish()
            }
        }
    }


    private val launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
            val data = result.data
            var assessmentStateResult =
                data?.getSerializableExtra(ASSESSMENT_RESULT) as AssessmentStateResult?
            val activity = data?.getStringExtra(ACTIVITY_FOR_RESULT)

            if (assessmentStateResult != null) {
                if (activity == ODK_INSTRUCTION_ACTIVITY) {
                    assessmentStateResult.grade = odkProps.grade
                    assessmentStateResult.workflowRefId = odkProps.formID
                    assessmentStateResult.subject = odkProps.subject
                } else if (activity == READ_ALONG_INSTRUCTION_ACTIVITY) {
                    if (viewModel.readAlongProps == null) {
                        return@registerForActivityResult
                    }
                    assessmentStateResult.moduleResult.stateGrade =
                        viewModel.readAlongProps?.stateGrade
                    assessmentStateResult.grade = viewModel.readAlongProps?.grade
                    assessmentStateResult.subject = viewModel.readAlongProps?.subject
                }
            } else {
                assessmentStateResult =
                    AssessmentStateResult()
                assessmentStateResult.studentId = viewModel.studentId
                if (activity == ODK_INSTRUCTION_ACTIVITY) {
                    assessmentStateResult.workflowRefId = odkProps.formID
                    assessmentStateResult.grade = odkProps.grade
                    assessmentStateResult.subject = odkProps.subject
                    val moduleResult = ModuleResult()
                    moduleResult.isNetworkActive = instance!!.networkConnectivityStatus
                    moduleResult.module = ODK
                    moduleResult.achievement = 0
                    moduleResult.isPassed = false // false case

                    moduleResult.sessionCompleted = false
                    moduleResult.appVersionCode = UtilityFunctions.getVersionCode()
                    moduleResult.totalQuestions = 0
                    moduleResult.successCriteria = 0
                    assessmentStateResult.moduleResult = moduleResult
                    assessmentStateResult.moduleResult.startTime = Date().time
                    assessmentStateResult.moduleResult.endTime = Date().time
                } else if (activity == READ_ALONG_INSTRUCTION_ACTIVITY) {
                    assessmentStateResult.workflowRefId = "0"
                    assessmentStateResult.grade = viewModel.readAlongProps?.grade
                    assessmentStateResult.subject = viewModel.readAlongProps?.subject
                    val moduleResult = ModuleResult()
                    moduleResult.module = BOLO
                    moduleResult.isNetworkActive = instance!!.networkConnectivityStatus
                    moduleResult.achievement = 0
                    moduleResult.isPassed = false
                    moduleResult.sessionCompleted = false
                    moduleResult.appVersionCode = UtilityFunctions.getVersionCode()
                    moduleResult.totalQuestions = 0
                    moduleResult.successCriteria = 0
                    assessmentStateResult.moduleResult = moduleResult
                    assessmentStateResult.moduleResult.startTime = Date().time
                    assessmentStateResult.moduleResult.endTime = Date().time

                }
            }
            val currentState = currentState ?: viewModel.cachedAssessmentStateDetails()
            ?: return@registerForActivityResult
            if (result.resultCode == RESULT_OK) {
                viewModel.markAssessmentComplete(
                    currentState.getAsAssessmentState(),
                    assessmentStateResult
                )
            } else if (result.resultCode == RESULT_CANCELED) {
                showToast((application as Context).getString(
                    R.string.assessment_cancelled
                ))
                finish()
            }

        } else {
            return@registerForActivityResult
        }
    }

    companion object {
        const val ODK_INSTRUCTION_ACTIVITY = "ODK_INSTRUCTION_ACTIVITY"
        const val READ_ALONG_INSTRUCTION_ACTIVITY = "READ_ALONG_INSTRUCTION_ACTIVITY"
        const val ODK_PROPS = "ODK_PROPS"
        const val READ_ALONG_PROPS = "READ_ALONG_PROPS"
        const val ACTIVITY_FOR_RESULT = "ACTIVITY_FOR_RESULT"
        const val ASSESSMENT_RESULT = "ASSESSMENT_RESULT"
    }

    private fun openOdkFlow(assessmentFlowState: AssessmentStateDetails) {
        val intent = Intent(this, OdkInstructionActivity::class.java)
        odkProps = OdkProperties(
            formID = CommonUtilities.selectRandomId(assessmentFlowState.refIds),
            showInstructions = true,
            grade = viewModel.grade,
            subject = assessmentFlowState.subjectName,
            studentCount = 1,
            competencyName = assessmentFlowState.learningOutcome,
            competencyId = assessmentFlowState.competencyId.toString(),
            subjectId = assessmentFlowState.subjectId
        )
        intent.putExtra(AppConstants.INTENT_ODK_PROPERTIES, odkProps)
        launcher.launch(intent)
    }

    private fun openReadAlongFlow(assessmentFlowState: AssessmentStateDetails) {
        viewModel.readAlongProps = ReadAlongProperties()
        viewModel.readAlongProps?.grade = viewModel.grade
        viewModel.readAlongProps?.competencyId = assessmentFlowState.competencyId.toString()
        viewModel.readAlongProps?.isCheckFluency = true
        viewModel.readAlongProps?.subject = assessmentFlowState.subjectName
        viewModel.readAlongProps?.bookIdList = assessmentFlowState.refIds

        if (!isAppInstalled(this.packageManager, BoloConstants.READ_ALONG_PACKAGE)) {
            val intent = Intent(this, ReadAlongDisclaimerActivity::class.java)
            intent.putExtra(AppConstants.INTENT_RA_PROPERTIES, viewModel.readAlongProps)
            launcher.launch(intent)
        } else if (viewModel.readAlongProps?.shouldShowInstructions() != false) {
            val intent = Intent(this, ReadAlongInstructionActivity::class.java)
            intent.putExtra(AppConstants.INTENT_RA_PROPERTIES, viewModel.readAlongProps)
            launcher.launch(intent)
        }
    }
}