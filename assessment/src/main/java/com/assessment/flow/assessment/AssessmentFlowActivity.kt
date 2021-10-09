package com.assessment.flow.assessment

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.assessment.R
import com.assessment.databinding.ActivityAssessmentFlowBinding
import com.assessment.flow.AssessmentConstants
import com.assessment.flow.scoreboard.StudentScoreboardActivity
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.bolo.BoloConstants
import com.assessment.flow.workflowengine.bolo.ReadAlongProperties
import com.assessment.flow.workflowengine.bolo.instruction.ReadAlongInstructionActivity
import com.assessment.flow.workflowengine.bolo.readalong.ReadAlongDisclaimerActivity
import com.assessment.flow.workflowengine.odk.OdkInstructionActivity
import com.assessment.flow.workflowengine.odk.OdkProperties
import com.data.FlowType
import com.data.db.models.entity.School
import com.data.db.models.helper.AssessmentStateDetails
import com.google.gson.Gson
import com.samagra.commons.CommonUtilities
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.utils.isAppInstalled
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AssessmentFlowActivity : BaseActivity<ActivityAssessmentFlowBinding, AssessmentFlowVM>() {

    private var currentState: AssessmentStateDetails? = null
    private var odkProps: OdkProperties = OdkProperties()
    val gson: Gson by lazy { Gson() }

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
                        Timber.e(it.t.message, "Assessment flow error.")
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

                    is AssessmentFlowState.Completed -> {
                        showToast(getString(R.string.assessment_completed))
                        viewModel.moveToResults(this@AssessmentFlowActivity)
                    }

                    is AssessmentFlowState.OnLaunchScoreboard -> {
                        val i = Intent(this@AssessmentFlowActivity, StudentScoreboardActivity::class.java)
                        val json = gson.toJson(it.scorecards)
                        i.putExtra(StudentScoreboardActivity.SCORECARD_LIST, json)
                        if (it.schoolData!=null) {
                            i.putExtra(AssessmentConstants.KEY_SCHOOL_DATA, it.schoolData)
                        }
                        i.putExtra(AssessmentConstants.KEY_STUDENT_ID, it.studentId)
                        i.putExtra(AssessmentConstants.KEY_STUDENT_NAME, it.studentName)
                        i.putExtra(AssessmentConstants.KEY_GRADE, it.grade)
                        startActivity(i)
                        finish()
                    }
                }
            }
        }

    }


    private val launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.processAssessmentResult(result, odkProps, currentState, this)
    }

    companion object {
        const val ODK_INSTRUCTION_ACTIVITY = "ODK_INSTRUCTION_ACTIVITY"
        const val READ_ALONG_INSTRUCTION_ACTIVITY = "READ_ALONG_INSTRUCTION_ACTIVITY"
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