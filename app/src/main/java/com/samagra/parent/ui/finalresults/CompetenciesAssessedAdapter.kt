package com.samagra.parent.ui.finalresults

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemReadOnlyCompetencyBinding
import com.samagra.parent.ui.competencyselection.CompetencyDatum

class CompetenciesAssessedAdapter(
   private val competencyList: ArrayList<CompetencyDatum>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemReadOnlyCompetencyBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_read_only_competency, parent, false
        )
        return CompetenciesAssessedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CompetenciesAssessedViewHolder).bind(competencyList[position])
    }

    override fun getItemCount() = competencyList.size

    open class CompetenciesAssessedViewHolder(val binding: ItemReadOnlyCompetencyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CompetencyDatum) {
            binding.tvCompetency.text = data.learningOutcome
            if (data.subjectId == 1) {
                binding.ivSubjectImage.setImageResource(R.drawable.ic_math)
            } else {
                binding.ivSubjectImage.setImageResource(R.drawable.ic_hindi)
            }
        }
    }
}