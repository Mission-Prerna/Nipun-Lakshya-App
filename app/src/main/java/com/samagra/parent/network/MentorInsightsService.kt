package com.samagra.parent.network

import com.data.db.models.MentorPerformanceInsightsItem
import com.morziz.network.annotation.Retry
import com.morziz.network.annotation.RetryPolicyType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface MentorInsightsService {
    @GET("v2/mentor/performance/insights")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    suspend fun getMentorInsights(
        @Header("authorization") token: String,
        @Header("Accept-Language") lang: String
    ): Response<MentorPerformanceInsightsItem>
}