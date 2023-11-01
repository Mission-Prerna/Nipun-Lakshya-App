package com.assessment.schoollist

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.assessment.R
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.data.network.Result
import com.data.repository.CycleDetailsRepository
import com.data.repository.SchoolsRepository
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.utils.NetworkStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SchoolSelectionVM @Inject constructor(
    application: Application,
    private val schoolsRepository: SchoolsRepository,
    private val cycleDetailsRepository: CycleDetailsRepository
) : BaseViewModel(application) {

    val schoolSelectionState = MutableLiveData<SchoolSelectionState>()
    private var schoolHistoryFetched = false

    private suspend fun fetchSchoolHistory(ctx: Context, cycleId: Int): Result<Unit> {
        if (NetworkStateManager.instance?.networkConnectivityStatus == false) {
            return Result.Error(RuntimeException(ctx.getString(R.string.not_connected_to_internet)))
        }
        return schoolsRepository.fetchSchoolStatusHistories(cycleId)
    }

    @SuppressLint("SimpleDateFormat")
    fun getSchools(ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val cycleDetails = cycleDetailsRepository.getCurrentCycleDetails()
            if (cycleDetails == null) {
                schoolSelectionState.postValue(
                    SchoolSelectionState.Error(
                        IllegalStateException("Cycle is empty")
                    )
                )
                return@launch
            }
            if (NetworkStateManager.instance?.networkConnectivityStatus == true) {
                val remoteResult = fetchSchoolHistory(ctx, cycleDetails.id)
                schoolHistoryFetched = when (remoteResult) {
                    is Result.Error -> {
                        Timber.e(
                            remoteResult.exception,
                            "getSchools: School Selection API call fail"
                        )
                        true
                    }

                    is Result.Success -> true
                }
            }

            withContext(Dispatchers.IO) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val cycleStartDate = dateFormat.parse(cycleDetails.startDate)
                Timber.d("getSchools: cycleStartDate: $cycleStartDate")
                val cycleEndDate = dateFormat.parse(cycleDetails.endDate)
                Timber.d("getSchools: cycleEndDate: $cycleEndDate")
                val currentDate = Date()
                val invalidCycle =
                    currentDate.before(cycleStartDate) || currentDate.after(cycleEndDate)
                Timber.d("getSchools: invalid cycle: $invalidCycle")
                schoolsRepository.getSchoolsStatusHistory().collect {
                    schoolSelectionState.postValue(
                        SchoolSelectionState.Success(
                            schoolsReportHistory = it,
                            hideLoader = schoolHistoryFetched,
                            validCyle = invalidCycle.not()
                        )
                    )
                }
            }
        }
    }
}

sealed class SchoolSelectionState {
    data class Success(
        val schoolsReportHistory: List<SchoolDetailsWithReportHistory>,
        val hideLoader: Boolean,
        val validCyle: Boolean
    ) : SchoolSelectionState()

    class Error(val error: Throwable) : SchoolSelectionState()
}