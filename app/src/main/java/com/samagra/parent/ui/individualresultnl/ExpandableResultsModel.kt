package com.samagra.parent.ui.individualresultnl

import com.samagra.commons.models.Results

data class ExpandableResultsModel(
    var competencyName: String? = null,
    var isNipun: Boolean? = null,
    var studentResultList: List<Results> = ArrayList()
)