package com.samagra.commons.posthog.data

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Object(
    @SerializedName("id") @Expose var id: String? = null,
    @SerializedName("type") @Expose var type: String? = null,
    @SerializedName("ver") @Expose var ver: String? = null,
//    @SerializedName("rollup") @Expose private var rollup: RealmMap<String, String>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.id,
        builder.type,
        builder.ver,
//        builder.rollup
    )

    class Builder {
        var id: String? = null
        var type: String? = null
        var ver: String? = null
//        var rollup: RealmMap<String, String>? = null

        fun id(id: String?) = apply { this.id = id }
        fun type(type: String?) = apply { this.type = type }
        fun ver(ver: String?) = apply { this.ver = ver }
//        fun rollup(rollup: RealmMap<String, String>?) = apply { this.rollup = rollup }

        fun build() = Object(this)
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

}