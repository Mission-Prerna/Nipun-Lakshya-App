package com.assessment.flow.workflowengine

import com.data.models.stateresult.AssessmentStateResult
import com.data.models.stateresult.ModuleResult
import com.data.models.stateresult.StateResult
import com.google.gson.Gson
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
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

    fun createDummyResults(startTime: Date, bookId: String, readAlongInstalled : Boolean = true): AssessmentStateResult {
        val endTime = UtilityFunctions.getTimeMilis()
        val assessmentResult =
            AssessmentStateResult()
        assessmentResult.workflowRefId = bookId
        val module =
            ModuleResult(CommonConstants.BOLO, 1)
        module.achievement = 0
        module.isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus?:false
        module.isPassed = false // false case back pressed
        module.sessionCompleted = false
        module.appVersionCode = UtilityFunctions.getVersionCode()
        module.statement = "Unable to process result!. Read Along Installed : $readAlongInstalled"
        module.startTime = startTime.time
        module.endTime = endTime
        assessmentResult.moduleResult = module
        return assessmentResult
    }

}
