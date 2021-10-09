package com.samagra.parent.ui.finalresults

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemBoloFinalResultNlBinding
import com.samagra.parent.ui.competencyselection.CompetencyDatum
import java.util.*

class FinalResultsNlAdapter(
    private val resultMap: List<Pair<CompetencyDatum, Int>>,
    private val noOfStudent: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding: ItemBoloFinalResultNlBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_bolo_final_result_nl, parent, false
        )
        return BOLOViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BOLOViewHolder).bind(resultMap[position],noOfStudent)
    }

    override fun getItemCount() = resultMap.size

    open class BOLOViewHolder(val binding: ItemBoloFinalResultNlBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Pair<CompetencyDatum, Int>, noOfStudent: Int) {
            binding.tvCompetency.text = data.first.learningOutcome
            binding.tvIsNipun.text = "${data.second}/$noOfStudent"
        }
    }
}
