package com.samagra.parent.authentication

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.samagra.ancillaryscreens.R
import com.samagra.ancillaryscreens.data.login.LoginModel
import com.samagra.ancillaryscreens.data.login.UserTokenInfoData
import com.samagra.ancillaryscreens.data.otp.*
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.*
import com.samagra.commons.CommonUtilities
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.basemvvm.SingleLiveEvent
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class OTPViewVM(
    application: Application,
    private val repository: AuthenticationRepository,
) : BaseViewModel(application) {
    private lateinit var phoneNumber: String
    val backButtonClicked = SingleLiveEvent<Unit>()
    val resendButtonClicked = SingleLiveEvent<Unit>()
    val verifyOtpFailure = SingleLiveEvent<Int>()
    val verifyOtpSuccess = SingleLiveEvent<Int>()
    val resendOtpSuccess = SingleLiveEvent<Int>()
    val loginUserSuccess = SingleLiveEvent<UserTokenInfoData>()
    val btnResendEnable = ObservableBoolean(false)
    val btnResendTextResId = ObservableField(R.string.resend_otp_password)

    fun onVerifyOtpButtonClicked(otpText: String) {
        otpVerifyFunctionality(otpText)
    }

    fun otpVerifyFunctionality(otpText: String) {
        if (otpText.length == NumberConstants.FOUR) {
            if (CommonUtilities.isNetworkAvailable(getApplication())) {
                showProgress.call()
                hideKeyboard.call()
                callApiVerifyOTP(otpText)
            } else {
                verifyOtpFailure.value = R.string.not_connected_to_internet
            }
        } else if (otpText.isNotEmpty() && otpText.length < 4) {
            verifyOtpFailure.value = R.string.incorrect_otp_entered
        } else {
            verifyOtpFailure.value = R.string.please_enter_the_otp
        }
    }

    private fun callApiVerifyOTP(otpText: String) {
        repository.apiVerifyOtp(
            getApplication(),
            VerifyOtpRequest(otpText, phoneNumber),
            object :
                ApiCallbackListener {
                override fun onSuccess(response: Any) {
                    if (response is ApiResponseModel<*>) {
                        if (response.status.status == KeyConstants.SUCCESS) {
                            verifyOtpSuccess.value = R.string.otp_verified_successfully
                        } else if (response.status.error.errorCode == ServerStatusConstants.TRYING_EARLY) {
                            failure.value = getStringByResId(R.string.trying_early)
                        } else if (response.status.error.errorCode == ServerStatusConstants.OTP_INCORRECT) {
                            failure.value = getStringByResId(R.string.incorrect_otp_entered)
                        } else if (response.status.error.errorCode == ServerStatusConstants.OTP_NOT_EXIST) {
                            failure.value = getStringByResId(R.string.otp_not_exist)
                        } else {
                            failure.value = getStringByResId(R.string.error_generic_message)
                        }
                    } else {
                        failure.value = getStringByResId(R.string.error_generic_message)
                    }
                }

                override fun onFailureResponse(error: String) {
                    try {
                        val errorDatum = Gson().fromJson(error, HttpErrorDatum::class.java)
                        failure.value = errorDatum.message
                    } catch (e: Exception) {
                        failure.value = getStringByResId(R.string.error_generic_message)
                    }
                }

                override fun onFailure(errorMessage: String?) {
                    failure.value = errorMessage
                }
            })
    }

    private fun getStringByResId(stringResId: Int): String {
        return (getApplication() as Context).getString(stringResId)
    }

    fun onBackButtonClicked() {
        backButtonClicked.call()
    }

    fun onResendOtpButtonClicked() {
        resendButtonClicked.call()
    }

    fun onViewCreated(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        setResendButtonEnabled(false)
    }

    fun setResendButtonEnabled(enabled: Boolean) {
        btnResendEnable.set(enabled)
    }

    fun onFinishTimer() {
        setResendButtonEnabled(true)
    }

    fun callLoginUserApi() {
        showProgress.call()
        val app_id =
            getFirebaseRemoteConfigInstance().getString(RemoteConfigUtils.FUSION_AUTH_APPLICATION_ID)
        val password =
            getFirebaseRemoteConfigInstance().getString(RemoteConfigUtils.FUSION_AUTH_PASSWORD)

        repository.loginUserApi(
            getApplication(),
            LoginRequest(phoneNumber, app_id, password),
            object :
                ApiCallbackListener {
                override fun onSuccess(response: Any) {
                    if (response is LoginModel) {
                        if (response.params.status.equals(KeyConstants.SUCCESS, true)) {
                            loginUserSuccess.value = response.result.userData.user
                        } else {
                            failure.value =
                                (getApplication() as Context).getString(R.string.error_generic_message)
                        }
                    } else {
                        failure.value =
                            (getApplication() as Context).getString(R.string.error_generic_message)
                    }
                }

                override fun onFailureResponse(error: String) {
                    try {
                        val errorDatum = Gson().fromJson(error, HttpErrorDatum::class.java)
                        failure.value = errorDatum.message
                    } catch (e: Exception) {
                        failure.value =
                            (getApplication() as Context).getString(R.string.error_generic_message)
                    }
                }

                override fun onFailure(errorMessage: String?) {
                    failure.value = errorMessage
                }
            })
    }

    fun apiSendOtp(request: SendOtpRequest) {

        repository.sendOtp(getApplication(), request, object :
            ApiCallbackListener {
            override fun onSuccess(response: Any) {
                if (response is ApiResponseModel<*>) {
                    if (response.status.status == KeyConstants.SUCCESS) {
                        resendOtpSuccess.value = R.string.otp_resend_success
                    } else {
                        if (response.status.error.errorCode == ServerStatusConstants.TRYING_EARLY) {
                            failure.value =
                                (getApplication() as Context).getString(R.string.trying_early)
                        } else {
                            failure.value =
                                (getApplication() as Context).getString(R.string.error_generic_message)
                        }
                    }
                } else {
                    failure.value =
                        (getApplication() as Context).getString(R.string.error_generic_message)
                }
            }

            override fun onFailureResponse(error: String) {
                try {
                    val errorDatum = Gson().fromJson(error, HttpErrorDatum::class.java)
                    failure.value = errorDatum.message
                } catch (e: Exception) {
                    failure.value =
                        (getApplication() as Context).getString(R.string.error_generic_message)
                }
            }

            override fun onFailure(errorMessage: String?) {
                failure.value = errorMessage ?: ""
            }
        })
    }

}