package com.samagra.parent.authentication

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.assets.uielements.CustomMessageDialog
import com.samagra.ancillaryscreens.AncillaryScreensDriver
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.fcm.NotificationViewModel
import com.samagra.ancillaryscreens.utils.KeyConstants
import com.samagra.ancillaryscreens.utils.SnackbarUtils
import com.samagra.ancillaryscreens.utils.TagConstants
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.CommonUtilities.isNetworkAvailable
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.ExchangeObject
import com.samagra.commons.Modules
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.constants.Constants
import com.samagra.commons.helper.GatekeeperHelper
import com.samagra.commons.models.mentordetails.MentorDetailsRemoteResponse
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.*
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivityAuthenticationBinding
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import com.samagra.parent.ui.userselection.UserSelectionRepository
import com.samagra.parent.ui.userselection.UserSelectionVM
import org.odk.collect.android.utilities.ToastUtils
import timber.log.Timber

class AuthenticationActivity :
    BaseActivity<ActivityAuthenticationBinding, AuthenticationVM>(), AuthenticationCallbacks {

    private val prefs by lazy { initPreferences() }

    private val notificationViewModel by lazy {
        val repository = AuthenticationRepository()
        val viewModelProviderFactory =
            com.samagra.ancillaryscreens.utils.ViewModelProviderFactory(application, repository)
        ViewModelProvider(this, viewModelProviderFactory)[NotificationViewModel::class.java]
    }

    private val userVM by lazy {
        val repository = UserSelectionRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(application, repository)
        ViewModelProvider(this, viewModelProviderFactory)[UserSelectionVM::class.java]
    }

    @LayoutRes
    override fun layoutRes() = R.layout.activity_authentication

    override fun getBaseViewModel(): AuthenticationVM {
        val repository = AuthenticationRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(this.application, repository)
        return ViewModelProvider(
            this,
            viewModelProviderFactory
        )[AuthenticationVM::class.java]
    }

    override fun getBindingVariable() = BR.loginVm

    enum class RedirectionFlow {
        CALL_SEND_OTP,
        LAUNCH_PARENT_FLOW,
        NOTHING
    }

    override fun onLoadData() {
        setObserver()
        setupUi()
        setKeyboardListener()
        setupListeners()
        userVM.fetchData(prefs, Pair(RedirectionFlow.NOTHING, ""))
        GatekeeperHelper.assess(
            context = this,
            skipWarning = true
        )
        userVM.flowDecisionLiveData.observe(this) { pair ->
            when (pair.first) {
                RedirectionFlow.CALL_SEND_OTP -> {
                    setSendOtpFunctionality(pair.second)
                }
                RedirectionFlow.LAUNCH_PARENT_FLOW -> {
                    redirectToParentFLow()
                }
                RedirectionFlow.NOTHING -> {
                    //do nothing
                }
            }
        }
    }

    private fun setupListeners() {
        binding.cvParent.setOnClickListener {
            if (checkIfMetaDataPresent()) {
                redirectToParentFLow()
            } else {
                userVM.fetchData(prefs, Pair(RedirectionFlow.LAUNCH_PARENT_FLOW, ""))
            }
        }
    }

    private fun redirectToParentFLow() {
        prefs.saveSelectedUser(AppConstants.USER_PARENT)
        prefs.saveAssessmentType(AppConstants.NIPUN_ABHYAS)
        Timber.d("setListeners: selected user ${prefs.selectedUser}")
        sendTelemetry(EVENT_SELECT_PARENT, PARENT_BUTTON)
        CustomEventCrashUtil.setSelectedUserProperty(prefs.selectedUser)
        val intentToHome = Intent(this, DetailsSelectionActivity::class.java)
        startActivity(intentToHome)
    }

    private fun checkIfMetaDataPresent(): Boolean {
        return prefs.actorsListJson.isNotEmpty() && prefs.competencyData.isNotEmpty()
    }

    private fun sendTelemetry(event: String, button: String) {
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedUser = prefs.selectedUser
        PostHogManager.createBaseMap(
            PRODUCT,
            selectedUser,
            selectedUser,
            selectedUser,
            selectedUser,
            defaultSharedPreferences
        )
        val properties = PostHogManager.createProperties(
            USERSELECTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_USER_SELECTION, ArrayList()),
            Edata(NL_USERSELECTION, TYPE_CLICK),
            Object.Builder().id(button).type(OBJ_TYPE_UI_ELEMENT).build(), defaultSharedPreferences
        )
        PostHogManager.capture(this, event, properties)
    }

    private fun setupUi() {
        binding.tvVersionName.text = UtilityFunctions.getVersionName(this)
        binding.llCallHelpline.setOnClickListener { callHelpline() }
    }

    private fun callHelpline() {
        val intent = Intent(Intent.ACTION_DIAL)
        val phoneNumber = getString(R.string.helpline_number)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun setObserver() {
        with(viewModel) {
            observe(backButtonClicked, ::handleBackButton)
            observe(sendOtpBtnClickedClicked, ::handleSendOtpButton)
            observe(otpSentSuccess, ::handleOtpSentSuccess)
            observe(otpSentFailure, ::handleOtpSentFailure)
            observe(mentorDataSavedState, ::handleMentorDataSavedState)
        }
    }

    private fun setKeyboardListener() {
        binding.etResult.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (checkIfMetaDataPresent()) {
                        setSendOtpFunctionality(binding.etResult.text.toString())
                    } else {
                        userVM.fetchData(
                            prefs,
                            Pair(RedirectionFlow.CALL_SEND_OTP, binding.etResult.text.toString())
                        )
                    }
                }
                true
            } else false
        }
    }

    private fun handleOtpSentFailure(errorMessage: String?) {
        hideProgressBar()
        binding.btnCollect.isClickable = true
        binding.btnCollect.isEnabled = true
        ContextCompat.getDrawable(
            this,
            R.drawable.ic_warning_error
        )?.let {
            val customDialog = CustomMessageDialog(
                this,
                it,
                getString(R.string.unable_to_send_OTP),
                errorMessage
            )
            customDialog.setOnFinishListener {
                //handle finish
            }
            customDialog.show()
        }
    }

    private fun handleOtpSentSuccess(phoneNumber: String?) {
        hideProgressBar()
        binding.etResult.text.clear()
        phoneNumber?.let {
            SnackbarUtils.showShortSnackbar(
                binding.otpParent,
                getString(R.string.otp_sent_successfully, it.getHiddenMobileNumber())
            )
            redirectToOtpVerifyScreen(it)
        }
    }

    private fun handleSendOtpButton(mobileNo: String?) {
        mobileNo?.let {
            if (checkIfMetaDataPresent()) {
                setSendOtpFunctionality(it)
            } else {
                userVM.fetchData(prefs, Pair(RedirectionFlow.CALL_SEND_OTP, it))
            }
        } ?: run {
            SnackbarUtils.showShortSnackbar(
                binding.otpParent.rootView,
                getString(R.string.try_again)
            )
        }
    }

    private fun setSendOtpFunctionality(phoneNumber: String) {
        if (isNetworkAvailable(this)) {
            if (phoneNumber.isNotEmpty() && !phoneNumber.isValidPhoneNumber()) {
                binding.btnCollect.isClickable = true
                showProgressBar()
                hideKeyboard(this)
                val sendOtpRequest = SendOtpRequest(
                    phoneNumber, getString(R.string.error_message_send_otp),
                    RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                        .getString(RemoteConfigUtils.FUSION_AUTH_APPLICATION_ID)
                )
                viewModel.apiSendOtp(sendOtpRequest)
            } else {
                val customDialog = CustomMessageDialog(
                    this,
                    null,
                    getString(R.string.invalid_phone_number),
                    getString(R.string.enter_valid_number)
                )
                customDialog.setOnFinishListener {
                    //handle finish
                }
                customDialog.show()
                binding.etResult.text.clear()
            }
        } else {
            ToastUtils.showShortToast(getString(R.string.internet_not_connected_error_login))
        }
    }

    private fun handleBackButton(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (binding.mobileFragmentContainer.childCount > 0) {
            binding.mobileFragmentContainer.removeAllViews()
        } else {
            super.onBackPressed()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        CompositeDisposableHelper.destroyCompositeDisposable()
    }

    private fun redirectToOtpVerifyScreen(mobileNo: String?) {
        val otpFragment = OTPViewFragment()
        val arguments = Bundle()
        arguments.putString(KeyConstants.PHONE_NUMBER, mobileNo)
        otpFragment.arguments = arguments
        addFragment(
            binding.mobileFragmentContainer.id,
            supportFragmentManager,
            otpFragment,
            TagConstants.OTP_VIEW_FRAGMENT,
            true
        )
    }

    private fun initPreferences() = CommonsPrefsHelperImpl(this, "prefs")
    override fun onLoginSuccess() = viewModel.getMentorData(prefs)

    private fun handleMentorDataSavedState(mentorDataSaved: AuthenticationRepository.MentorDataSaved?) {
        when (mentorDataSaved) {
            is AuthenticationRepository.MentorDataSaved.MentorDataSaveFailed -> {
                ToastUtils.showShortToast(getString(R.string.not_nipun))
            }

            is AuthenticationRepository.MentorDataSaved.MentorDataSaveSuccessful -> {
                setupHomeRedirection()
                logLoginSuccessEvent(mentorDataSaved.mentorData)
            }
            else -> {
                //DO NOTHING
            }
        }
    }

    private fun setupHomeRedirection() {
        hideProgressBar()
        notificationViewModel.registerFCMToken(
            prefs = prefs
        )
        redirectToAssessmentHome()
    }

    private fun redirectToAssessmentHome() {
        val intentToHome: Intent = when {
            prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true) -> {
                Intent(Constants.INTENT_LAUNCH_DIET_ASSESSMENT_TYPE_ACTIVITY)
            }
            prefs.selectedUser.equals(Constants.USER_TEACHER, true) -> {
                Intent(Constants.INTENT_LAUNCH_ASSESSMENT_HOME_ACTIVITY)
            }
            else -> {
                Intent(Constants.INTENT_LAUNCH_ASSESSMENT_HOME_ACTIVITY)
            }
        }
        val signalExchangeObject =
            ExchangeObject.SignalExchangeObject(
                Modules.MAIN_APP,
                Modules.ANCILLARY_SCREENS,
                intentToHome,
                true
            )
        AncillaryScreensDriver.mainApplication.eventBus.send(signalExchangeObject)
        finish()
    }

    private fun logLoginSuccessEvent(mentorData: MentorDetailsRemoteResponse?) {
        val cDataArrayList = arrayListOf(
            Cdata(
                type = "actor",
                id = mentorData?.mentor?.actorId.toString()
            ),
            Cdata(
                type = "id",
                id = mentorData?.mentor?.id.toString()
            )
        )
        val properties = PostHogManager.createProperties(
            page = CHATBOT_SCREEN,
            eventType = EVENT_TYPE_USER_ACTION,
            eid = EID_INTERACT,
            context = PostHogManager.createContext(
                id = APP_ID,
                pid = NL_APP_CHATBOT,
                dataList = cDataArrayList
            ),
            eData = Edata(NL_LOGIN, TYPE_CLICK),
            objectData = null,
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(
            context = this,
            eventName = EVENT_LOGIN_SUCCESS,
            properties = properties
        )
    }

}
