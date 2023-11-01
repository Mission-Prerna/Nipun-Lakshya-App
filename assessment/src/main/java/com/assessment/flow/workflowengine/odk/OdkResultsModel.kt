package com.assessment.flow.workflowengine.odk

import com.assessment.R


data class OdkResultsModel(
    var question: String,
    var result: String,
    var isCorrect: Boolean = false,
    var colorResId: Int = R.color.red_500
) {
    lateinit var r: String

    init {
        print("")
    }

    constructor(s: String, r: String) : this(s, "", false, 0) {
        this.r = r
    }
}