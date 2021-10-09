package com.samagra.commons.posthog.data

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Actor(
    @SerializedName("id") @Expose private var id: String? = null,
    @SerializedName("type") @Expose private var type: String? = null
) {
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}