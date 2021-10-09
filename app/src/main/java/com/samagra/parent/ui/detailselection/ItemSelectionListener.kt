package com.samagra.parent.ui.detailselection

interface ItemSelectionListener<T> {
    fun onSelectionChange(pos: Int, item: T)
}