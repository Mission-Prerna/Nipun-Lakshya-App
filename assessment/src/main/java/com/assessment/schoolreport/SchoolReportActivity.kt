package com.assessment.schoolreport

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessment.R
import com.assessment.databinding.ActivitySchoolReportBinding
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.schoolreport.data.AssessmentStatus
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.extensions.hide
import com.samagra.commons.extensions.setDebounceClickListener
import com.samagra.commons.extensions.show
import com.samagra.commons.utils.loadGif
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

private const val SCHOOL = 1
@AndroidEntryPoint
class SchoolReportActivity : BaseActivity<ActivitySchoolReportBinding, SchoolReportVM>() {

    override fun layoutRes() = R.layout.activity_school_report

    override fun getBaseViewModel(): SchoolReportVM {
        val viewModel: SchoolReportVM by viewModels()
        return viewModel
    }

    override fun getBindingVariable() = 0

    private val schoolUdise by lazy {
        intent.getLongExtra(AppConstants.INTENT_SCHOOL_UDISE, 0)
    }

    private val studentReportAdapter by lazy { SchoolStudentReportAdapter() }

    override fun onLoadData() {
        setupToolbar()
        if (!verifyDataFromIntent()) {
            return
        }
        setClickListeners()
        setupRecyclerView()
        setObservers()
        viewModel.getReportForSchool(schoolUdise)
    }

    private fun verifyDataFromIntent() = if (schoolUdise == 0L) {
        showToast(R.string.school_not_found)
        finish()
        false
    } else {
        true
    }

    private fun setObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.reportState.collect { state ->
                Timber.d("setObservers: $state")
                when (state) {
                    is SchoolReportState.Error -> {
                        //FIXME change to error dialog?
                        binding.progressBar.hide()
                        showToast("Error: ${state.exception.message}")
                        finish()
                    }

                    SchoolReportState.Loading -> {
                        binding.progressBar.show()
                    }

                    is SchoolReportState.SchoolReportSuccess -> {
                        binding.progressBar.hide()
                        bindReportData(state)
                    }
                }

            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        binding.toolbar.title.text = getString(R.string.school_report)
        binding.toolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setClickListeners() {
        binding.cta.setDebounceClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvSchoolReport.apply {
            layoutManager = LinearLayoutManager(this@SchoolReportActivity)
            adapter = studentReportAdapter
        }
    }

    private fun bindReportData(model: SchoolReportState.SchoolReportSuccess) {
        binding.clParent.show()
        binding.header.apply {
            if (model.headerInfo != null) {
                show()
                bind(model.headerInfo, SCHOOL)
            } else {
                hide()
            }
        }

        val bannerGif: Int
        val bannerImage: Int
        if (model.schoolStatus == AssessmentStatus.SUCCESSFUL) {
            bannerGif = R.drawable.ic_celebrating_bird
            bannerImage = R.drawable.successful_school_banner
        } else {
            bannerGif = R.drawable.ic_flying_bird
            bannerImage = R.drawable.unsuccessful_school_banner
        }
        loadGif(view = binding.gifIv, gifResource = bannerGif, placeHolder = bannerGif)
        binding.ivNipunBanner.setImageResource(bannerImage)

        studentReportAdapter.swapData(model.students)

        binding.tvUserCount.text = model.bottomText
    }

    companion object {
        fun start(context: Context, schoolUdise: Long) {
            val intent = Intent(context, SchoolReportActivity::class.java)
            intent.putExtra(AppConstants.INTENT_SCHOOL_UDISE, schoolUdise)
            context.startActivity(intent)
        }
    }

}