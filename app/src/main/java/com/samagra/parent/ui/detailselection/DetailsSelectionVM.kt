package com.samagra.parent.ui.detailselection

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.data.db.models.entity.Student
import com.data.repository.MetadataRepository
import com.data.repository.StudentsRepository
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.parent.ui.DataSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DetailsSelectionVM(application: Application, private val dataSyncRepo: DataSyncRepository, private val metadataRepository: MetadataRepository, private val studentsRepository: StudentsRepository) :
    BaseViewModel(application) {
    val competenciesList: MutableLiveData<ArrayList<CompetencyModel>> = MutableLiveData()
    val infoNotesLiveData: MutableLiveData<String> = MutableLiveData()
    val gradesLiveData: MutableLiveData<List<Int>> = MutableLiveData()

    fun getCompetenciesData(prefs: CommonsPrefsHelperImpl) {
        competenciesList.value = dataSyncRepo.fetchCompetencies(prefs)
    }

    fun getInfoNote() {
        infoNotesLiveData.value = dataSyncRepo.getDetailsSelectionInfoNote()
    }

    fun getDistinctGrades(){
        CoroutineScope(Dispatchers.IO).launch{
            val grades = metadataRepository.getDistinctGrades()
            val listOfAnonymousStudents = mutableListOf<Student>()
            grades.forEach{
                val anonymousId = it * -1
                val anonymousStudent =
                    Student(
                        id = anonymousId.toString(),
                        name = "-",
                        it,
                        rollNo = it.toLong(),
                        isPlaceHolderStudent = true,
                        schoolUdise = it.toLong()
                    )
                listOfAnonymousStudents.add(anonymousStudent)
            }
            studentsRepository.insertStudents(listOfAnonymousStudents)
            gradesLiveData.postValue(grades)
        }

    }
}
