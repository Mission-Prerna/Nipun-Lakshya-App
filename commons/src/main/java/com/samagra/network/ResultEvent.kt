package com.samagra.network

sealed class ResultEvent<out T : Any> {
    data class Success<out T : Any>(val response: T) : ResultEvent<T>()
    data class Error(val exception: Exception?=null, val message:String) : ResultEvent<Nothing>()
}
