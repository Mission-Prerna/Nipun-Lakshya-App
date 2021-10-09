package com.samagra.commons.utils

import android.content.Context
import android.media.MediaPlayer
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.data.configmodel.NipunCriteriaModel

/*
* based on type pass grade and subject to get nipun criteria
* */
fun String.getNipunCriteria(grade: Int, subject: String): Int {
    val nipunCriteria = ConfigFile.NIPUN_CRITERIA_JSON.fetchNipunCriteria()
    var nipunCriteriaValue = "0"
    for (item in nipunCriteria) {
        if (grade == item.grade && subject.equals(item.subject, true) && item.flow_type.equals(
                this
            )
        ) {
            nipunCriteriaValue = item.nipun_criteria ?: "0"
        }
    }
    return nipunCriteriaValue.toInt()
}

/*
* Config based criteria given from ConfigFile constant.
* */
fun String.fetchNipunCriteria(): ArrayList<NipunCriteriaModel> {
    val gson = Gson()
    val type = object : TypeToken<ArrayList<NipunCriteriaModel>>() {}.type
    return gson.fromJson(this, type)
}

object ConfigFile {
    const val NIPUN_CRITERIA_JSON =
        "[{\"grade\":1,\"subject\":\"Hindi\",\"flow_type\":\"ODK\",\"nipun_criteria\":\"75\"},{\"grade\":1,\"subject\":\"Hindi\",\"flow_type\":\"RA\",\"nipun_criteria\":null},{\"grade\":1,\"subject\":\"Math\",\"flow_type\":\"ODK\",\"nipun_criteria\":\"75\"},{\"grade\":1,\"subject\":\"Math\",\"flow_type\":\"RA\",\"nipun_criteria\":null},{\"grade\":2,\"subject\":\"Hindi\",\"flow_type\":\"ODK\",\"nipun_criteria\":\"75\"},{\"grade\":2,\"subject\":\"Hindi\",\"flow_type\":\"RA\",\"nipun_criteria\":\"45\"},{\"grade\":2,\"subject\":\"Math\",\"flow_type\":\"ODK\",\"nipun_criteria\":\"75\"},{\"grade\":2,\"subject\":\"Math\",\"flow_type\":\"RA\",\"nipun_criteria\":null},{\"grade\":3,\"subject\":\"Hindi\",\"flow_type\":\"ODK\",\"nipun_criteria\":\"75\"},{\"grade\":3,\"subject\":\"Hindi\",\"flow_type\":\"RA\",\"nipun_criteria\":\"60\"},{\"grade\":3,\"subject\":\"Math\",\"flow_type\":\"ODK\",\"nipun_criteria\":\"75\"},{\"grade\":3,\"subject\":\"Math\",\"flow_type\":\"RA\",\"nipun_criteria\":null}]"
}

object CommonConstants{
    const val SELECT: String = "--Select--"
    const val ASSESSMENT_RESULTS_TEMP: String = "assessment_results_temp"
    const val BOLO = "bolo"
    const val ODK = "odk"
    const val IDENTITY_APP_SERVICE = "identity_app_service"
    const val IDENTITY_HASURA = "identity_hasura"
}

fun Context.playMusic(audioResource: Int) {
    val mediaPlayer: MediaPlayer = MediaPlayer.create(this, audioResource)
    mediaPlayer.start()
}

fun Context.loadGif(
    view: ImageView,
    drawableResource: Int,
    placeHolder: Int
) {
    Glide.with(this).load(drawableResource)
        .into(view)
        .onLoadFailed(
            ContextCompat.getDrawable(
                this,
                placeHolder
            )
        )
}