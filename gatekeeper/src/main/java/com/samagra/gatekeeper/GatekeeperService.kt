package com.samagra.gatekeeper

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

internal interface GatekeeperService {

    @GET("gatekeeper")
    fun getGatekeeper(
        @Header("x-application-id") applicationId: String,
        @Header("x-api-key") apiKey: String
    ): Call<GatekeeperRemoteResponse>

}