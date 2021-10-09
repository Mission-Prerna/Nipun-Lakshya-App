package com.assessment.studentselection

import androidx.lifecycle.ViewModel
import com.assessment.R
import com.data.db.models.helper.StudentWithAssessmentHistory
import com.data.models.submissions.StudentNipunStates
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentSharedViewModel: ViewModel() {

    fun convertDateToString(inputDate: Date?): String {
        val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)
        return outputFormat.format(inputDate)
    }

    fun getColorForState(studentStatus: String?): Int {
        return when (studentStatus) {
            StudentNipunStates.pending -> R.color.color_e2e2e2
            StudentNipunStates.pass -> R.color.color_72ba86
            StudentNipunStates.fail -> R.color.color_c98a7a
            else -> R.color.white
        }
    }

    fun getStudentRollNoText(studentItem: StudentWithAssessmentHistory) = "(${studentItem.rollNo})"

    fun getLastAssessmentDateText(studentItem: StudentWithAssessmentHistory) = "आखरी आकलन : ${convertDateToString(studentItem.last_assessment_date)}"

}