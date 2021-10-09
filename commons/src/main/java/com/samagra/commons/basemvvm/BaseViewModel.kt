package com.samagra.commons.basemvvm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.samagra.commons.utils.RemoteConfigUtils

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val _remoteConfigString: MutableLiveData<String> = MutableLiveData()
    val remoteConfigString: LiveData<String> = _remoteConfigString

    fun getInfoNoteFromRemoteConfig(remoteConfigKey: String) {
        val infoNoteText = RemoteConfigUtils.getFirebaseRemoteConfigInstance().getString(remoteConfigKey)
        _remoteConfigString.value =
            if(infoNoteText.isNotEmpty()){
             infoNoteText
            }else{
                "निपुण लक्ष्य ऐप में किसी भी समस्या होने पर हेल्पलाइन नंबर 0522-3538777 पर संपर्क करें।\n"
            }
    }

    val showProgress = SingleLiveEvent<Unit>()
    val hideProgress = SingleLiveEvent<Unit>()
    val hideKeyboard = SingleLiveEvent<Unit>()
    val failure = SingleLiveEvent<String>()
    val showToastResWithId = SingleLiveEvent<Int>()

    val progressBarVisibility = SingleLiveEvent<Boolean>()
}