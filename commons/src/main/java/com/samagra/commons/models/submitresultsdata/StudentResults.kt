package com.samagra.commons.models.submitresultsdata

import com.google.gson.annotations.SerializedName
import com.samagra.commons.models.Results
import java.io.Serializable

data class StudentResults(
    @SerializedName("student_name") var studentName: String? = null,
    @SerializedName("competency_id") var competencyId: Int? = null,
    @SerializedName("module") var module: String? = null,
    @SerializedName("end_time") var endTime: Long? = null,
    @SerializedName("is_passed") var isPassed: Boolean? = null,
    @SerializedName("start_time") var startTime: Long? = null,
    @SerializedName("statement") var statement: String? = null,
    @SerializedName("achievement") var achievement: Int? = null,
    @SerializedName("total_questions") var totalQuestions: Int? = null,
    @SerializedName("success_criteria") var successCriteria: Int? = null,
    @SerializedName("session_completed") var sessionCompleted: Boolean? = null,
    @SerializedName("is_network_active") var isNetworkActive: Boolean? = null,
    @SerializedName("workflow_ref_id") var workflowRefId: String? = null,
    @SerializedName("total_time_taken") var totalTimeTaken: Int? = null,
    @SerializedName("student_session") var studentSession: String? = null,
    @SerializedName("odk_results") var odkResults: ArrayList<Results> = arrayListOf()
): Serializable