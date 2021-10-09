package com.samagra.ancillaryscreens.data.pinverification

import java.io.Serializable

data class PinModel(
    var resourceImageValue : Int,
    var textTitle : String,
    var textButton : String,
    var createPin: Boolean
):Serializable
