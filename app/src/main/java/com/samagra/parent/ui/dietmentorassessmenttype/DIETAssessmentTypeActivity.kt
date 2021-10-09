/*
information:
* This class handles the SPOT assessment, State Led assessment and Nipun abhyas flow redirections
* Opens from the Mentor and Diet mentor flows.
* for mentor - Spot assessment and Nipun abhyas
* for Diet mentor - Spot assessment and State Led assessment
* */

package com.samagra.parent.ui.dietmentorassessmenttype

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivityDietassessmentTypeBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.assessmenthome.AssessmentHomeActivity
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import timber.log.Timber

class DIETAssessmentTypeActivity :
    BaseActivity<ActivityDietassessmentTypeBinding, DIETAssessmentTypeVM>() {
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var schoolsData: SchoolsData? = null

    @LayoutRes
    override fun layoutRes() = R.layout.activity_dietassessment_type

    override fun getBaseViewModel(): DIETAssessmentTypeVM {
        val repository = DataSyncRepository()
        val viewModelProviderFactory = ViewModelProviderFactory(this.application, repository)
        return ViewModelProvider(this, viewModelProviderFactory)[DIETAssessmentTypeVM::class.java]
    }

    override fun getBindingVariable() = BR.vm

    private val viewModels: DIETAssessmentTypeVM by viewModels()

    override fun onLoadData() {
        setupToolbar()
        initPreferences()
        getDataFromIntent()
        setupObserver()
        setupUI()
        setupListener()
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(AppConstants.INTENT_SCHOOL_DATA)) {
            schoolsData =
                intent.getSerializableExtra(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
    }

    private fun setupListener() {
        /*
        * Spot assessments
        * */
        binding.btnNipundietSpotAssessment.setOnClickListener {
            prefs.saveAssessmentType(AppConstants.NON_STATE_LED_ASSESSMENT)
            prefs.saveSelectStateLedAssessment(AppConstants.DIET_MENTOR_SPOT_ASSESSMENT)
            val intent = Intent(this, AssessmentHomeActivity::class.java)
            startActivity(intent)
        }
        /*
        * State led assessment and Nipun Abhyas
        * */
        binding.btnNipunstateLetAssessment.setOnClickListener {
            if (prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true)) {
                prefs.saveAssessmentType(Constants.STATE_LED_ASSESSMENT)
                prefs.saveSelectStateLedAssessment(AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT)
                val intent = Intent(this, AssessmentHomeActivity::class.java)
                startActivity(intent)
            } else {
                if (prefs.selectedUser.equals(AppConstants.USER_MENTOR,true)) {
                    prefs.saveAssessmentType(AppConstants.NIPUN_ABHYAS)
                    redirectToDetailsSelectionScreen()
                } else {
                    Timber.e("Issue found! please check the user selection or designation!")
                }
            }
        }
    }

    private fun redirectToDetailsSelectionScreen() {
        schoolsData?.let {
            val intentToDetails = Intent(this, DetailsSelectionActivity::class.java)
            intentToDetails.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
            startActivity(intentToDetails)
        }
    }

    private fun setupObserver() {
        with(viewModels) {
//            getInfoNoteFromRemoteConfig(Constant.INFO_NOTES_TYPE_SELECTION)
//            observe(remoteConfigString, ::handleInfoNoteText)
        }
    }

    private fun handleInfoNoteText(infoNote: String?) {
        infoNote?.let {
            binding.profileInfoNote.tvInfoNote.text = it
        }
    }

    // schools header setup only in Nipun Abhyas flow
    private fun setupUI() {
        if (prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true)) {
            binding.tvTitle.text = getString(R.string.dietassessmenttype)
            binding.tvBtn2.text = getString(R.string.diet_state_led_assessment)
            binding.profileInfoNote.tvInfoNote.text = getString(R.string.stateledAssessment_note)
            binding.schoolInfo.root.visibility = View.GONE
        } else {
            binding.tvTitle.text = getString(R.string.select_assessment_type_title)
            binding.tvBtn2.text = getString(R.string.nipun_abhyas_text)
            binding.profileInfoNote.tvInfoNote.text = getString(R.string.stateledAssessment_note)
            setupHeaderUi()
        }
    }

    private fun setupHeaderUi() {
        with(binding.schoolInfo) {
            root.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            udise.visibility = View.VISIBLE
            tvTime.visibility = View.GONE
            address.visibility = View.GONE
            when (prefs.selectedUser) {
                AppConstants.USER_MENTOR -> {
                    name.text =
                        setHeaderUiText(
                            R.string.school_name_top_banner,
                            schoolsData?.schoolName ?: ""
                        )
                    udise.text =
                        setHeaderUiText(R.string.udise_top_banner, schoolsData?.udise.toString())
                }
            }
        }
    }

    private fun setHeaderUiText(resValue: Int, text: String): String {
        return String.format(getString(resValue), text)
    }

    private fun initPreferences() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.nipun_lakshya_app)
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}