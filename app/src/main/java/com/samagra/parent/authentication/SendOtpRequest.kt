package com.samagra.parent.authentication

data class SendOtpRequest(
    var phoneNo:String,
    var errorMessage:String,
    var applicationId:String
)
