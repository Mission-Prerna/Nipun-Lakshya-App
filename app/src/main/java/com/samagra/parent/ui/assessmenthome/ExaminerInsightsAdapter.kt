package com.samagra.parent.ui.assessmenthome
import android.animation.ValueAnimator
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.data.db.models.ExaminerPerformanceInsightsItem
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemInsightBinding
import com.samagra.parent.databinding.ItemExaminerPerformanceInsightsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExaminerInsightsAdapter : RecyclerView.Adapter<ExaminerInsightsAdapter.InsightsViewHolder>() {

    private val itemList: MutableList<ExaminerPerformanceInsightsItem> = mutableListOf()

    inner class InsightsViewHolder(binding: ItemExaminerPerformanceInsightsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val periodTextView = binding.periodTextView
        val insightsLayout = binding.insightsLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemExaminerPerformanceInsightsBinding.inflate(inflater, parent, false)
        return InsightsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InsightsViewHolder, position: Int) {
        val item = itemList[position]

        holder.periodTextView.text = item.period
        val paint = holder.periodTextView.paint
        holder.periodTextView.paint.flags = paint.flags or Paint.UNDERLINE_TEXT_FLAG
        holder.insightsLayout.removeAllViews()

        val layoutInflater = LayoutInflater.from(holder.insightsLayout.context)

        for (insight in item.insights) {

            if (position == 0) {
                val space = layoutInflater.inflate(
                    R.layout.space_holder_layout,
                    holder.insightsLayout,
                    false
                )
                holder.insightsLayout.addView(space)
            }

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

        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.horizontal_scroll_animation)
        if (position == itemCount - 1) {
            CoroutineScope(Dispatchers.Main).launch{
                delay(400)
                holder.insightsLayout.startAnimation(animation)
            }
        } else {
            holder.insightsLayout.clearAnimation()
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun updateItems(newItems: List<ExaminerPerformanceInsightsItem>) {
        val diffResult = DiffUtil.calculateDiff(InsightsDiffCallback(itemList, newItems))
        itemList.clear()
        itemList.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    private class InsightsDiffCallback(
        private val oldList: List<ExaminerPerformanceInsightsItem>,
        private val newList: List<ExaminerPerformanceInsightsItem>
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