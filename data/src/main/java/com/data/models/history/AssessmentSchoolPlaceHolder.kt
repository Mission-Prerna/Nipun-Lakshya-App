package com.data.models.history

import com.data.db.models.helper.AssessmentSchool

class AssessmentSchoolPlaceHolder(
    val total: String,
    var assessed: String,
    var successful: String,
    val period: String,
) : AssessmentSchool