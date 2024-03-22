package com.assessment.flow.scoreboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assessment.R
import com.assessment.databinding.ScorecardItemBinding
import com.data.models.ui.ScorecardData

private const val COLOR_PASSED = "#72BA86"
private const val COLOR_FAILED = "#CA8A77"

class ScoreboardAdapter(private val onItemClickListener: ((ScorecardData) -> Unit)? = null) :
    ListAdapter<ScorecardData, ScoreboardAdapter.ScorecardViewHolder>(ScorecardDataDiffCallback()) {

    inner class ScorecardViewHolder(val binding: ScorecardItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScorecardViewHolder {
        return ScorecardViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.scorecard_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ScorecardViewHolder, position: Int) {
        val scorecardItem = getItem(position)
        holder.binding.apply {
            tvCompetencyDescription.text = scorecardItem.competencyDescription
            tvCompetencyScore.text = scorecardItem.competencyScore
            tvCompetencyScoreDescription.text = scorecardItem.competencyScoreDescription
            scorecardItemCard.setCardBackgroundColor(
                ColorStateList.valueOf(Color.parseColor(if (scorecardItem.isPassed) COLOR_PASSED else COLOR_FAILED))
            )
            root.setOnClickListener {
                onItemClickListener?.let { it(scorecardItem) }
            }
        }
    }
}

class ScorecardDataDiffCallback : DiffUtil.ItemCallback<ScorecardData>() {
    override fun areItemsTheSame(oldItem: ScorecardData, newItem: ScorecardData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ScorecardData, newItem: ScorecardData): Boolean {
        return oldItem.competencyDescription == newItem.competencyDescription
    }
}