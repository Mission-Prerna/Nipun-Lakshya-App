package com.assessment.flow.scoreboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.assessment.R
import com.assessment.databinding.ScorecardItemBinding
import com.data.models.ui.ScorecardData

class ScoreboardAdapter : RecyclerView.Adapter<ScoreboardAdapter.ScorecardViewHolder>() {

    inner class ScorecardViewHolder(val binding: ScorecardItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<ScorecardData>() {
        override fun areItemsTheSame(oldItem: ScorecardData, newItem: ScorecardData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ScorecardData, newItem: ScorecardData): Boolean {
            return oldItem.competencyDescription == newItem.competencyDescription
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScorecardViewHolder {
        return ScorecardViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.scorecard_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((ScorecardData) -> Unit)? = null

    override fun onBindViewHolder(holder: ScorecardViewHolder, position: Int) {
        val scorecardItem = differ.currentList[position]
        holder.binding.apply {
            tvCompetencyDescription.text = scorecardItem.competencyDescription
            tvCompetencyScore.text = scorecardItem.competencyScore
            tvCompetencyScoreDescription.text = scorecardItem.competencyScoreDescription
            scorecardItemCard.setCardBackgroundColor(
                ColorStateList.valueOf(Color.parseColor(if (scorecardItem.isPassed) "#72BA86" else "#CA8A77"))
            )
            root.setOnClickListener {
                onItemClickListener?.let { it(scorecardItem) }
            }
        }
    }


    fun setOnItemClickListener(listener: (ScorecardData) -> Unit) {
        onItemClickListener = listener
    }
}