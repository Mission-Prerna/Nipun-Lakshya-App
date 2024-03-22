package com.samagra.parent.ui.competencyselection

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.data.db.models.entity.Competency
import com.data.repository.MetadataRepository
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.parent.ui.DataSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompetencySelectionVM(
    application: Application,
    private val dataSyncRepo: DataSyncRepository,
    private val metadataRepository: MetadataRepository
) : BaseViewModel(application) {

    val competenciesList: MutableLiveData<ArrayList<CompetencyModel>> = MutableLiveData()
    val learningOutcomeListFromDb: MutableLiveData<List<Competency>> = MutableLiveData()
    val onResultsPost: MutableLiveData<Unit> = MutableLiveData()

    fun getCompetenciesData(prefs: CommonsPrefsHelperImpl) {
        competenciesList.value = dataSyncRepo.fetchCompetencies(prefs)
    }

    fun getLearningOutcomeListFromDb(grade: Int){
        CoroutineScope(Dispatchers.IO).launch {
            val learningOutcomeList = metadataRepository.getLearningOutcomeForRefIdsOfCompetency(
                grade = grade,
            )
            learningOutcomeListFromDb.postValue(learningOutcomeList)
        }
    }
}