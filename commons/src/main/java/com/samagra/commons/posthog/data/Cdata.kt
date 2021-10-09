package com.samagra.commons.posthog.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Cdata(
    @SerializedName("type") @Expose var type: String? = null,
    @SerializedName("id") @Expose var id: String? = null
)