package com.data.db

object DbHelper {
    lateinit var db: NLDatabase

    fun isSyncingRequired() : Boolean {
        return db.getAssessmentSubmissionDao().getSubmissions().isNotEmpty() || db.getSchoolSubmissionDao().getSubmissions().isNotEmpty()
    }

}