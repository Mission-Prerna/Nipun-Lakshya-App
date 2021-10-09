package com.samagra.parent.ui.competencyselection

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.parent.ui.DataSyncRepository

class CompetencySelectionVM(
    application: Application,
    private val repo: CompetencySelectionRepository,
    private val dataSyncRepo: DataSyncRepository,
) : BaseViewModel(application) {

    val competenciesList: MutableLiveData<ArrayList<CompetencyModel>> = MutableLiveData()
    val onResultsPost: MutableLiveData<Unit> = MutableLiveData()

    fun getCompetenciesData(prefs: CommonsPrefsHelperImpl) {
        competenciesList.value = dataSyncRepo.fetchCompetencies(prefs)
    }
}