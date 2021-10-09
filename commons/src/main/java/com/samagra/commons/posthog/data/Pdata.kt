package com.samagra.commons.posthog.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Pdata(
    @SerializedName("id") @Expose var id: String? = null,
    @SerializedName("pid") @Expose var pid: String? = null,
    @SerializedName("ver") @Expose var ver: String? = null
) {
    private constructor(builder: Builder) : this(builder.id, builder.pid, builder.ver)

    class Builder {
        internal var id: String? = null
        internal var pid: String? = null
        internal var ver: String? = null

        fun id(id: String) = apply { this.id = id }
        fun pid(pid: String) = apply { this.pid = pid }
        fun ver(ver: String?) = apply { this.ver = ver }

        fun build() = Pdata(this)
    }

}