package com.samagra.ancillaryscreens.data.model

import com.data.db.models.AppAction
import com.data.models.mentordetails.MentorDetailsRemoteResponse
import com.data.models.mentordetails.MentorRemoteResponse
import com.samagra.ancillaryscreens.fcm.model.UpsertTokenRequest
import com.samagra.commons.models.metadata.MetaDataRemoteResponse
import retrofit2.http.*

interface RetrofitService {

    @GET("actions")
    suspend fun fetchAppActions(@Header("authorization") apiKey: String, @Query("timestamp") timestamp: Long): List<AppAction>

    @GET("v2/mentor/details")
    suspend fun fetchMentorData(@Header("authorization") apiKey: String): MentorDetailsRemoteResponse

    @GET("v2/metadata")
    suspend fun fetchMetaData(@Header("authorization") apiKey: String? = null): MetaDataRemoteResponse

    @POST("mentor/token")
    suspend fun upsertMentorToken(
        @Header("authorization") apiKey: String,
        @Body tokenRequest: UpsertTokenRequest
    ): MentorRemoteResponse

}