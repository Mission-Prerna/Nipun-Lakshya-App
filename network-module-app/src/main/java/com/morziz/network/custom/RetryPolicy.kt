package com.morziz.network.custom

import retrofit2.Call
import retrofit2.Callback

interface RetryPolicy {
    fun <T> retry(proxy: Call<T>, callback: Callback<T>): Boolean
}
