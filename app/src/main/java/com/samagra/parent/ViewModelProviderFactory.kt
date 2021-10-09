package com.samagra.parent

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.samagra.commons.basemvvm.BaseRepository
import com.samagra.parent.authentication.AuthenticationRepository
import com.samagra.parent.authentication.AuthenticationVM
import com.samagra.parent.authentication.OTPViewVM
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.assessmenthome.AssessmentHomeVM
import com.samagra.parent.ui.assessmentsetup.AssessmentSetupRepository
import com.samagra.parent.ui.assessmentsetup.AssessmentSetupVM
import com.samagra.parent.ui.assessmenttype.AssessmentTypeVM
import com.samagra.parent.ui.competencyselection.CompetencySelectionRepository
import com.samagra.parent.ui.competencyselection.CompetencySelectionVM
import com.samagra.parent.ui.detailselection.DetailsSelectionVM
import com.samagra.parent.ui.dietmentorassessmenttype.DIETAssessmentTypeVM
import com.samagra.parent.ui.userselection.UserSelectionRepository
import com.samagra.parent.ui.userselection.UserSelectionVM

@Suppress("UNCHECKED_CAST")
class ViewModelProviderFactory(
    val application: Application,
    private val repository: BaseRepository
) : ViewModelProvider.Factory {
    private lateinit var repository1: BaseRepository
    private lateinit var repository2: BaseRepository

    constructor(
        application: Application,
        repository: BaseRepository,
        repository1: BaseRepository
    ) : this(application, repository) {
        this.repository1 = repository1
    }

    constructor(
        application: Application,
        repository: BaseRepository,
        repository1: BaseRepository,
        repository2: BaseRepository
    ) : this(application, repository, repository1) {
        this.repository1 = repository1
        this.repository2 = repository2
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(AssessmentHomeVM::class.java) -> {
                return AssessmentHomeVM(
                    application,
                    repository as DataSyncRepository
                ) as T
            }
            modelClass.isAssignableFrom(AssessmentSetupVM::class.java) -> {
                return AssessmentSetupVM(
                    application,
                    repository as AssessmentSetupRepository
                ) as T
            }
            modelClass.isAssignableFrom(CompetencySelectionVM::class.java) -> {
                return CompetencySelectionVM(
                    application,
                    repository as CompetencySelectionRepository,
                    repository1 as DataSyncRepository,
                ) as T
            }
            modelClass.isAssignableFrom(UserSelectionVM::class.java) -> {
                return UserSelectionVM(
                    application,
                    repository as UserSelectionRepository,
                ) as T
            }
            modelClass.isAssignableFrom(AssessmentTypeVM::class.java) -> {
                return AssessmentTypeVM(
                    application,
                    repository as DataSyncRepository
                ) as T
            }
            modelClass.isAssignableFrom(DIETAssessmentTypeVM::class.java) -> {
                return DIETAssessmentTypeVM(
                    application,
                    repository as DataSyncRepository
                ) as T
            }
            modelClass.isAssignableFrom(DetailsSelectionVM::class.java) -> {
                return DetailsSelectionVM(
                    application,
                    repository as DataSyncRepository
                ) as T
            }
            modelClass.isAssignableFrom(OTPViewVM::class.java) -> {
                return OTPViewVM(
                    application,
                    repository as AuthenticationRepository
                ) as T
            }
            modelClass.isAssignableFrom(AuthenticationVM::class.java) -> {
                return AuthenticationVM(
                    application,
                    repository as AuthenticationRepository
                ) as T
            }
            else -> {
                throw IllegalArgumentException("View Model class not initialized in Factory : ${modelClass.name}")
            }
        }
    }
}