package com.data.network

import com.data.db.models.StudentAssessmentHistoryCompleteInfo
import com.data.models.history.GradeSummary
import com.data.models.submissions.SubmitResultsModel
import com.morziz.network.annotation.Retry
import com.morziz.network.annotation.RetryPolicyType
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AssessmentService {
    @GET("school/{udise}/students/result/summary")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    fun getAssessmentHistories(
        @Path("udise") udise: String,
        @Query("grade") grades : String,
        @Header("Accept-Language") lang: String,
        @Header("authorization") token: String
    ): Call<MutableList<GradeSummary>>


    @GET("school/{udise}/students/result")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    fun getSchoolStatusHistories(
        @Path("udise") udise: String,
        @Query("grade") grades : String,
        @Query("cycle_id") cycleId : Int,
        @Header("Accept-Language") lang: String,
        @Header("authorization") token: String
    ): Call<MutableList<StudentAssessmentHistoryCompleteInfo>>

    @POST("assessment-visit-results")
    fun postSubmissions(
        @Header("authorization") apiKey: String,
        @Body finalResult: List<SubmitResultsModel>
    ): Call<Void>

    @POST("assessment-visit-results")
    fun postSubmissionsForDeletedStudents(
        @Header("authorization") apiKey: String,
        @Query("trigger") trigger: String,
        @Body finalResult: List<SubmitResultsModel>
    ): Call<Void>

}