package com.samagra.parent.network

import com.data.db.models.ExaminerPerformanceInsightsItem
import com.morziz.network.annotation.Retry
import com.morziz.network.annotation.RetryPolicyType
import com.data.db.models.TeacherPerformanceInsightsItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ExaminerInsightsService {
    @GET("examiner/performance/insights")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    suspend fun getExaminerInsights(
        @Query("cycle_id") cycleId: Int,
        @Header("authorization") token: String,
        @Header("Accept-Language") lang: String
    ): Response<List<ExaminerPerformanceInsightsItem>>
}