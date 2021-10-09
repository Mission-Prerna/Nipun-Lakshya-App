package com.samagra.commons.posthog.data

import com.google.gson.Gson

open class Edata(var pageId: String, var type: String) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}