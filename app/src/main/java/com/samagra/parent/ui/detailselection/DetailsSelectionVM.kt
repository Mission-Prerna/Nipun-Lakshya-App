package com.samagra.parent.ui.detailselection

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.parent.ui.DataSyncRepository

class DetailsSelectionVM(application: Application, private val dataSyncRepo: DataSyncRepository) :
    BaseViewModel(application) {
    val competenciesList: MutableLiveData<ArrayList<CompetencyModel>> = MutableLiveData()
    val infoNotesLiveData: MutableLiveData<String> = MutableLiveData()

    fun getCompetenciesData(prefs: CommonsPrefsHelperImpl) {
        competenciesList.value = dataSyncRepo.fetchCompetencies(prefs)
    }

    fun getInfoNote() {
        infoNotesLiveData.value = dataSyncRepo.getDetailsSelectionInfoNote()
    }
}
