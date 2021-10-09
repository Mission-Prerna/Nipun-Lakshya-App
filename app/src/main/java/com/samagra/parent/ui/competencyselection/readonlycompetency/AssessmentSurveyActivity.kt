package com.samagra.parent.ui.competencyselection.readonlycompetency

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.models.surveydata.AssessmentSurveyModel
import com.samagra.commons.models.surveydata.SurveyResultData
import com.samagra.commons.models.surveydata.SurveyResultsModel
import com.samagra.parent.AppConstants
import com.samagra.parent.AppConstants.INTENT_SCHOOL_DATA
import com.samagra.parent.R
import com.samagra.parent.ViewModelProviderFactory
import com.samagra.parent.databinding.ActivityAssessmentSurveyBinding
import com.samagra.parent.helper.RealmStoreHelper
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.competencyselection.CompetencySelectionRepository
import com.samagra.parent.ui.competencyselection.CompetencySelectionVM
import com.samagra.parent.ui.formlite.form.StudentDetailFormFragment
import com.samagra.parent.ui.formlite.form.SubmissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

//todo Add base activity
class AssessmentSurveyActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var selectionVM: CompetencySelectionVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_survey)
        val binding: ActivityAssessmentSurveyBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_assessment_survey)
        setProgress()
        selectionVM = ViewModelProvider(
            this,
            ViewModelProviderFactory(
                this.application,
                CompetencySelectionRepository(),
                DataSyncRepository()
            )
        ).get(
            CompetencySelectionVM::class.java
        )
        setSupportActionBar(binding.includeToolbar.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.title = "मूलांकन अभियान"
        setObservers()
        setListeners()
        addForm()
    }

    //todo remove progress
    private fun setProgress() {
        progressDialog = ProgressDialog(this)
        with(progressDialog) {
            this.setMessage(getString(com.samagra.commons.R.string.please_wait))
            this.setCancelable(false)
            this.isIndeterminate = true
        }
    }

    private fun addForm() {
        val frag = StudentDetailFormFragment(object : SubmissionListener {
            override fun onFormSubmitted(responses: MutableMap<String, Any>?) {
                val surveyValue = ArrayList<SurveyResultData>()
                responses?.forEach { entry ->
                    val survey = SurveyResultData(entry.key, entry.value as String)
                    surveyValue.add(survey)
                }
                insertSurveyDataIntoDb(surveyValue)
            }

            override fun onError() {

            }
        })
        supportFragmentManager.beginTransaction().add(R.id.container, frag).commit()
    }

    private fun setListeners() {
       /* val textButton = findViewById<TextView>(R.id.tv_new_scr)
        textButton.setOnClickListener {
            insertSurveyDataIntoDb()
        }*/


    }

    private fun setObservers() {
        selectionVM.onResultsPost.observe(this) {
            if (progressDialog.isShowing){
                progressDialog.dismiss()
            }
            backToCallingActivity()
            }
    }

    private fun insertSurveyDataIntoDb(surveyValue: ArrayList<SurveyResultData>) {
        progressDialog.show()
        val schoolData = intent.getSerializableExtra(INTENT_SCHOOL_DATA) as SchoolsData
        val gradeValue = intent.getIntExtra(AppConstants.INTENT_SELECTED_GRADE, 0)
        val subject = intent.getStringExtra(AppConstants.INTENT_SELECTED_SUBJECT)
        val prefs = CommonsPrefsHelperImpl(this, "prefs")
        val submissionTimeStamp = Date().time
        val assessmentSurveyModel = AssessmentSurveyModel(
            submissionTimeStamp,
            "" + prefs.mentorDetailsData?.id,
            gradeValue,
            subject,
            Gson().toJson(SurveyResultsModel(surveyValue)),
            schoolData.udise?:0L,
            AppConstants.USER_EXAMINER
        )
        CoroutineScope(Dispatchers.IO).launch {
            RealmStoreHelper.insertSurveyResults(assessmentSurveyModel)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                backToCallingActivity()
            }
        }
    }

    private fun backToCallingActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        finish()
        return super.onSupportNavigateUp()
    }

}
