package com.samagra.parent.ui.assessmenthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.assessment.schoollist.SchoolSelectionActivity
import com.assessment.submission.AbsentStudentsDialogFragment
import com.chatbot.BotIconState
import com.chatbot.ChatBotActivity
import com.chatbot.ChatBotVM
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseFragment
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.Result
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.*
import com.samagra.parent.authentication.AuthenticationActivity
import com.samagra.parent.databinding.FragmentAssessmentExaminerHomeBinding
import com.samagra.parent.ui.*
import com.samagra.parent.ui.assessmenthome.states.ExaminerInsightsStates
import com.samagra.parent.ui.logout.LogoutUI
import dagger.hilt.android.AndroidEntryPoint
import org.odk.collect.android.utilities.ToastUtils
import java.util.*

@AndroidEntryPoint
class AssessmentExaminerHomeFragment :
    BaseFragment<FragmentAssessmentExaminerHomeBinding, AssessmentHomeVM>() {

    @LayoutRes
    override fun layoutId() = R.layout.fragment_assessment_examiner_home

    private var dialogShowing: Boolean = false
    private var dialogBuilder: AlertDialog? = null
    private val prefs: CommonsPrefsHelperImpl by lazy { initPreferences() }
    private lateinit var insightsRecyclerView: RecyclerView
    private lateinit var insightsAdapter: ExaminerInsightsAdapter

    private val chatVM by viewModels<ChatBotVM>()

    override fun getBaseViewModel(): AssessmentHomeVM {
        val hiltViewModel: AssessmentHomeVM by activityViewModels()
        return hiltViewModel
    }

    override fun getBindingVariable() = BR.assessmentHomeVm

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        callApis(true)
        setupChatBotFlow()
        setupOverViewUI()
        setListeners()
        initRecyclerView()
        // TODO: showing dialog from here just for demo purpose. to be removed. 
        binding.tvSummaryLabel.setOnClickListener {
            val dummyList: List<String> = listOf("ABC","DEF","IJK")
            val customDialogFragment = AbsentStudentsDialogFragment.newInstance(dummyList)

            customDialogFragment.show(activity!!.supportFragmentManager, "absentStudentsDialog")
        }
    }

    private fun initRecyclerView(){
        insightsRecyclerView = binding.recyclerView
        insightsAdapter = ExaminerInsightsAdapter()
        insightsRecyclerView.adapter = insightsAdapter
        insightsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupChatBotFlow() {
        chatVM.identifyChatIconState()
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
        binding.profileDetailsView.setBlockVisibility(visibility)
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
            observe(failure, ::handleFailure)
            observe(showToastResWithId, ::handleMessage)
            observe(showSyncBeforeLogout, ::handleSyncBeforeLogout)
            observe(logoutUserLiveData, ::handleLogoutUser)
            observe(gotoLogin, ::handleLogoutRedirection)
            observe(progressBarVisibility, ::handleProgressBarVisibility)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.examinerInsightsState.collect {
                when (it) {
                    is ExaminerInsightsStates.Loading -> {
                        showProgressBar()
                    }

                    is ExaminerInsightsStates.Error -> {
                        hideProgressBar()
                        handleFailure(it.error.message)
                        binding.mtlBtnSetupAssessment.visibility = if (it.ctaEnable)
                            View.VISIBLE
                        else
                            View.GONE
                    }

                    is ExaminerInsightsStates.Success -> {
                        hideProgressBar()
                        insightsAdapter.updateItems(it.examinerInsightsStatesInfo)
                    }
                }
            }
        }

        chatVM.iconVisibilityLiveData.observe(this, ::handeIconVisibilityState)
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

    private fun handleMessage(textResId: Int?) {
        textResId?.let {
            ToastUtils.showShortToast(it)
        }
    }

    private fun initPreferences() = CommonsPrefsHelperImpl(context, "prefs")

    private fun setupOverViewUI() {
        binding.clProfileOverview.visibility = View.VISIBLE
        binding.clOverview.visibility = View.VISIBLE
        if (prefs.selectedUser.equals(AppConstants.USER_EXAMINER, true)) {
            binding.titleMentorDetails.text = getString(R.string.examiner_profile)
        } else if (prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true)) {
            binding.titleMentorDetails.text = getString(R.string.diet_mentor_profile)
        }
    }

    private fun handleSetupNewAssessment(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        setPostHogEventSetupAssessment()
        startActivity(Intent(context, SchoolSelectionActivity::class.java))
//        startActivity(Intent(context, AssessmentSetupActivity::class.java))
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
        binding.profileDetailsView.setViewModel(viewModel, true)
        viewModel.examinerPerformanceInsights(requireContext())
        val designation =
            MetaDataExtensions.getDesignationFromId(
                result?.designation_id ?: 0,
                prefs.designationsListJson
            )
        result?.let {
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
        binding.profileDetailsView.setBindingToNull()
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

    private fun handeIconVisibilityState(state: BotIconState?) {
        when (state) {
            BotIconState.Hide -> {
                binding.botFab.visibility = View.GONE
            }
            is BotIconState.Show -> {
                showChatbot(
                    animate = state.animate,
                    botView = binding.botFab,
                    botIconView = binding.botIcon,
                    imageIconRes = R.drawable.bot,
                    animationGifRes = R.drawable.animate_bot,
                    intentOnClick = Intent(context, ChatBotActivity::class.java)
                )
            }
            null -> {
                //IGNORE
            }
        }
    }

    companion object {
        fun newInstance(): AssessmentExaminerHomeFragment = AssessmentExaminerHomeFragment().withArgs {
            putString("KeyConstants.PHONE_NUMBER", "mobileNo")
        }
    }

}