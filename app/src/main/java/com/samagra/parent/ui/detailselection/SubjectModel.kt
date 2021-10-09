package com.samagra.parent.ui.detailselection

data class SubjectModel @JvmOverloads constructor(
    var title: String,
    var tag: String? = null,
    var imgid: Int = 0,
    var isSelected: Boolean = false
)