package com.samagra.ancillaryscreens.data.model;

import com.google.gson.JsonElement;
import com.morziz.network.annotation.Retry;
import com.morziz.network.annotation.RetryPolicyType;
import com.samagra.ancillaryscreens.data.login.LoginModel;
import com.samagra.ancillaryscreens.data.otp.ApiResponseModel;
import com.samagra.ancillaryscreens.data.otp.LoginRequest;
import com.samagra.ancillaryscreens.data.otp.Status;
import com.samagra.commons.models.submitresultsdata.SubmitResultsModel;
import com.samagra.commons.models.surveydata.SurveyModel;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AssessmentService {
    @POST("sendOTP")
    Observable<ApiResponseModel<Status>> sendOtp(@Query("phone") String phoneNo,
                                                 @Query("errorMessage") String errorMessage);

    @POST("verifyOTP")
    Observable<LoginModel> verifyOtp(
            @Query("phone") String phoneNo,
            @Query("otp") String otp
    );

    @POST("assessment-visit-results")
    @Retry(retryPolicy = RetryPolicyType.linear, retryCount = 3)
    Call<Response<JsonElement>> insertVisitsResultsSync(@Header("authorization") String apiKey,
                                                        @Body List<SubmitResultsModel> finalResult);

    @POST("assessment-survey-results")
    Call<Response<JsonElement>> insertSurvey(@Header("authorization") String apiKey,
                                             @Body List<SurveyModel> survey);

    @POST("/api/school/{udise}/result/calculate")
    Call<Void> postSchoolSubmission(@Header("authorization") String apiKey, @Path("udise") Long udise, @Query("cycle_id") int cycleId);
}
