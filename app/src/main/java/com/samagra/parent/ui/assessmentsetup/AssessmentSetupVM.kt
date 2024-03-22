package com.samagra.parent.ui.assessmentsetup

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.data.db.models.entity.School
import com.data.repository.SchoolsRepository
import com.samagra.commons.basemvvm.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AssessmentSetupVM(
    application: Application,
    val schoolsRepository: SchoolsRepository
) : BaseViewModel(application) {

    val schoolsListLiveData = MutableLiveData<List<School>>()

    fun getSchoolsFromRoom(){
        CoroutineScope(Dispatchers.IO).launch {
            val schools = schoolsRepository.getSchools()
            schoolsListLiveData.postValue(schools)
        }
    }
}