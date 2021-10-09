package com.samagra.parent.ui.competencyselection.readonlycompetency

import com.google.gson.Gson
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.parent.UtilityFunctions
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import com.samagra.workflowengine.workflow.model.stateresult.ModuleResult
import com.samagra.workflowengine.workflow.model.stateresult.StateResult
import java.util.*

object ResultsHelper {

    val gson by lazy { Gson() }

    fun getTotalTimeOfOneStudentSession(stateResults: List<StateResult>): Long {
        var totalTime: Long = 0
        for (results in stateResults) {
            val time = results as AssessmentStateResult
            val timeTaken = UtilityFunctions.getTimeDifferenceMilis(
                time.moduleResult.startTime,
                time.moduleResult.endTime
            )
            totalTime += timeTaken
        }
        return totalTime
    }

    fun createDummyResults(startTime: Date, bookId: String): AssessmentStateResult {
        val endTime = UtilityFunctions.getTimeMilis()
        val assessmentResult = AssessmentStateResult()
        assessmentResult.workflowRefId = bookId
        val module = ModuleResult(CommonConstants.BOLO, 1)
        module.achievement = 0
        module.isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus?:false
        module.isPassed = false // false case back pressed
        module.sessionCompleted = false
        module.appVersionCode = UtilityFunctions.getVersionCode()
        /*if (props.isCheckFluency) {
            module.statement =
                "अनुच्छेद को $requiredWordCount शब्द प्रति मिनट के प्रवाह से पढ़ लेते हैं"
        } else {
            module.statement = "अनुच्छेद के $requiredWordCount शब्द पढ़ लेते हैं"
        }*/
        module.statement = "Unable to process result!"
        module.startTime = startTime.time
        module.endTime = endTime
        assessmentResult.moduleResult = module
        return assessmentResult
    }

}
