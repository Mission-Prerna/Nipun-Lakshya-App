package com.samagra.parent.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.assets.uielements.CustomMessageDialog
import com.samagra.ancillaryscreens.custom.otptextview.OTPListener
import com.samagra.ancillaryscreens.custom.otpview.OnOtpCompletionListener
import com.samagra.ancillaryscreens.data.login.UserTokenInfoData
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.fcm.NotificationViewModel
import com.samagra.ancillaryscreens.utils.Constant
import com.samagra.ancillaryscreens.utils.KeyConstants
import com.samagra.ancillaryscreens.utils.NumberConstants
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.CommonUtilities
import com.samagra.commons.basemvvm.BaseSmsReceiverFragment
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import com.samagra.commons.utils.isValidPhoneNumber
import com.samagra.commons.utils.removeFragment
import com.samagra.parent.BR
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.ViewModelProviderFactory
import com.samagra.parent.databinding.FragmentOtpViewPinBinding
import org.odk.collect.android.utilities.ToastUtils

class OTPViewFragment : BaseSmsReceiverFragment<FragmentOtpViewPinBinding, OTPViewVM>(),
    OnOtpCompletionListener, OTPListener {

    private lateinit var prefs: CommonsPrefsHelperImpl
    private lateinit var phoneNumber: String
    private lateinit var countDownTimer: CountDownTimer
    private val notificationViewModel by lazy {
        val repository = AuthenticationRepository()
        val viewModelProviderFactory =
            com.samagra.ancillaryscreens.utils.ViewModelProviderFactory(appCompatActivity!!.application, repository)
        ViewModelProvider(this, viewModelProviderFactory)[NotificationViewModel::class.java]
    }

    override fun onCodeReceived(code: String) {
        binding.otpViewTemp.setOTP(code)
        viewModel.onVerifyOtpButtonClicked(binding.otpViewTemp.otp.toString())
    }

    @LayoutRes
    override fun layoutId() = R.layout.fragment_otp_view_pin

    override fun getBaseViewModel(): OTPViewVM {
        val repository = AuthenticationRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(appCompatActivity!!.application, repository)
        return ViewModelProvider(this, viewModelProviderFactory)[OTPViewVM::class.java]
    }

    override fun getBindingVariable() = BR.otpVerifyVm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getValueFromPreferences()
        startSmsUserConsent()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTimer()// first time
        setObservers()
        initPrefs()
        setupUi()
        setListeners()
    }

    private fun setupUi() {
        val text = getString(R.string.resend_otp)
        binding.btnResend.text = text
        binding.btnResend.paintFlags = binding.btnResend.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.btnResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
    }

    private fun initPrefs() {
        prefs = CommonsPrefsHelperImpl(activity, "prefs")
    }

    private fun setObservers() {
        with(viewModel) {
            onViewCreated(phoneNumber)
            getInfoNoteFromRemoteConfig(Constant.INFO_NOTES_NEW_PIN)
            observe(verifyOtpSuccess, ::handleOTPVerifiedSuccess)
            observe(resendOtpSuccess, ::handleResendOTPSuccess)
            observe(loginUserSuccess, ::handleLoginUserSuccess)
            observe(showProgress, ::handleShowProgress)
            observe(hideKeyboard, ::handleHideKeyboard)
            observe(verifyOtpFailure, ::handleOTPVerifiedFailure)
            observe(failure, ::handleApiOTPVerifiedFailure)
            observe(backButtonClicked, ::handleBackBtnClicked)
            observe(resendButtonClicked, ::handleResendOtpBtnClicked)
        }
    }

    private fun handleLoginUserSuccess(data: UserTokenInfoData?) {
        data?.let { userData ->
            hideProgressBar()
            activity?.let { activityCtx ->
                val customDialog = CustomMessageDialog(
                    activityCtx,
                    null,
                    getString(R.string.user_logged_in),
                    null
                )
                customDialog.setOnFinishListener {
                    val authToken =
                        getFirebaseRemoteConfigInstance().getString(RemoteConfigUtils.HASURA_SERVER_AUTH_TOKEN)
                    /*
                    * first pick Jwt token from the callback api if token is null or empty then use Token from Firebase.
                    * */
                    with(userData) {
                        prefs.saveAuthToken(token ?: authToken)
                        prefs.saveRefreshToken(refreshToken ?: "")
                    }
                    prefs.saveIsUserLoggedIn(true)
                    // Give Callback to Activity
                    if (context is AuthenticationCallbacks) {
                        (context as AuthenticationCallbacks).onLoginSuccess()
                    }
                }
                customDialog.show()
            }
        }
    }

    private fun handleHideKeyboard(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        hideKeyboard()
    }

    private fun handleResendOTPSuccess(textResId: Int?) {
        hideProgressBar()
        textResId?.let { ToastUtils.showShortToast(it) }
    }

    private fun handleShowProgress(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        showProgressBar()
    }

    private fun handleApiOTPVerifiedFailure(message: String?) {
        hideKeyboard()
        hideProgressBar()
        message?.let {
            activity?.let { activityCtx ->
                val customDialog = CustomMessageDialog(
                    activityCtx, null, message, null
                )
                customDialog.setOnFinishListener {
                    //handle finish
                }
                customDialog.show()
            }
        }
    }

    private fun handleResendOtpBtnClicked(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        if (CommonUtilities.isNetworkAvailable(appCompatActivity as Context)) {
            with(viewModel) {
                binding.otpViewTemp.clearOtp()
                setResendButtonEnabled(false)
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                setSendOtpFunctionality(phoneNumber)
            }
            startTimer()
        } else {
            handleOTPVerifiedFailure(R.string.not_connected_to_internet)
        }
    }

    private fun setSendOtpFunctionality(phoneNumber: String) {
        if (CommonUtilities.isNetworkAvailable(context!!)) {
            if (phoneNumber.isNotEmpty() && !phoneNumber.isValidPhoneNumber()) {
                showProgressBar()
                val sendOtpRequest = SendOtpRequest(
                    phoneNumber, getString(R.string.error_message_send_otp),
                    getFirebaseRemoteConfigInstance()
                        .getString(RemoteConfigUtils.FUSION_AUTH_APPLICATION_ID)
                )
                viewModel.apiSendOtp(sendOtpRequest)
            } else {
                val customDialog = CustomMessageDialog(
                    activity!!,
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

    private fun handleBackBtnClicked(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        requireActivity().onBackPressed()
    }

    private fun handleOTPVerifiedFailure(textResId: Int?) {
        hideProgressBar()
        textResId?.let {
            ToastUtils.showShortToast(getString(it))
        }
    }

    private fun handleOTPVerifiedSuccess(@Suppress("UNUSED_PARAMETER") textResId: Int?) {
        hideProgressBar()
        viewModel.callLoginUserApi()
    }

    private fun getValueFromPreferences() {
        if (arguments != null) {
            phoneNumber = requireArguments().getString(KeyConstants.PHONE_NUMBER)!!
        }
    }

    private fun startTimer() {
        countDownTimer = object :
            CountDownTimer(NumberConstants.SIXTY_THOUSAND_LONG, NumberConstants.ONE_THOUSAND_LONG) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.countdownTimer.text =
                    "(00:" + millisUntilFinished / NumberConstants.ONE_THOUSAND_LONG + ")"
            }

            override fun onFinish() {
                viewModel.onFinishTimer()
                binding.countdownTimer.text = "(0)"
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }
        countDownTimer.start()
    }

    private fun setListeners() {
        binding.otpViewTemp.setOnKeyboardDefaultButtonClickListener(this)

        binding.validateButton.setOnClickListener {
            viewModel.onVerifyOtpButtonClicked(binding.otpViewTemp.otp.toString())
        }
    }

    override fun onOtpCompleted(otp: String?) {
        if (otp != null && otp.length == NumberConstants.FOUR) {
            hideKeyboard()
        }
    }

    override fun onStop() {
        countDownTimer.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        countDownTimer.cancel()
        super.onDestroy()
    }

    override fun onKeyboardDefaultButtonClick(pinText: String) {
        viewModel.onVerifyOtpButtonClicked(binding.otpViewTemp.otp.toString())
    }
}