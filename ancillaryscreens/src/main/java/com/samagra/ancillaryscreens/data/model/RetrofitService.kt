package com.samagra.ancillaryscreens.data.model

import com.samagra.ancillaryscreens.fcm.model.UpsertTokenRequest
import com.samagra.commons.models.mentordetails.MentorDetailsRemoteResponse
import com.samagra.commons.models.mentordetails.MentorRemoteResponse
import com.samagra.commons.models.metadata.MetaDataRemoteResponse
import retrofit2.http.*

import retrofit2.http.GET
import retrofit2.http.Header

interface RetrofitService {

    @GET("mentor/details")
    suspend fun fetchMentorData(@Header("authorization") apiKey: String): MentorDetailsRemoteResponse

    @GET("metadata")
    suspend fun fetchMetaData(@Header("authorization") apiKey: String): MetaDataRemoteResponse

    @PUT("mentor/token")
    suspend fun upsertMentorToken(
        @Header("authorization") apiKey: String,
        @Body tokenRequest: UpsertTokenRequest
    ): MentorRemoteResponse

}