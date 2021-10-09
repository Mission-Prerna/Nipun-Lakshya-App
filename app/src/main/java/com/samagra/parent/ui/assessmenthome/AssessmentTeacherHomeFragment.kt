package com.samagra.parent.ui.assessmenthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.chatbot.ChatBotActivity
import com.google.android.gms.location.*
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseFragment
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.Result
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.isChatBotEnabled
import com.samagra.grove.logging.Grove
import com.samagra.parent.*
import com.samagra.parent.R
import com.samagra.parent.authentication.AuthenticationActivity
import com.samagra.parent.databinding.FragmentAssessmentTeacherHomeBinding
import com.samagra.parent.ui.*
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import com.samagra.parent.ui.logout.LogoutUI
import org.odk.collect.android.utilities.ToastUtils
import timber.log.Timber
import java.util.*

class AssessmentTeacherHomeFragment :
    BaseFragment<FragmentAssessmentTeacherHomeBinding, AssessmentHomeVM>() {

    @LayoutRes
    override fun layoutId() = R.layout.fragment_assessment_teacher_home

    private var schoolsData: SchoolsData? = null
    private var dialogShowing: Boolean = false
    private var dialogBuilder: AlertDialog? = null
    private val prefs: CommonsPrefsHelperImpl by lazy { initPreferences() }

    override fun getBaseViewModel(): AssessmentHomeVM {
        val syncRepository = DataSyncRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(activity!!.application, syncRepository)
        return ViewModelProvider(
            activity!!,
            viewModelProviderFactory
        )[AssessmentHomeVM::class.java]
    }

    override fun getBindingVariable() = BR.assessmentHomeVm

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromArgument()
        setObservers()
        callApis(true)
        setupChatBotFlow()
        setupOverViewUI()
        setListeners()
    }

    private fun setupChatBotFlow() {
        binding.botFab.visibility = View.GONE
        var isChatBotVisibilityEnabled =
            isChatBotEnabled(prefs.mentorDetailsData.actorId)
        Timber.d("setupUI isChatbotEnabled: ${isChatBotVisibilityEnabled}")
        if (isChatBotVisibilityEnabled == true) {
            binding.botFab.visibility = View.VISIBLE
        }
        binding.botFab.setOnClickListener {
            openBot()
            logChatbotInitiate()
        }
    }

    private fun getDataFromArgument() {
        schoolsData = arguments?.getSerializable(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
    }

    private fun setBlockVisibility(visibility: Int) {
        binding.groupBlock.visibility = visibility
    }

    private fun callApis(enforce: Boolean) {
        viewModel.downloadDataFromRemoteConfig(prefs, UtilityFunctions.isInternetAvailable(context))
        viewModel.syncDataFromServer(prefs, enforce)
        viewModel.checkForFallback(prefs)
    }

    private fun setListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            callApis(enforce = true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (dialogShowing) {
            showSyncAlertDialog()
        }
        setSyncButtonUI()
    }

    private fun showSyncAlertDialog() {
        dialogBuilder?.let {
            if (it.isShowing) {
                return@let
            } else {
                it.show()
            }
        } ?: run {
            dialogBuilder =
                AlertDialog.Builder(context!!).setMessage(getString(R.string.data_sync_successful))
                    .setPositiveButton(getText(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                    }.show()
        }
    }

    private fun setSyncButtonUI() {
        binding.mtlBtnSetupAssessment.visibility = View.VISIBLE
        binding.mtlBtnSetupAssessment.minLines = 2
    }

    private fun setObservers() {
        with(viewModel) {
            observe(setupNewAssessmentClicked, ::handleSetupNewAssessment)
            observe(mentorDetailsSuccess, ::handleMentorDetails)
            observe(updateSync, ::handleSyncFlow)
            observe(mentorOverViewData, ::handleOverviewData)
            observe(failure, ::handleFailure)
            observe(showToastResWithId, ::handleMessage)
            observe(showSyncBeforeLogout, ::handleSyncBeforeLogout)
            observe(logoutUserLiveData, ::handleLogoutUser)
            observe(gotoLogin, ::handleLogoutRedirection)
            observe(progressBarVisibility, ::handleProgressBarVisibility)
        }
    }

    private fun handleLogoutRedirection(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        setRedirectionsOnIntent()
    }

    private fun handleLogoutUser(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        LogoutUI.confirmLogout(context) {
            viewModel.onLogoutUserData(prefs)
        }
    }

    private fun handleSyncBeforeLogout(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        confirmLogoutWithSync()
    }

    private fun confirmLogoutWithSync() {
        LogoutUI.confirmLogoutWithSync(context!!) {
            viewModel.syncDataToServer(prefs, {
                viewModel.onLogoutUserData(prefs)
            }) {
                ToastUtils.showShortToast(R.string.error_generic_message)
            }
        }
    }

    private fun handleSyncFlow(msg: Int?) {
        msg?.let {
            ToastUtils.showShortToast(it)
        }
        setSyncButtonUI()
    }

    override fun onPause() {
        super.onPause()
        dialogBuilder?.let {
            if (it.isShowing) {
                dialogShowing = true
                it.dismiss()
            }
        }
    }

    private fun handleOverviewData(overview: HomeOverviewData?) {
        setupOverViewUIWithData(overview)
    }

    private fun handleMessage(textResId: Int?) {
        textResId?.let {
            ToastUtils.showShortToast(it)
        }
    }

    private fun initPreferences() = CommonsPrefsHelperImpl(context, "prefs")

    private fun openBot() {
        context!!.startActivity(Intent(context, ChatBotActivity::class.java))
    }

    private fun logChatbotInitiate() {
        val properties = PostHogManager.createProperties(
            page = DASHBOARD_SCREEN,
            eventType = EVENT_TYPE_USER_ACTION,
            eid = EID_INTERACT,
            context = PostHogManager.createContext(
                id = APP_ID,
                pid = NL_APP_DASHBOARD,
                dataList = ArrayList()
            ),
            eData = Edata(NL_DASHBOARD, TYPE_CLICK),
            objectData = Object.Builder().id(BOT_INITIATION_BUTTON).type(OBJ_TYPE_UI_ELEMENT)
                .build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(
            context = context!!,
            eventName = EVENT_CHATBOT_INITIATE,
            properties = properties
        )
    }

    private fun setupOverViewUI() {
        binding.tvMonth.visibility = View.GONE
        binding.clProfileOverview.visibility = View.VISIBLE
        binding.clOverview.visibility = View.VISIBLE
        binding.titleMentorDetails.text = getString(R.string.teacher_profile)
        with(binding.includeAssessmentOverview) {
            cvBox3.visibility = View.GONE
            tvCount1.text = Constants.ZERO
            tvCount2.text = Constants.ZERO
            tvNameBox1.text = getString(R.string.abhyas_today)
            tvNameBox2.text = getString(R.string.abhyas_weekly)
            tvTitleNormal.visibility = View.VISIBLE
            tvTitleNormal.text = getString(R.string.no_of_abhyas_sessions)
            titleAssessmentsField.text = getString(R.string.teacher_overview_title)
        }
        with(binding.includeGradeWiseOverview) {
            cvBox3.visibility = View.GONE
            tvCount1.text = Constants.ZERO
            tvCount2.text = Constants.ZERO
            tvNameBox1.text = getString(R.string.abhyas_today)
            tvNameBox2.text = getString(R.string.abhyas_weekly)
            titleAssessmentsField.visibility = View.GONE
            tvTitleNormal.visibility = View.VISIBLE
            tvTitleNormal.text = getString(R.string.no_of_nipun_students)
        }
    }

    private fun setupOverViewUIWithData(overview: HomeOverviewData?) {
        with(binding.includeAssessmentOverview) {
            tvCount1.text = overview?.teacherOverviewData?.assessmentsToday.toString()
            tvCount2.text = overview?.teacherOverviewData?.assessmentTotal.toString()
        }
        with(binding.includeGradeWiseOverview) {
            tvCount1.text = overview?.teacherOverviewData?.nipunToday.toString()
            tvCount2.text = overview?.teacherOverviewData?.nipunTotal.toString()
        }
    }

    private fun handleSetupNewAssessment(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        setPostHogEventSetupAssessment()
        redirectToGradeSelectionScreen()
    }

    private fun setPostHogEventSetupAssessment() {
        val properties = PostHogManager.createProperties(
            page = DASHBOARD_SCREEN,
            eventType = EVENT_TYPE_USER_ACTION,
            eid = EID_INTERACT,
            context = PostHogManager.createContext(APP_ID, NL_APP_DASHBOARD, ArrayList()),
            eData = Edata(NL_DASHBOARD, TYPE_CLICK),
            objectData = Object.Builder().id(SETUP_ASSESSMENT_BUTTON).type(OBJ_TYPE_UI_ELEMENT)
                .build(),
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(activity!!, EVENT_SETUP_ASSESSMENT, properties)
    }

    private fun handleFailure(errorMessage: String?) {
        ToastUtils.showShortToast(errorMessage)
    }

    private fun handleMentorDetails(result: Result?) {
        val designation =
            MetaDataExtensions.getDesignationFromId(
                result?.designation_id ?: 0,
                prefs.designationsListJson
            )
        result?.let {
            Grove.e("user id mentors ${it.id}")
            if (designation.equals(Constants.USER_DESIGNATION_SRG, true)) {
                setBlockVisibility(View.GONE)
            } else {
                setBlockVisibility(View.VISIBLE)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CompositeDisposableHelper.destroyCompositeDisposable()
    }

    private fun setRedirectionsOnIntent() {
        val intentToUserSelection = Intent(context, AuthenticationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intentToUserSelection)
        activity!!.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressed()
    }

    private fun handleProgressBarVisibility(visible: Boolean?) {
        if (visible == true) {
            showProgressBar()
        } else {
            binding.swipeRefresh.isRefreshing = false
            hideProgressBar()
        }
    }

    private fun redirectToGradeSelectionScreen() {
        val intent = Intent(activity!!, DetailsSelectionActivity::class.java)
        if (schoolsData != null) {
            intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
        } else {
            Grove.e("Schools data is null and selected user is: ${prefs.selectedUser}")
        }
        startActivity(intent)
    }

    companion object {
        fun newInstance(schoolsData: SchoolsData?): AssessmentTeacherHomeFragment =
            AssessmentTeacherHomeFragment().withArgs {
                putSerializable(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
            }
    }
}