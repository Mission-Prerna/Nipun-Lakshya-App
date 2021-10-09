package com.samagra.parent.authentication

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.assets.uielements.CustomMessageDialog
import com.samagra.ancillaryscreens.AncillaryScreensDriver
import com.samagra.ancillaryscreens.data.pinverification.PinModel
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
import com.samagra.commons.posthog.*
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
        PostHogManager.createBaseMap(
            PRODUCT,
            prefs.selectedUser,
            prefs.selectedUser,
            prefs.selectedUser,
            prefs.selectedUser,
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
        setSpannableInfo()
        binding.tvVersionName.text = UtilityFunctions.getVersionName(this)
    }

    private fun setSpannableInfo() {
        val text = getText(R.string.trouble_in_login)
        val ss = SpannableString(text)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_DIAL)
                val phoneNumber = text.substring(text.lastIndexOf(" ") + 1, text.length)
                intent.data = Uri.parse("tel:$phoneNumber")
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(
            clickableSpan,
            text.lastIndexOf(" ") + 1,
            text.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        binding.tvTroubleShoot.text = ss
        binding.tvTroubleShoot.movementMethod = LinkMovementMethod.getInstance()
        binding.tvTroubleShoot.highlightColor = ContextCompat.getColor(this, R.color.blue_2e3192)
    }

    private fun setObserver() {
        with(viewModel) {
            getInfoNoteFromRemoteConfig(RemoteConfigUtils.INFO_NOTES_LOGIN)
            observe(remoteConfigString, ::handleInfoNoteText)
            observe(backButtonClicked, ::handleBackButton)
            observe(sendOtpBtnClickedClicked, ::handleSendOtpButton)
            observe(otpSentSuccess, ::handleOtpSentSuccess)
            observe(otpSentFailure, ::handleOtpSentFailure)
            observe(pinUpdateState, ::handlePinUpdateState)
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

    private fun handleInfoNoteText(infoNote: String?) {
        infoNote?.let {
            binding.llInfo.tvInfoNote.text = it
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

    private fun redirectToPinCreation() {
        val pinModel = PinModel(
            resourceImageValue = R.drawable.ic_create_pin,
            textTitle = getString(R.string.enter_new_pin),
            textButton = getString(R.string.create_pin),
            createPin = true
        )
        val pinFrag = PinFragment.newInstance(pinModel)
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        replaceFragment(
            binding.mobileFragmentContainer.id,
            supportFragmentManager,
            pinFrag,
            TagConstants.PIN_FRAGMENT,
            false
        )
        pinFrag.setListener(object : PinFragment.PinActionListener {
            override fun onCloseClicked() {
                onBackPressed()
            }

            override fun onCreateNewPinClicked(pin: String) {
                if (isNetworkAvailable(this@AuthenticationActivity)) {
                    showProgressBar()
                    viewModel.updateLoginPinMutation(pin, prefs)
                } else {
                    ToastUtils.showShortToast(getString(R.string.not_connected_to_internet))
                }
            }
        })
    }


    private fun initPreferences() = CommonsPrefsHelperImpl(this, "prefs")
    override fun onLoginSuccess() {
        redirectToPinCreation()
    }

    private fun handlePinUpdateState(pinUpdateResult: AuthenticationRepository.PinUpdateResult?) {
        when (pinUpdateResult) {
            is AuthenticationRepository.PinUpdateResult.PinUpdateFailed -> {
                hideProgressBar()
                ToastUtils.showShortToast(getString(R.string.pin_update_error))
            }
            AuthenticationRepository.PinUpdateResult.PinUpdateSuccessful -> {
                hideProgressBar()
                setupHomeRedirection()
            }
            else -> {
                //DO NOTHING
            }
        }
    }

    private fun setupHomeRedirection() {
        hideProgressBar()
        notificationViewModel.registerFCMToken(prefs.mentorDetailsData.id)
        LogEventsHelper.setPinCreationEvent(
            context = this,
            loginPin = prefs.loginPin,
            screen = PIN_CREATION_DIALOG,
            pId = NL_APP_PIN_CREATION,
            ePageId = NL_PIN_CREATION
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

}
