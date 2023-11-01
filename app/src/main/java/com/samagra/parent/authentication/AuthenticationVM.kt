package com.samagra.parent.authentication

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.data.network.Result
import com.data.repository.StudentsRepository
import com.google.gson.Gson
import com.samagra.ancillaryscreens.R
import com.samagra.ancillaryscreens.data.otp.ApiResponseModel
import com.samagra.ancillaryscreens.data.otp.HttpErrorDatum
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.KeyConstants
import com.samagra.ancillaryscreens.utils.ServerStatusConstants
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.basemvvm.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AuthenticationVM(
    application: Application,
    private val repository: AuthenticationRepository,
    private val studentsRepository: StudentsRepository
) : BaseViewModel(application) {
    val backButtonClicked = SingleLiveEvent<Unit>()
    val otpSentFailure = SingleLiveEvent<String>()
    val sendOtpBtnClickedClicked = SingleLiveEvent<String>()
    val otpSentSuccess = SingleLiveEvent<String>()
    val mentorDataSavedState = SingleLiveEvent<AuthenticationRepository.MentorDataSaved>()
    private val _showProgressBar = MutableStateFlow(false)
    val showProgressBar: Flow<Boolean> = _showProgressBar.asStateFlow()

    companion object {
        const val UDISE_NULL: String = "udise_null"
        const val TEACHER_ACTOR_ID = 3
    }

    private fun showProgress(show: Boolean) {
        _showProgressBar.value = show
    }

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

    fun getMentorData(prefs: CommonsPrefsHelperImpl) {
        viewModelScope.launch(Dispatchers.IO) {
            showProgress(true)
            repository.fetchMentorData(prefs).collect {
                when (it) {
                    is AuthenticationRepository.MentorDataSaved.MentorDataSaveFailed -> {
                        mentorDataSavedState.postValue(it)
                        showProgress(false)
                    }

                    is AuthenticationRepository.MentorDataSaved.MentorDataSaveSuccessful -> {
                        val mentor = it.mentorData?.mentor
                        if (mentor == null) {
                            mentorDataSavedState.postValue(
                                AuthenticationRepository.MentorDataSaved.MentorDataSaveFailed(
                                    RuntimeException((getApplication() as Context).getString(R.string.data_save_failed))
                                )
                            )
                            showProgress(false)
                            return@collect
                        }
                        //TODO :: Optimise logic with dynamic value
                        if (mentor.actorId == TEACHER_ACTOR_ID) {
                            val udise = mentor.teacherSchoolListMapping?.schoolList?.udise
                            udise?.let { udise ->
                                val studentsResult = studentsRepository.fetchStudents(
                                    udise = udise,
                                    addDummyStudents = true
                                )
                                val cal = Calendar.getInstance()
                                studentsRepository.fetchStudentsAssessmentHistory(
                                    udise = udise,
                                    grade = "1,2,3",
                                    month = cal.get(Calendar.MONTH) + 1,
                                    year = cal.get(Calendar.YEAR)
                                )
                                when (studentsResult) {
                                    is Result.Success -> {
                                        mentorDataSavedState.postValue(it)
                                    }

                                    is Result.Error -> {
                                        mentorDataSavedState.postValue(it)
                                    }
                                }
                            } ?: also {
                                mentorDataSavedState.postValue(
                                    AuthenticationRepository.MentorDataSaved.MentorDataSaveFailed(
                                        Exception(UDISE_NULL)
                                    )
                                )
                            }
                        } else {
                            mentorDataSavedState.postValue(it)
                        }
                        showProgress(false)
                    }

                    else -> {
                        // Handle None here
                        showProgress(false)
                    }
                }
            }
        }
    }
}
