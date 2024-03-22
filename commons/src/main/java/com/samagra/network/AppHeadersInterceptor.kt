package com.samagra.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AppHeadersInterceptor(private val context: Context) : Interceptor {

    private val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("appVersionCode", pInfo.versionCode.toString())
            .header("appVersionName", pInfo.versionName)
            .build()
        return chain.proceed(request)
    }
}