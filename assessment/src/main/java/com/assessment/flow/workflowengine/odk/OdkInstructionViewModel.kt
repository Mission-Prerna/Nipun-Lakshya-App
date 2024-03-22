package com.assessment.flow.workflowengine.odk

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.UtilityFunctions
import com.data.models.stateresult.AssessmentStateResult
import com.data.models.stateresult.ModuleResult
import com.samagra.commons.getPercentage
import com.samagra.commons.models.OdkResultData
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.EID_INTERACT
import com.samagra.commons.posthog.EVENT_ODK_COMPETENCY_SELECTION
import com.samagra.commons.posthog.EVENT_TYPE_USER_ACTION
import com.samagra.commons.posthog.NL_APP_ODK_INSTRUCTION
import com.samagra.commons.posthog.NL_SPOT_ASSESSMENT
import com.samagra.commons.posthog.OBJ_TYPE_UI_ELEMENT
import com.samagra.commons.posthog.ODK_INSTRUCTION_SCREEN
import com.samagra.commons.posthog.ODK_START_ASSESSMENT_BUTTON
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.TYPE_CLICK
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.getNipunCriteria
import java.util.Date

class OdkInstructionViewModel: ViewModel() {

    private var endTime: Long = 0
    private var startTime: Date = Date()

    fun processResult(resultData: OdkResultData?, props: OdkProperties): AssessmentStateResult {
        val assessmentResult =
            AssessmentStateResult()
        val nipunCriteria =
            AppConstants.ODK_CRITERIA_KEY.getNipunCriteria(props.grade, props.subject)
        val module = ModuleResult(CommonConstants.ODK, nipunCriteria).apply {
            totalQuestions = resultData?.totalQuestions ?: 0
            achievement = resultData?.totalMarks?.toInt()
            val percentage = getPercentage(
                resultData?.totalMarks?.toInt() ?: 0,
                resultData?.totalQuestions ?: 0
            )
            isPassed = percentage >= nipunCriteria
            isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus ?: false
            statement = "ODK flow"
        }
        assessmentResult.odkResultsData = resultData
        val startTimeLong = startTime.time
        module.apply {
            sessionCompleted = true
            appVersionCode = UtilityFunctions.getVersionCode()
            startTime = startTimeLong
            endTime = UtilityFunctions.getTimeMilis()
        }

        assessmentResult.moduleResult = module
        return assessmentResult
    }

    fun setFailureResult(): AssessmentStateResult {
        endTime = UtilityFunctions.getTimeMilis()
        val startTimeLong = startTime.time
        val dummyResultObject =
            AssessmentStateResult()
        val moduleResult = ModuleResult().apply {
            isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus ?: false
            module = CommonConstants.ODK
            achievement = 0
            isPassed = false
            totalQuestions = 0
            successCriteria = 0
            sessionCompleted = false
            appVersionCode = UtilityFunctions.getVersionCode()
            startTime = startTimeLong
            endTime = endTime
        }
        dummyResultObject.moduleResult = moduleResult
        return dummyResultObject
    }

    fun setPostHogEventSelectOdkCompetency(context: Context, props: OdkProperties, formId: String) {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("module", CommonConstants.ODK))
        cDataList.add(Cdata("competencyId", props.competencyId))
        cDataList.add(Cdata("formId", formId))
        val properties = PostHogManager.createProperties(
            ODK_INSTRUCTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_ODK_INSTRUCTION, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_CLICK),
            Object.Builder().id(ODK_START_ASSESSMENT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_ODK_COMPETENCY_SELECTION, properties)
    }

}