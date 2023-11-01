package com.assessment.studentselection

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assessment.R
import com.assessment.databinding.ItemNipunStatesBinding
import com.data.db.models.Summary

class NipunStatesAdapter : ListAdapter<Summary, NipunStatesAdapter.NipunStatesViewHolder>(SummaryDiffCallback()) {

    inner class NipunStatesViewHolder(val binding: ItemNipunStatesBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Summary>() {
        override fun areItemsTheSame(oldItem: Summary, newItem: Summary): Boolean {
            return oldItem.identifier == newItem.identifier
        }

        override fun areContentsTheSame(
            oldItem: Summary,
            newItem: Summary
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NipunStatesViewHolder {
        return NipunStatesViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_nipun_states,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NipunStatesViewHolder, position: Int) {
        val studentsNipunSummary = getItem(position)
        holder.binding.apply {
            tvNipunText.text = studentsNipunSummary.label.plus(": ")
            tvNipunCount.text = studentsNipunSummary.count.toString()

            val fillColorCode = studentsNipunSummary.colour
            val fillColor = Color.parseColor(fillColorCode)

            val backgroundDrawable = ivNipunStateColor.background as GradientDrawable
            backgroundDrawable.setColor(fillColor)

            root.setOnClickListener {
                onItemClickListener?.invoke(studentsNipunSummary)
            }
        }
    }

    private var onItemClickListener: ((Summary) -> Unit)? = null

    fun setOnItemClickListener(listener: (Summary) -> Unit) {
        onItemClickListener = listener
    }
}

class SummaryDiffCallback : DiffUtil.ItemCallback<Summary>() {
    override fun areItemsTheSame(oldItem: Summary, newItem: Summary): Boolean {
        return oldItem.label == newItem.label
    }

    override fun areContentsTheSame(oldItem: Summary, newItem: Summary): Boolean {
        return oldItem == newItem
    }
}
