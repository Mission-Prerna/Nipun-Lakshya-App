package com.samagra.gatekeeper

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GatekeeperRemoteResponse(
    @SerializedName("system") val system: System?,
    @SerializedName("actors") val actors: List<Actor>?,
    @SerializedName("cron") val cron: Cron,
)

data class System(
    @SerializedName("error") val error: Error?
)


data class Actor(
    @SerializedName("id") val id: String,
    @SerializedName("error") val error: Error,
)

data class Error(
    @SerializedName("action") val action: String,
    @SerializedName("description") val description: String,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String
) : Serializable

data class Cron(
    @SerializedName("enabled") val enabled: Boolean
)