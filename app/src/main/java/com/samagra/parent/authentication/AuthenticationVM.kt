package com.samagra.parent.authentication

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.samagra.ancillaryscreens.R
import com.samagra.ancillaryscreens.data.otp.ApiResponseModel
import com.samagra.ancillaryscreens.data.otp.CreatePinRequest
import com.samagra.ancillaryscreens.data.otp.HttpErrorDatum
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.ServerStatusConstants
import com.samagra.ancillaryscreens.utils.KeyConstants
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.basemvvm.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthenticationVM(
    application: Application,
    private val repository: AuthenticationRepository
) : BaseViewModel(application) {
    val backButtonClicked = SingleLiveEvent<Unit>()
    val otpSentFailure = SingleLiveEvent<String>()
    val sendOtpBtnClickedClicked = SingleLiveEvent<String>()
    val otpSentSuccess = SingleLiveEvent<String>()
    val mentorDataSavedState = SingleLiveEvent<AuthenticationRepository.MentorDataSaved>()

    fun onSendOtpBtnClicked(mobileNo: String) {
        sendOtpBtnClickedClicked.value = mobileNo
    }

    fun onBackBtnClicked() {
        backButtonClicked.call()
    }

    fun apiSendOtp(request: SendOtpRequest) {
        repository.sendOtp(getApplication(), request, object :
            ApiCallbackListener {
            override fun onSuccess(response: Any) {
                if (response is ApiResponseModel<*>) {
                    if (response.status.status == KeyConstants.SUCCESS) {
                        otpSentSuccess.value = request.phoneNo
                    } else {
                        if (response.status.error.errorCode == ServerStatusConstants.TRYING_EARLY) {
                            otpSentFailure.value =
                                (getApplication() as Context).getString(R.string.trying_early)
                        } else {
                            otpSentFailure.value =
                                (getApplication() as Context).getString(R.string.error_generic_message)
                        }
                    }
                } else {
                    otpSentFailure.value =
                        (getApplication() as Context).getString(R.string.error_generic_message)
                }
            }

            override fun onFailureResponse(error: String) {
                try {
                    val errorDatum = Gson().fromJson(error, HttpErrorDatum::class.java)
                    if (errorDatum.statusCode == ServerStatusConstants.USER_NOT_REGISTERED) {
                        otpSentFailure.value = errorDatum.message
                    } else {
                        otpSentFailure.value =
                            (getApplication() as Context).getString(R.string.error_generic_message)
                    }
                } catch (e: Exception) {
                    otpSentFailure.value =
                        (getApplication() as Context).getString(R.string.error_generic_message)
                }
            }

            override fun onFailure(errorMessage: String?) {
                otpSentFailure.value = errorMessage ?: ""
            }
        })
    }

    fun getActor(): String {
        val prefs = CommonsPrefsHelperImpl(getApplication(), "prefs")
        return prefs.selectedUser
    }

    fun getMentorData(prefs: CommonsPrefsHelperImpl){
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchMentorData(prefs).collect{
                if (it != AuthenticationRepository.MentorDataSaved.None) {
                    mentorDataSavedState.postValue(it)
                }
            }
        }
    }
}
