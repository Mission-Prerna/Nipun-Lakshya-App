package com.assessment.schoollist

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.assessment.R
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.data.models.submissions.StudentNipunStates
import com.data.network.Result
import com.data.repository.CycleDetailsRepository
import com.data.repository.SchoolsRepository
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.EID_INTERACT
import com.samagra.commons.posthog.EVENT_SCHOOL_SELECTION
import com.samagra.commons.posthog.EVENT_TYPE_USER_ACTION
import com.samagra.commons.posthog.NL_APP_SCHOOL_SELECTION
import com.samagra.commons.posthog.NL_SCHOOL_SELECTION
import com.samagra.commons.posthog.OBJ_TYPE_UI_ELEMENT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.SCHOOLS_SELECTION_SCREEN
import com.samagra.commons.posthog.SELECT_SCHOOL_BUTTON
import com.samagra.commons.posthog.TYPE_CLICK
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
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
    private var visitList: ArrayList<SchoolDetailsWithReportHistory> = arrayListOf()

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
    fun onCheckboxVisitStateChanged(isChecked: Boolean, list: ArrayList<SchoolDetailsWithReportHistory>): ArrayList<SchoolDetailsWithReportHistory>{
        if (isChecked) {
            visitList.clear()
            visitList =
                list.toMutableList() as java.util.ArrayList<SchoolDetailsWithReportHistory>
            val iterator = visitList.iterator()
            while (iterator.hasNext()) {   //If user checks the option of showing only not visited schools, we iterate and filter out the nipun/not nipun schools so as to get schools which are only in pending state.
                val dis = iterator.next()
                val schoolStatus = dis.status
                if (!schoolStatus.isNullOrEmpty() && !schoolStatus.equals(
                        StudentNipunStates.pending,
                        true
                    )
                ) {
                    iterator.remove()
                }
                Log.i("update adapter", "checkboxVisit true")
            }
            Timber.d(" checktrue last visit list and size ${visitList.size} \n $visitList ")
            return visitList
        } else {
            Log.i("update adapter", "checkboxVisit false")
            return list
        }
    }

    fun districtFilterSchoolList(districtFilterSchoolList: ArrayList<SchoolDetailsWithReportHistory>, item: String): ArrayList<SchoolDetailsWithReportHistory>{
        val iterator = districtFilterSchoolList.iterator()
        while (iterator.hasNext()) {
            val dis = iterator.next()
            if (dis.district != item) { // Filtering the school list to get only schools based on criteria of district. Removing from the list if such a criteria is not matched.
                iterator.remove()
            }
        }
        return districtFilterSchoolList
    }

    fun blockFilterSchoolList(
        blockFilterSchoolList: ArrayList<SchoolDetailsWithReportHistory>,
        item: String,
        selectedDistrict: String?
    ): ArrayList<SchoolDetailsWithReportHistory>{
        val iterator = blockFilterSchoolList.iterator()
        while (iterator.hasNext()) {
            val dis = iterator.next()
            if (dis.block != item || dis.district != selectedDistrict) { // Filtering the school list to get only schools based on criteria of district, block. Removing from the list if such a criteria is not matched.
                iterator.remove()
            }
        }
        return blockFilterSchoolList
    }

    fun npFilterSchoolList(
        npFilterSchoolList: ArrayList<SchoolDetailsWithReportHistory>,
        item: String,
        selectedDistrict: String?,
        selectedBlock: String?
    ): ArrayList<SchoolDetailsWithReportHistory>{
        val iterator = npFilterSchoolList.iterator()
        while (iterator.hasNext()) {
            val dis = iterator.next()
            if (dis.nyayPanchayat != item || dis.district != selectedDistrict || dis.block != selectedBlock) { // Filtering the school list to get only schools based on criteria of district, block, NP. Removing from the list if such a criteria is not matched.
                iterator.remove()
            }
        }
        return npFilterSchoolList
    }

    fun setPostHogEventSelectSchool(schoolsData: SchoolDetailsWithReportHistory, context: Context) {
        val cDataList = ArrayList<Cdata>()
        if (schoolsData.schoolName != null) {
            cDataList.add(Cdata("schoolName", schoolsData.schoolName))
        }
        if (schoolsData.visitStatus != null) {
            cDataList.add(Cdata("isVisited", "${schoolsData.visitStatus}"))
        }
        if (schoolsData.udise != null) {
            cDataList.add(Cdata("UDISE", schoolsData.udise.toString()))
        }
        cDataList.add(Cdata("dateOfSelection", Date().time.toString()))
        val properties = PostHogManager.createProperties(
            page = SCHOOLS_SELECTION_SCREEN,
            eventType = EVENT_TYPE_USER_ACTION,
            eid = EID_INTERACT,
            context = PostHogManager.createContext(APP_ID, NL_APP_SCHOOL_SELECTION, cDataList),
            eData = Edata(NL_SCHOOL_SELECTION, TYPE_CLICK),
            objectData = Object.Builder().id(SELECT_SCHOOL_BUTTON).type(OBJ_TYPE_UI_ELEMENT)
                .build(),
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_SCHOOL_SELECTION, properties)
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