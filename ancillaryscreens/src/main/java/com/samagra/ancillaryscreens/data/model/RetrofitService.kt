package com.samagra.ancillaryscreens.data.model

import com.samagra.ancillaryscreens.data.otp.CreatePinRequest
import com.samagra.commons.models.mentordetails.MentorDetailsRemoteResponse
import com.samagra.commons.models.mentordetails.MentorRemoteResponse
import com.samagra.commons.models.metadata.MetaDataRemoteResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH

interface RetrofitService {

    @PATCH("mentor/pin")
    suspend fun updateUserPin(
        @Header("authorization") apiKey: String,
        @Body body: CreatePinRequest
    ): MentorRemoteResponse

    @GET("mentor/details")
    suspend fun fetchMentorData(@Header("authorization") apiKey: String): MentorDetailsRemoteResponse

    @GET("metadata")
    suspend fun fetchMetaData(@Header("authorization") apiKey: String): MetaDataRemoteResponse
}