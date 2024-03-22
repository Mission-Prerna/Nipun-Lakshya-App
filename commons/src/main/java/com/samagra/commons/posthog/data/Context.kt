package com.samagra.commons.posthog.data

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Context(
    @SerializedName("channel") @Expose var channel: String? = null,
    @SerializedName("pdata") @Expose var pdata: Pdata? = null,
    @SerializedName("env") @Expose var env: String? = null,
    @SerializedName("sid") @Expose var sid: String? = null,
    @SerializedName("did") @Expose var did: String? = null,
    @SerializedName("cdata") @Expose var cdata: List<Cdata>? = null,
//    @SerializedName("rollup") @Expose private var rollup: RealmMap<String, String>? = null
) {
    private constructor(builder: Builder) : this(
        builder.channel,
        builder.pdata,
        builder.env,
        builder.sid,
        builder.did,
        builder.cdata,
//        builder.rollup
    )

    class Builder {
        var channel: String? = null
        var pdata: Pdata? = null
        var env: String? = null
        var sid: String? = null
        var did: String? = null
        var cdata: List<Cdata>? = null
//        var rollup: RealmMap<String, String>? = null

        fun channel(channel: String?) = apply { this.channel = channel }
        fun pdata(pdata: Pdata?) = apply { this.pdata = pdata }
        fun env(env: String?) = apply { this.env = env }
        fun sid(sid: String?) = apply { this.sid = sid }
        fun did(did: String) = apply { this.did = did }
        fun cdata(cdata: List<Cdata>?) = apply { this.cdata = cdata }
//        fun rollup(rollup: RealmMap<String, String>?) = apply { this.rollup = rollup }

        fun build() = Context(this)
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

}