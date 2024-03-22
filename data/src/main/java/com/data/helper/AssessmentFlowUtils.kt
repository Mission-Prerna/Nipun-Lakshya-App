package com.data.helper

import android.content.Context
import com.data.FlowType
import com.data.R
import com.data.db.models.entity.AssessmentState
import com.data.models.stateresult.AssessmentStateResult
import com.data.models.stateresult.ModuleResult
import com.samagra.commons.AppProperties
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AssessmentFlowUtils {

    fun getDummyAssessmentResult(
        state: AssessmentState,
        grade: Int,
        subject: String
    ): AssessmentStateResult {
        val assessmentStateResult =
            AssessmentStateResult()
        assessmentStateResult.studentId = state.studentId
        if (state.flowType == FlowType.ODK) {
            assessmentStateResult.workflowRefId = "-"
            assessmentStateResult.grade = grade
            assessmentStateResult.subject = subject
            val moduleResult = ModuleResult()
            moduleResult.isNetworkActive = NetworkStateManager.instance!!.networkConnectivityStatus
            moduleResult.module = CommonConstants.ODK
            moduleResult.achievement = 0
            moduleResult.isPassed = false // false case

            moduleResult.sessionCompleted = false
            moduleResult.appVersionCode = AppProperties.versionCode
            moduleResult.totalQuestions = 0
            moduleResult.successCriteria = 0
            assessmentStateResult.moduleResult = moduleResult
            assessmentStateResult.moduleResult.startTime = Date().time
            assessmentStateResult.moduleResult.endTime = Date().time
        } else {
            assessmentStateResult.workflowRefId = "0"
            assessmentStateResult.grade = grade
            assessmentStateResult.subject = subject
            val moduleResult = ModuleResult()
            moduleResult.module = CommonConstants.BOLO
            moduleResult.isNetworkActive = NetworkStateManager.instance!!.networkConnectivityStatus
            moduleResult.achievement = 0
            moduleResult.isPassed = false // false case
            moduleResult.sessionCompleted = false
            moduleResult.appVersionCode = AppProperties.versionCode
            moduleResult.totalQuestions = 0
            moduleResult.successCriteria = 0
            assessmentStateResult.moduleResult = moduleResult
            assessmentStateResult.moduleResult.startTime = Date().time
            assessmentStateResult.moduleResult.endTime = Date().time

        }
        return assessmentStateResult
    }

    fun getMonthText(context: Context, month: Int): String {
        return when (month) {
            1 -> context.getString(R.string.month_january)
            2 -> context.getString(R.string.month_february)
            3 -> context.getString(R.string.month_march)
            4 -> context.getString(R.string.month_april)
            5 -> context.getString(R.string.month_may)
            6 -> context.getString(R.string.month_june)
            7 -> context.getString(R.string.month_july)
            8 -> context.getString(R.string.month_august)
            9 -> context.getString(R.string.month_september)
            10 -> context.getString(R.string.month_october)
            11 -> context.getString(R.string.month_november)
            12 -> context.getString(R.string.month_december)
            else -> ""
        }
    }

    fun getCurrentMonthNameInHindi() : String {
        val calendar = Calendar.getInstance();
        val hindiLocale = Locale("hi", "IN"); // Create a Hindi locale
        val monthFormat = SimpleDateFormat("MMMM", hindiLocale);
        return monthFormat.format(calendar.time);
    }
}