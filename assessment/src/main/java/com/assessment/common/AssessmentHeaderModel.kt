package com.assessment.common

data class AssessmentHeaderModel(
    val name: String,
    val identifier: Long,
    val date: String,
    val mentorType: String,
    val mentorName: String
)
