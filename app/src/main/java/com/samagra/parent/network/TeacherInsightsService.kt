package com.samagra.parent.network

import com.morziz.network.annotation.Retry
import com.morziz.network.annotation.RetryPolicyType
import com.data.db.models.TeacherPerformanceInsightsItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface TeacherInsightsService {
    @GET("school/{udise}/teacher/performance/insights")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    suspend fun getTeacherInsights(
        @Path("udise") udise: String,
        @Header("authorization") token: String,
        @Header("Accept-Language") lang: String
    ): Response<List<TeacherPerformanceInsightsItem>>
}