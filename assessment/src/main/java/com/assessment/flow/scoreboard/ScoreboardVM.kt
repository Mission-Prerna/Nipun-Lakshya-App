package com.assessment.flow.scoreboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.data.db.models.entity.School
import com.data.models.ui.ScorecardData
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.EID_IMPRESSION
import com.samagra.commons.posthog.EVENT_STUDENT_RESULT
import com.samagra.commons.posthog.EVENT_TYPE_SCREEN_VIEW
import com.samagra.commons.posthog.NL_APP_STUDENT_SCORECARD
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.STUDENT_SCORECARD_SCREEN
import com.samagra.commons.posthog.TYPE_VIEW
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.utils.NetworkStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScoreboardVM
@Inject constructor(
    application: Application
) : BaseViewModel(application = application) {

    private val _isNipunLiveData = MutableLiveData<Boolean>()
    val isNipunLiveData: LiveData<Boolean> get() = _isNipunLiveData

    fun checkNipunStatus(list: List<ScorecardData>) {
        val isNipun = list.all { it.isPassed }
        _isNipunLiveData.value = isNipun
    }

    fun setScorecardLoadedEvent(
        ctx: Context,
        grade: String,
        schoolData: School?,
        studentId: String
    ) {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("grade", grade))
        cDataList.add(Cdata("student_id", studentId))
        cDataList.add(Cdata("speed", NetworkStateManager.instance?.getSpeed(getApplication())?:"none"))
        cDataList.add(Cdata("online", NetworkStateManager.instance?.networkConnectivityStatus?.toString()?:"none"))
        if (schoolData != null) {
            cDataList.add(Cdata("block", schoolData.block))
            cDataList.add(Cdata("block_id", schoolData.blockId.toString()))
            cDataList.add(Cdata("district", schoolData.block))
            cDataList.add(Cdata("district_id", schoolData.districtId.toString()))
        }
        val properties = PostHogManager.createProperties(
            STUDENT_SCORECARD_SCREEN,
            EVENT_TYPE_SCREEN_VIEW,
            EID_IMPRESSION,
            PostHogManager.createContext(APP_ID, NL_APP_STUDENT_SCORECARD, cDataList),
            Edata(NL_APP_STUDENT_SCORECARD, TYPE_VIEW),
            null,
            PreferenceManager.getDefaultSharedPreferences(ctx)
        )
        PostHogManager.capture(ctx, EVENT_STUDENT_RESULT, properties)
    }
}