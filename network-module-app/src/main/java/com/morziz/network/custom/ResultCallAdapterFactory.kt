package com.morziz.network.custom

import com.morziz.network.annotation.Retry
import com.morziz.network.custom.retry.LinearRetryPolicy
import com.morziz.network.models.ApiResult
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(returnType: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?) =
        if (getRawType(returnType) == Call::class.java) {
            val callType = getParameterUpperBound(0, returnType as ParameterizedType)
            if (getRawType(callType) == ApiResult::class.java) {
                val retryPolicy: RetryPolicy? = getRetryPolicy(annotations)
                ResultCallAdapter(callType, retryPolicy)
            } else null
        } else null

    private fun getRetryPolicy(annotations: Array<out Annotation>?): RetryPolicy? {
        for (annotation in annotations ?: arrayOf()) {
            when (annotation) {
                is Retry -> {
                    return LinearRetryPolicy(annotation.retryCount)
                }
            }
        }
        return null
    }

    companion object {
        @JvmStatic
        fun create() = ResultCallAdapterFactory()
    }
}