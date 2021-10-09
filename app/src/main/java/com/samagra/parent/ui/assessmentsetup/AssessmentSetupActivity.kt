package com.samagra.parent.ui.assessmentsetup

import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import com.data.repository.SchoolsRepository
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.utils.addFragment
import com.samagra.parent.BR
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.ViewModelProviderFactory
import com.samagra.parent.databinding.ActivityAssessmentSetupBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssessmentSetupActivity : BaseActivity<ActivityAssessmentSetupBinding, AssessmentSetupVM>() {

    @LayoutRes
    override fun layoutRes() = R.layout.activity_assessment_setup
    private lateinit var prefs: CommonsPrefsHelperImpl

    @Inject
    lateinit var schoolsRepository: SchoolsRepository
    override fun getBaseViewModel(): AssessmentSetupVM {
        val viewModelProviderFactory = ViewModelProviderFactory(this.application, schoolsRepository)
        return ViewModelProvider(this, viewModelProviderFactory)[AssessmentSetupVM::class.java]
    }

    override fun getBindingVariable() = BR.assessmentSetupVm

    override fun onLoadData() {
        setupToolbar()
        setListeners()
    }

    private fun setListeners() {
        binding.includeToolbar.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupToolbar() {
        prefs = CommonsPrefsHelperImpl(this, "")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.select_school)
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    override fun loadFragment() {
        val fragment = AssessmentSetupFragment.newInstance()
        addFragment(
            binding.container.id,
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