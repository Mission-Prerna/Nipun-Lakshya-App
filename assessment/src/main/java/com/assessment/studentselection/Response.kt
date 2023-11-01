package com.assessment.studentselection

import com.assessment.studentselection.data.StudentList

sealed class Response {
    object Loading : Response()
    class Error(val t: Throwable) : Response()
    class Success(val studentList: StudentList?) : Response()
    object Empty : Response()
}