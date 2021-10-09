package com.samagra.parent.ui.assessmentsetup

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.parent.helper.RealmStoreHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AssessmentSetupVM(
    application: Application,
    private val repository: AssessmentSetupRepository
) : BaseViewModel(application) {

    val schoolDataList = MutableLiveData<ArrayList<SchoolsData>>()

    private fun getSchoolsFromRealm() {
        CoroutineScope(Dispatchers.IO).launch {
            val schoolsDataList: ArrayList<SchoolsData> = RealmStoreHelper.getSchoolsData()
            schoolDataList.postValue(schoolsDataList)
        }
    }

    fun getSchoolsData() {
        CoroutineScope(Dispatchers.IO).launch {
            getSchoolsFromRealm()
        }
    }
}