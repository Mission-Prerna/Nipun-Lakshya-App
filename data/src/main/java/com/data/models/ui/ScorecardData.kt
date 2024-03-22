package com.data.models.ui

import java.io.Serializable

data class ScorecardData(
    val competencyDescription: String,
    val competencyScore: String,
    val competencyScoreDescription: String,
    val isPassed: Boolean
) : Serializable
