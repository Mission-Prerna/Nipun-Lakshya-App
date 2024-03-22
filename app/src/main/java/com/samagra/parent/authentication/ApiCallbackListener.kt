package com.samagra.parent.authentication

interface ApiCallbackListener {

    fun onSuccess(response: Any)

    fun onFailure(errorMessage: String?)

    fun onFailureResponse(error: String)
}