package com.samagra.commons

object AppProperties {

    var versionCode : Int = 0
    var versionName : String = ""

    fun setVersions(versionCode: Int, versionName : String) {
        this.versionCode = versionCode
        this.versionName = versionName
    }
}