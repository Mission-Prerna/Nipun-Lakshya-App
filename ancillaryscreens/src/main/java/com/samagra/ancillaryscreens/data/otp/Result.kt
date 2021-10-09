package com.samagra.ancillaryscreens.data.otp

data class Result(val status: Status, var refreshToken: String, var token: String, var msg: String, var designation: String)