package com.assessment.schoollist.model

import com.assessment.R
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.data.models.submissions.StudentNipunStates

data class SchoolUiModel(
    val schoolStatusHistory: SchoolDetailsWithReportHistory,
    val isValidCycle: Boolean
) {

    val actionBtnTextId: Int
    val isVisitTaken: Boolean
    val actionBtnTextColor: Int
    val showActionButton: Boolean

    init {
        val schoolStatus = schoolStatusHistory.status
        if (schoolStatus.isNullOrEmpty() || schoolStatus.equals(StudentNipunStates.pending, true)) {
            actionBtnTextId = R.string.do_assessment
            actionBtnTextColor = R.color.black
            isVisitTaken = false
            showActionButton = isValidCycle
        } else {
            isVisitTaken = true
            showActionButton = true
            if (schoolStatus.equals(StudentNipunStates.pass, true)) {
                actionBtnTextId = R.string.nipun_text
                actionBtnTextColor = R.color.color_nipun
            } else {
                actionBtnTextId = R.string.not_nipun_text
                actionBtnTextColor = R.color.color_not_nipun
            }
        }
    }

}
