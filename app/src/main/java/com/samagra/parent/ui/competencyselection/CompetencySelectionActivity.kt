package com.samagra.parent.ui.competencyselection

import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.utils.addFragment
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivityCompetencySelectionBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.commons.models.schoolsresponsedata.SchoolsData

class CompetencySelectionActivity : BaseActivity<ActivityCompetencySelectionBinding, CompetencySelectionVM>() {

    private lateinit var prefs: CommonsPrefsHelperImpl
    private lateinit var grade: String
    private lateinit var subject: String
    private var schoolsData: SchoolsData? = null

    @LayoutRes
    override fun layoutRes() = R.layout.activity_competency_selection

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
        when(prefs.selectedUser){
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
            else->{
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
        val fragment = CompetencySelectionFragment.newInstance(schoolsData, grade, subject)
        addFragment(
            binding.mobileFragmentContainer.id,
            supportFragmentManager,
            fragment,
            fragment.tag.toString(), false
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val findFragmentById =
            supportFragmentManager.findFragmentById(binding.mobileFragmentContainer.id)
        findFragmentById?.onActivityResult(requestCode, resultCode, data)
    }
}