package com.samagra.parent.ui.assessmenthome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.chatbot.ChatBotActivity
import com.google.gson.JsonParser
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.CommonUtilities
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseFragment
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.Result
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.isChatBotEnabled
import com.samagra.parent.*
import com.samagra.parent.authentication.AuthenticationActivity
import com.samagra.parent.databinding.FragmentAssessmentUserHomeBinding
import com.samagra.parent.ui.*
import com.samagra.parent.ui.assessmentsetup.AssessmentSetupActivity
import com.samagra.parent.ui.logout.LogoutUI
import org.odk.collect.android.utilities.ToastUtils
import java.util.*

class AssessmentUserHomeFragment :
    BaseFragment<FragmentAssessmentUserHomeBinding, AssessmentHomeVM>() {

    @LayoutRes
    override fun layoutId() = R.layout.fragment_assessment_user_home

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
        setObservers()
        callApis(true)
        setupChatBotFlow()
        setupOverViewUI()
        setListeners()
    }


    private fun setupChatBotFlow() {
        val isChatBotEnabled = isChatBotEnabled(prefs.mentorDetailsData.actorId)

        if (isChatBotEnabled != null) {
            if (isChatBotEnabled==false) {
                binding.botFab.visibility = View.GONE
                return
            }
        }
        binding.botFab.visibility = View.VISIBLE
        binding.botFab.setOnClickListener {
            openBot()
            logChatBotInitiate()
        }
    }


    private fun getConfigSettingsFromRemoteConfigActor(): String {
        return RemoteConfigUtils.getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.CHATBOT_ICON_VISIBILITY_TO_ACTOR)
    }


    private fun openBot() {
        requireContext().startActivity(Intent(requireContext(), ChatBotActivity::class.java))
    }

    private fun logChatBotInitiate() {
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
            context = requireContext(),
            eventName = EVENT_CHATBOT_INITIATE,
            properties = properties
        )
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

    private fun setupOverViewUI() {
        binding.tvMonth.text =
            String.format(
                getString(R.string.oveview_month_basis),
                getString(UtilityFunctions.getCurrentMonth())
            )
        binding.clProfileOverview.visibility = View.VISIBLE
        binding.clOverview.visibility = View.VISIBLE
        if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER, true)) {
            binding.titleMentorDetails.text = getString(R.string.examiner_profile)
        } else if (prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true)) {
            binding.titleMentorDetails.text = getString(R.string.diet_mentor_profile)
        }
        with(binding.includeAssessmentOverview) {
            tvTitleNormal.visibility = View.GONE
            cvBox3.visibility = View.VISIBLE
            tvCount1.text = Constants.ZERO
            tvCount2.text = Constants.ZERO
            tvCount3.text = String.format("${Constants.ZERO} %s", getString(R.string.min_one))
            tvNameBox1.text = getString(R.string.schools_visited)
            tvNameBox2.text = getString(R.string.students_assessed)
            tvNameBox3.text = getString(R.string.average_time_per_assessment)
            titleAssessmentsField.text = getString(R.string.visits_overview)
        }
        with(binding.includeGradeWiseOverview) {
            tvTitleNormal.visibility = View.GONE
            cvBox3.visibility = View.VISIBLE
            tvCount1.text = Constants.ZERO
            tvCount2.text = Constants.ZERO
            tvCount3.text = Constants.ZERO
            tvNameBox1.text = getString(R.string.grade_1_students)
            tvNameBox2.text = getString(R.string.grade_2_students)
            tvNameBox3.text = getString(R.string.grade_3_students)
            titleAssessmentsField.text = getString(R.string.grade_wise_overview)
        }
    }

    private fun setupOverViewUIWithData(overview: HomeOverviewData?) {
        with(binding.includeAssessmentOverview) {
            if (prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
                this.titleAssessmentsField.text = getString(R.string.spot_assessment_overview)
            } else if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER, true)) {
                this.cvBox1.visibility = View.VISIBLE
                this.titleAssessmentsField.text = getString(R.string.visits_overview)
            } else {
                this.cvBox1.visibility = View.VISIBLE
                this.titleAssessmentsField.text = getString(R.string.visits_overview)
            }
            tvCount1.text = overview?.schoolsVisited.toString()
            tvCount2.text = overview?.studentsAssessed.toString()
            if (CommonUtilities.convertSecondToMinute(overview?.avgTimePerStudent ?: 0) > 1) {
                tvCount3.text =
                    String.format(
                        getString(R.string._min),
                        CommonUtilities.convertSecondToMinute(overview?.avgTimePerStudent ?: 0)
                            .toString()
                    )
            } else {
                //if no students assessed show 0 min else 1
                val minute = if ((overview?.studentsAssessed ?: 0) < 1) 0 else 1
                tvCount3.text = String.format(getString(R.string._min_one), minute)
            }
        }
        with(binding.includeGradeWiseOverview) {
            tvCount1.text = overview?.grade1Students.toString()
            tvCount2.text = overview?.grade2Students.toString()
            tvCount3.text = overview?.grade3Students.toString()
        }
    }

    private fun handleSetupNewAssessment(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        setPostHogEventSetupAssessment()
        startActivity(Intent(context, AssessmentSetupActivity::class.java))
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
            prefs = PreferenceManager.getDefaultSharedPreferences(activity!!)
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
            Log.e("-->>", "user id mentors ${it.id}")
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
        activity?.finish()
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

    fun chatBoatVisibilty(actorId: Int) {
        if (actorId == prefs.mentorDetailsData.actorId) {

        }
    }

    companion object {
        fun newInstance(): AssessmentUserHomeFragment = AssessmentUserHomeFragment().withArgs {
            putString("KeyConstants.PHONE_NUMBER", "mobileNo")
        }
    }

}