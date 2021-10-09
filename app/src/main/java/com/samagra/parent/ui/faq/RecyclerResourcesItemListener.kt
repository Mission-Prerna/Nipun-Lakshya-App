package com.samagra.parent.ui.faq

import android.widget.TextView
import com.samagra.parent.data.KnowledgeResourceData

interface RecyclerResourcesItemListener {
    fun openResourcesDetails(
        resourcesItem: KnowledgeResourceData,
        faqTitle: TextView,
        faqDocumentLink: TextView
    )
}