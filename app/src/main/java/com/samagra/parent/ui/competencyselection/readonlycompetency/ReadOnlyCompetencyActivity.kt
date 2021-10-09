package com.samagra.parent.ui.competencyselection.readonlycompetency

import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.utils.addFragment
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivityReadOnlyCompetencyBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.competencyselection.CompetencySelectionRepository
import com.samagra.parent.ui.competencyselection.CompetencySelectionVM

class ReadOnlyCompetencyActivity :
    BaseActivity<ActivityReadOnlyCompetencyBinding, CompetencySelectionVM>() {

    private lateinit var prefs: CommonsPrefsHelperImpl
    private lateinit var grade: String
    private lateinit var subject: String
    private var schoolsData: SchoolsData? = null

    @LayoutRes
    override fun layoutRes() = R.layout.activity_read_only_competency

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
        prefs = CommonsPrefsHelperImpl(this, "")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        when (prefs.selectedUser) {
            AppConstants.USER_EXAMINER -> {
                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
            }
            AppConstants.USER_MENTOR -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_ABHYAS -> {
                        binding.includeToolbar.toolbar.setTitle(R.string.nipun_abhyas_text)
                    }
                    else->{
                        binding.includeToolbar.toolbar.setTitle(R.string.assessment)
                    }
                }
            }
            Constants.USER_DIET_MENTOR -> {
                binding.includeToolbar.toolbar.setTitle(R.string.assessment)
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

    private fun setListeners() {
        binding.includeToolbar.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(AppConstants.INTENT_SCHOOL_DATA)) {
            schoolsData =
                intent.getSerializableExtra(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
        grade = intent.getStringExtra(AppConstants.INTENT_SELECTED_GRADE) ?: ""
        subject = intent.getStringExtra(AppConstants.INTENT_SELECTED_SUBJECT) ?: ""
    }

    override fun loadFragment() {
        val fragment = ReadOnlyCompetencyFragment.newInstance(schoolsData, grade, subject)
        addFragment(
            binding.mobileFragmentContainer.id,
            supportFragmentManager,
            fragment,
            fragment.tag.toString(),
            false
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
