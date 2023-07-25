package com.samagra.parent.authentication

import android.content.Context
import com.google.gson.Gson
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network.Companion.getClient
import com.samagra.ancillaryscreens.R
import com.samagra.ancillaryscreens.data.model.AssessmentService
import com.samagra.ancillaryscreens.data.model.RetrofitService
import com.samagra.ancillaryscreens.data.otp.LoginRequest
import com.samagra.ancillaryscreens.data.otp.VerifyOtpRequest
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.basemvvm.BaseRepository
import com.samagra.commons.models.mentordetails.MentorDetailsRemoteResponse
import com.samagra.commons.posthog.PRODUCT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.LOGIN_SERVICE_BASE_URL
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import com.samagra.parent.helper.MentorDataHelper
import com.samagra.parent.ui.getBearerAuthToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException

class AuthenticationRepository : BaseRepository() {

    private val gson by lazy { Gson() }
    fun sendOtp(
        context: Context,
        request: SendOtpRequest,
        listener: ApiCallbackListener
    ) {
        val service = generateUserService() ?: return
        val sendOtp = service.sendOtp(request.phoneNo, request.errorMessage, request.applicationId)
        CompositeDisposableHelper.addCompositeDisposable(sendOtp.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response -> listener.onSuccess(response) }) { error: Throwable? ->
                if (error is HttpException) {
                    listener.onFailureResponse(error.response()?.errorBody()?.string() ?: "")
                } else if (error is SocketTimeoutException) {
                    listener.onFailure(context.getString(com.samagra.parent.R.string.network_error))
                } else {
                    error?.let {
                        listener.onFailure(it.message)
                    }
                        ?: kotlin.run { listener.onFailure(context.getString(R.string.error_generic_message)) }
                }
            })
    }

    fun apiVerifyOtp(
        context: Context,
        verifyOtpRequest: VerifyOtpRequest,
        listener: ApiCallbackListener
    ) {
        val service = generateUserService() ?: return
        val verifyOtp = service.verifyOtp(verifyOtpRequest.phone_no, verifyOtpRequest.otp)
        CompositeDisposableHelper.addCompositeDisposable(verifyOtp.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response -> listener.onSuccess(response) }) { error: Throwable? ->
                if (error is HttpException) {
                    listener.onFailureResponse(error.response()?.errorBody()?.string() ?: "")
                } else if (error is SocketTimeoutException) {
                    listener.onFailure(context.getString(com.samagra.parent.R.string.network_error))
                } else {
                    error?.let {
                        listener.onFailure(it.message)
                    }
                        ?: kotlin.run { listener.onFailure(context.getString(R.string.error_generic_message)) }
                }
            })
    }

    fun loginUserApi(
        context: Context,
        loginRequest: LoginRequest,
        listener: ApiCallbackListener
    ) {
        val service = generateUserService() ?: return
        val apiKey = getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.FUSION_AUTH_API_KEY)
        val loginService = service.loginUser(loginRequest, apiKey)
        CompositeDisposableHelper.addCompositeDisposable(loginService.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                listener.onSuccess(response)
                Timber.d("loginUserApi: response: $response")
            }) { error: Throwable? ->
                if (error is HttpException) {
                    val string: String? = error.response()?.errorBody()?.string()
                    listener.onFailureResponse(string ?: "")
                } else {
                    listener.onFailure(context.getString(R.string.error_generic_message))
                }
            })
    }

    suspend fun fetchMentorData(
        prefs: CommonsPrefsHelperImpl
    ): StateFlow<MentorDataSaved> {
        val responseStatus: MutableStateFlow<MentorDataSaved> =
            MutableStateFlow(MentorDataSaved.None)
        try {
            val responseMentor = CoroutineScope(Dispatchers.IO).async {
                MentorDataHelper.apiService?.fetchMentorData(apiKey = prefs.getBearerAuthToken())
            }
            val mentorData: MentorDetailsRemoteResponse? = responseMentor.await()
            mentorData?.let {
                Timber.d("fetchMentorData: response got")
                MentorDataHelper.parseMentorData(it.mentor, prefs)
            }
            responseStatus.emit(MentorDataSaved.MentorDataSaveSuccessful(mentorData))
        } catch (t: Exception) {
            Timber.e(t, "fetchMentorData error: %s", t.message)
            responseStatus.emit(MentorDataSaved.MentorDataSaveFailed(t))
        }
        return responseStatus
    }

    private fun setBaseMap(prefs: CommonsPrefsHelperImpl, context: Context) {
        with(prefs.mentorDetailsData) {
            PostHogManager.createBaseMap(
                PRODUCT,
                id.toString(),
                id.toString(),
                designation_id.toString(),
                prefs.selectedUser,
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            )
        }
    }

    private fun generateUserService(): AssessmentService? {
        return getClient(
            ClientType.RETROFIT,
            AssessmentService::class.java,
            LOGIN_SERVICE_BASE_URL
        )
    }

    private fun generateAppService(): RetrofitService? {
        return getClient(
            ClientType.RETROFIT,
            RetrofitService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )
    }

    sealed class MentorDataSaved {
        object None : MentorDataSaved()
        class MentorDataSaveFailed(val t: Throwable) : MentorDataSaved()
        class MentorDataSaveSuccessful(val mentorData: MentorDetailsRemoteResponse?) : MentorDataSaved()
    }
}