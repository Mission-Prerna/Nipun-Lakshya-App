package com.data.network

import com.data.models.metadata.MetaDataRemoteResponse
import com.morziz.network.annotation.Retry
import com.morziz.network.annotation.RetryPolicyType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface MetadataService {

    @GET("metadata")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    suspend fun fetchMetaData(@Header("authorization") apiKey: String): Response<MetaDataRemoteResponse>
}