package com.samagra.parent.ui.competencyselection

import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.utils.addFragment
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.models.OdkResultData
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivitySuchiAbhyasIndividualResultBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.workflowengine.odk.ODKResultFragment
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult

const val NIPUN_ABHYAS_INDIVIDUAL_RESULT_REQUEST_CODE: Int = 22

class SuchiAbhyasIndividualResultActivity : BaseActivity<ActivitySuchiAbhyasIndividualResultBinding, CompetencySelectionVM>() {
    private lateinit var boloResults: AssessmentStateResult
    private var odkResults: OdkResultData? =null
    private var startTime: Long =0L
    private var studentCount: Int = 0
    private lateinit var competency: String
    private lateinit var prefs: CommonsPrefsHelperImpl
    private  var grade: Int = 0
    private lateinit var subject: String
    private var schoolsData: SchoolsData? = null

    @LayoutRes
    override fun layoutRes() = R.layout.activity_suchi_abhyas_individual_result

    override fun getBaseViewModel(): CompetencySelectionVM {
        val repository = CompetencySelectionRepository()
        val dataSyncRepo = DataSyncRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(this.application, repository, dataSyncRepo)
        return ViewModelProvider(
            this,
            viewModelProviderFactory
        )[CompetencySelectionVM::class.java]
    }

    override fun getBindingVariable() = BR.viewModel

    override fun onLoadData() {
        initPreferences()
        getDataFromIntent()
        setupToolbar()
        setListeners()
    }

    private fun initPreferences() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.assessment)
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
        }
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun setListeners() {
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            showSubmitResults()
        }
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(AppConstants.INTENT_SCHOOL_DATA)) {
            schoolsData =
                intent.getSerializableExtra(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
        grade = intent.getIntExtra(AppConstants.INTENT_SELECTED_GRADE,0)
        subject = intent.getStringExtra(AppConstants.INTENT_SELECTED_SUBJECT) ?: ""
        competency = intent.getStringExtra(AppConstants.INTENT_COMPETENCY_NAME) ?: ""
        studentCount = intent.getIntExtra(AppConstants.INTENT_STUDENT_COUNT, 0)
        startTime = intent.getLongExtra(AppConstants.ODK_START_TIME,0L)
        odkResults = intent.getSerializableExtra(AppConstants.INTENT_ODK_RESULT) as OdkResultData?
        boloResults = intent.getSerializableExtra(AppConstants.INTENT_BOLO_RESULT) as AssessmentStateResult
    }

    override fun loadFragment() {
        val fragment = ODKResultFragment.newInstance(
            schoolsData!!,
            studentCount,
            odkResults,
            competency,
            null,
            startTime,
            grade,
            subject
        )
        addFragment(
            binding.mobileFragmentContainer.id,
            supportFragmentManager,
            fragment,
            fragment.tag.toString(), false
        )
    }

    override fun onBackPressed() {
        showSubmitResults()
        super.onBackPressed()
    }

    fun showSubmitResults() {
        val intent = Intent()
        intent.putExtra(AppConstants.SHOW_FINAL_RESULTS, true)
        setResult(NIPUN_ABHYAS_INDIVIDUAL_RESULT_REQUEST_CODE, intent)
        finish()
    }

    fun proceedWithNextStudent() {
        val intent = Intent()
        intent.putExtra(AppConstants.SHOW_FINAL_RESULTS, false)
        setResult(NIPUN_ABHYAS_INDIVIDUAL_RESULT_REQUEST_CODE, intent)
        finish()
    }
}