package com.data.network

import com.data.db.models.entity.SchoolStatusHistory
import com.morziz.network.annotation.Retry
import com.morziz.network.annotation.RetryPolicyType
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SchoolService {

    @GET("school/status")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    fun fetchSchoolStatusHistory(
        @Query("cycle_id") cycleId: Int,
        @Header("authorization") apiKey: String
    ): Call<List<SchoolStatusHistory>>
}