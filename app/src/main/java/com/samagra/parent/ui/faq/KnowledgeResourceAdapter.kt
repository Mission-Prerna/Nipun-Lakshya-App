package com.samagra.parent.ui.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.samagra.parent.R
import com.samagra.parent.data.KnowledgeResourceData

class KnowledgeResourceAdapter(
    private val getResourcesList: List<KnowledgeResourceData>,
    private val onUserCallbackListener: RecyclerResourcesItemListener?
) : RecyclerView.Adapter<KnowledgeResourceAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_resources_item, parent, false)
        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return getResourcesList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val resourcesItem = getResourcesList[position]
        holder.faqQuesName.text ="प्र. ${position + 1} :"
        holder.faqAnsName.text = "उ. ${position + 1} :"
        holder.faqTitle.text = resourcesItem.ques
        holder.faqDocumentLink.text = resourcesItem.ans
        // holder.lLParent.setOnClickListener {
//            onUserCallbackListener?.openResourcesDetails( resourcesItem,holder.faqTitle,holder.faqDocumentLink)
        //}

    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val faqTitle: TextView = itemView.findViewById(R.id.faq_title)
        val faqQuesName: TextView = itemView.findViewById(R.id.faq_ques_name)
        val faqAnsName: TextView = itemView.findViewById(R.id.faq_ans_name)
        val faqDocumentLink: TextView = itemView.findViewById(R.id.faq_doc)
    }
}