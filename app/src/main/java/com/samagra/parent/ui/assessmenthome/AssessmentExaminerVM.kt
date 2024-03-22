package com.samagra.parent.ui.assessmenthome

import android.app.Application
import androidx.lifecycle.ViewModel
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AssessmentExaminerVM @Inject constructor(application: Application) : BaseViewModel(application) {
    private val prefs by lazy { initPreferences() }
    val store = "empty"

    private fun initPreferences() = CommonsPrefsHelperImpl(getApplication(), "prefs")

    fun setProfileDetails(){
        val mentorDetails = prefs.mentorDetailsData
        mentorDetails.let {

        }
    }
}