package com.samagra.parent.authentication

import android.content.Context
import com.google.gson.Gson
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network.Companion.getClient
import com.samagra.ancillaryscreens.R
import com.samagra.ancillaryscreens.data.model.AssessmentService
import com.samagra.ancillaryscreens.data.model.RetrofitService
import com.samagra.ancillaryscreens.data.otp.CreatePinRequest
import com.samagra.ancillaryscreens.data.otp.LoginRequest
import com.samagra.ancillaryscreens.data.otp.VerifyOtpRequest
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.basemvvm.BaseRepository
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.models.Result
import com.samagra.commons.models.mentordetails.MentorRemoteResponse
import com.samagra.commons.posthog.PRODUCT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.LOGIN_SERVICE_BASE_URL
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

    suspend fun updateUserPin(
        context: Context,
        requestBody: CreatePinRequest,
        prefs: CommonsPrefsHelperImpl
    ): StateFlow<PinUpdateResult> {
        val remoteResponseStatus: MutableStateFlow<PinUpdateResult> =
            MutableStateFlow(PinUpdateResult.None)
        Timber.d("updateUserPin: ")
        try {
            val response = generateAppService()?.updateUserPin(
                apiKey = Constants.BEARER_ + prefs.authToken,
                body = requestBody
            )
            response?.let {
                Timber.d("updateUserPin: response got $it")
                handleUpdatedUser(
                    mentor = it,
                    prefs = prefs,
                    pin = requestBody.pin,
                    remoteResponseStatus = remoteResponseStatus,
                    context
                )
            }
        } catch (t: Exception) {
            Timber.e(t, "updateUserPin error: %s", t.message)
            remoteResponseStatus.emit(PinUpdateResult.PinUpdateFailed(t))
        }
        return remoteResponseStatus
    }

    private suspend fun handleUpdatedUser(
        mentor: MentorRemoteResponse,
        prefs: CommonsPrefsHelperImpl,
        pin: String,
        remoteResponseStatus: MutableStateFlow<PinUpdateResult>,
        context: Context
    ) {
        Timber.d("handleUpdatedUser: ")
        saveUserDetails(mentor, prefs, pin)
        handleRedirections(mentor, prefs, remoteResponseStatus, context)
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

    private fun saveUserDetails(
        mentor: MentorRemoteResponse,
        prefs: CommonsPrefsHelperImpl,
        pin: String
    ) {
        Timber.d("saveUserDetails: $pin")
        val mentorDetailsToSave = Result(
            id = mentor.id ?: 0,
            designation_id = mentor.designationId ?: 0,
            district_id = mentor.districtId ?: 0,
            district_name = mentor.districtName ?: "",
            block_id = mentor.blockId ?: 0,
            block_town_name = mentor.blockTownName ?: "",
            officer_name = mentor.officerName ?: "",
            phone_no = mentor.phoneNo ?: "",
            schoolId = mentor.teacherSchoolListMapping?.schoolList?.schoolId ?: 0,
            schoolDistrict = mentor.teacherSchoolListMapping?.schoolList?.district ?: "",
            schoolDistrictId = mentor.teacherSchoolListMapping?.schoolList?.districtId ?: 0,
            schoolBlock = mentor.teacherSchoolListMapping?.schoolList?.block ?: "",
            schoolBlockId = mentor.teacherSchoolListMapping?.schoolList?.blockId ?: 0,
            schoolNyayPanchayat = mentor.teacherSchoolListMapping?.schoolList?.nyayPanchayat ?: "",
            schoolNyayPanchayatId = mentor.teacherSchoolListMapping?.schoolList?.nyayPanchayatId
                ?: 0,
            schoolName = mentor.teacherSchoolListMapping?.schoolList?.schoolName ?: "",
            udise = mentor.teacherSchoolListMapping?.schoolList?.udise ?: 0L,
            actorId = if (mentor.actorId == UserConstants.DIET_MENTOR_INT) {
                1
            } else {
                mentor.actorId ?: 0
            },
            schoolLat = mentor.teacherSchoolListMapping?.schoolList?.schoolLat ?: 0.0,
            schoolLong = mentor.teacherSchoolListMapping?.schoolList?.schoolLong ?: 0.0,
            schoolGeoFenceEnabled = mentor.teacherSchoolListMapping?.schoolList?.geofencingEnabled
                ?: true
        )
        Timber.d("saveUserDetails: $mentorDetailsToSave")
        prefs.saveMentorDetails(gson.toJson(mentorDetailsToSave))
        prefs.saveCreatedPin(pin)
    }

    private suspend fun handleRedirections(
        mentor: MentorRemoteResponse,
        prefs: CommonsPrefsHelperImpl,
        remoteResponseStatus: MutableStateFlow<PinUpdateResult>,
        context: Context
    ) {
        Timber.d("handleRedirections: ")
        val actor = MetaDataExtensions.getActorFromActorId(
            actorId = if (mentor.actorId == UserConstants.DIET_MENTOR_INT) {
                1
            } else {
                mentor.actorId ?: 0
            },
            actorListJson = prefs.actorsListJson
        )
        Timber.d("handleRedirections: actor: $actor")
        if (actor.equals(Constants.USER_EXAMINER, true)) {
            Timber.d("handleRedirections: user examiner")
            prefs.saveAssessmentType(Constants.STATE_LED_ASSESSMENT)
        }
        Timber.d("handleRedirections: success")
        prefs.saveSelectedUser(actor)
        setBaseMap(prefs, context)
        remoteResponseStatus.emit(PinUpdateResult.PinUpdateSuccessful)
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

    sealed class PinUpdateResult {
        object None : PinUpdateResult()
        class PinUpdateFailed(val t: Throwable) : PinUpdateResult()
        object PinUpdateSuccessful : PinUpdateResult()
    }
}