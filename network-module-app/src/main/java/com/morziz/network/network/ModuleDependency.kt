package com.morziz.network.network

import android.content.Context
import okhttp3.Interceptor

interface ModuleDependency {
    fun getBaseUrl(type: String): String
    fun getHeaders(): HashMap<String, String>?
    fun reValidateUer(code: Int)
    fun getAppContext(): Context
    fun getExtraInterceptors(): List<Interceptor>?
    fun getGoogleKeys(): String? {
        return ""
    }
}