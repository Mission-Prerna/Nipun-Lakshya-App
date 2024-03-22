package com.samagra.parent.ui.individualresultnl

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import com.samagra.commons.basemvvm.BaseViewModel
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.EID_INTERACT
import com.samagra.commons.posthog.EVENT_INDIVIDUAL_RESULT_COMPLETED
import com.samagra.commons.posthog.EVENT_TYPE_USER_ACTION
import com.samagra.commons.posthog.INDIVIDUAL_NL_RESULT_SCREEN
import com.samagra.commons.posthog.NL_APP_INDIVIDUAL_RESULT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.TYPE_VIEW
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.utils.NetworkStateManager

class ResultsVM(application: Application) : BaseViewModel(application) {

    fun sendIndividualSubmissionEvent(context: Context, assessmentType: String) {
        val cDataList = java.util.ArrayList<Cdata>()
        cDataList.add(Cdata("assessment_type", assessmentType))
        cDataList.add(Cdata("speed", NetworkStateManager.instance?.getSpeed(getApplication())?:"none"))
        cDataList.add(Cdata("online", NetworkStateManager.instance?.networkConnectivityStatus?.toString()?:"none"))
        val properties = PostHogManager.createProperties(
            INDIVIDUAL_NL_RESULT_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_INDIVIDUAL_RESULT, cDataList),
            Edata(NL_APP_INDIVIDUAL_RESULT, TYPE_VIEW),
            null,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_INDIVIDUAL_RESULT_COMPLETED, properties)
    }

}
