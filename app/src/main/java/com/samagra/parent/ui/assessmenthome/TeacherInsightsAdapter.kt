package com.samagra.parent.ui.assessmenthome
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.data.db.models.TeacherPerformanceInsightsItem
import com.samagra.parent.databinding.ItemInsightBinding
import com.samagra.parent.databinding.ItemTeacherPerformanceInsightsBinding

class TeacherInsightsAdapter : RecyclerView.Adapter<TeacherInsightsAdapter.InsightsViewHolder>() {

    private val itemList: MutableList<TeacherPerformanceInsightsItem> = mutableListOf()

    inner class InsightsViewHolder(binding: ItemTeacherPerformanceInsightsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val periodTextView = binding.periodTextView
        val insightsLayout = binding.insightsLayout
        val viewDivider = binding.viewDivider
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTeacherPerformanceInsightsBinding.inflate(inflater, parent, false)
        return InsightsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InsightsViewHolder, position: Int) {
        val item = itemList[position]

        if (position == 0) {
            holder.viewDivider.visibility = View.VISIBLE
        } else {
            holder.viewDivider.visibility = View.GONE
        }

        holder.periodTextView.text = item.period
        val paint = holder.periodTextView.paint
        holder.periodTextView.paint.flags = paint.flags or Paint.UNDERLINE_TEXT_FLAG
        holder.insightsLayout.removeAllViews()

        for (insight in item.insights) {
            val insightBinding = ItemInsightBinding.inflate(
                LayoutInflater.from(holder.insightsLayout.context),
                holder.insightsLayout,
                true
            )

            insightBinding.countTextView.text = insight.count.toString()

            val splitLabel = insight.label.split(" ")
            val formattedLabel = when (splitLabel.size) {
                2 -> {
                    "${splitLabel[0]}\n${splitLabel[1]}"
                }
                1 -> {
                    "${splitLabel[0]}\n"
                }
                else -> {
                    insight.label
                }
            }

            insightBinding.labelTextView.text = formattedLabel
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun updateItems(newItems: List<TeacherPerformanceInsightsItem>) {
        val diffResult = DiffUtil.calculateDiff(InsightsDiffCallback(itemList, newItems))
        itemList.clear()
        itemList.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    private class InsightsDiffCallback(
        private val oldList: List<TeacherPerformanceInsightsItem>,
        private val newList: List<TeacherPerformanceInsightsItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].period == newList[newItemPosition].period
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}