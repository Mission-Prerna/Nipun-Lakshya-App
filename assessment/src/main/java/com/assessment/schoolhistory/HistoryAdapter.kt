package com.assessment.schoolhistory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.assessment.R
import com.assessment.databinding.ItemSchoolHistoryBinding
import com.data.db.models.entity.AssessmentSchoolHistory
import com.data.db.models.helper.AssessmentSchool
import com.data.models.history.AssessmentSchoolPlaceHolder


class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ItemViewHolder>() {

    private val itemList: MutableList<AssessmentSchool> = mutableListOf()

    inner class ItemViewHolder(val binding: ItemSchoolHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_school_history,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val historyItem = itemList[position]
        if (historyItem is AssessmentSchoolHistory) {
            holder.binding.monthTv.text = historyItem.period
            holder.binding.totalStudentsCountTv.text = historyItem.total.toString()
            holder.binding.totalAssessmentsCountTv.text = historyItem.assessed.toString()
            holder.binding.nipunStudentCountTv.text = historyItem.successful.toString()
        } else if(historyItem is AssessmentSchoolPlaceHolder) {
            holder.binding.monthTv.text = historyItem.period
            holder.binding.totalStudentsCountTv.text = historyItem.total
            holder.binding.totalAssessmentsCountTv.text = historyItem.assessed
            holder.binding.nipunStudentCountTv.text = historyItem.successful
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: MutableList<AssessmentSchool>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

}