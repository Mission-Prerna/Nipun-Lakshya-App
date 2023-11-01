package com.assessment.flow.workflowengine.bolo.readalong

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.assessment.databinding.ActivityReadAlongDisclaimerBinding
import com.assessment.flow.assessment.AssessmentFlowActivity
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.ResultsHelper
import com.assessment.flow.workflowengine.bolo.ReadAlongProperties
import com.assessment.flow.workflowengine.bolo.instruction.ReadAlongInstructionActivity
import com.data.models.stateresult.AssessmentStateResult
import java.util.Date

private const val READ_ALONG_APP_URI_STRING = "market://details?id=com.google.android.apps.seekh"

class ReadAlongDisclaimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadAlongDisclaimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadAlongDisclaimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        binding.youtubeView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.youtube.com/watch?v=tHyPLZjVsLo")
            intent.putExtra("force_fullscreen", true)
            intent.putExtra("finish_on_ended", true)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!appInstalledOrNot(packageManager)) {
            binding.proceedToAssessmentPlaystore.setOnClickListener {
                val uri: Uri = Uri.parse(READ_ALONG_APP_URI_STRING)
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                goToMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
                try {
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.seekh")
                        )
                    )
                }
            }
        } else {
            val readAlongProperties =
                intent.getSerializableExtra(AppConstants.INTENT_RA_PROPERTIES) as ReadAlongProperties
            val intentToInstruction = Intent(this, ReadAlongInstructionActivity::class.java).apply {
                putExtra(AppConstants.INTENT_RA_PROPERTIES, readAlongProperties)
            }
            launcher.launch(intentToInstruction)
        }
    }

    private val launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
            val data = result.data
            var assessmentStateResult =
                data?.getSerializableExtra(AssessmentFlowActivity.ASSESSMENT_RESULT) as AssessmentStateResult
            val activity = data.getStringExtra(AssessmentFlowActivity.ACTIVITY_FOR_RESULT)

            val resultIntent = Intent()
            resultIntent.putExtra(AssessmentFlowActivity.ASSESSMENT_RESULT, assessmentStateResult)
            resultIntent.putExtra(
                AssessmentFlowActivity.ACTIVITY_FOR_RESULT,
                activity
            )
            if (assessmentStateResult.moduleResult.sessionCompleted) {
                setResult(Activity.RESULT_OK, resultIntent)
            } else {
                setResult(Activity.RESULT_CANCELED, resultIntent)
            }
            finish()
        } else {
            return@registerForActivityResult
        }
    }

    private fun appInstalledOrNot(
        pm: PackageManager,
        uri: String? = "com.google.android.apps.seekh"
    ): Boolean {
        return try {
            pm.getPackageInfo(uri!!, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        sendReadAlongDataToCallingActivity(ResultsHelper.createDummyResults(Date(), ""))
        return true
    }

    override fun onBackPressed() {
        sendReadAlongDataToCallingActivity(ResultsHelper.createDummyResults(Date(), ""))
    }

    private fun sendReadAlongDataToCallingActivity(assessmentResult: AssessmentStateResult) {
        val resultIntent = Intent()
        resultIntent.putExtra(AssessmentFlowActivity.ASSESSMENT_RESULT, assessmentResult)
        resultIntent.putExtra(
            AssessmentFlowActivity.ACTIVITY_FOR_RESULT,
            AssessmentFlowActivity.READ_ALONG_INSTRUCTION_ACTIVITY
        )
        setResult(Activity.RESULT_CANCELED, resultIntent)
        finish()
    }

}