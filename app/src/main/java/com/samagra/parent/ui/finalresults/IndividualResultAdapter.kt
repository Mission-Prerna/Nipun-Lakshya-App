package com.samagra.parent.ui.finalresults

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.samagra.parent.R
import com.samagra.parent.databinding.ListGroupBinding
import com.samagra.parent.ui.individualresultnl.ExpandableResultsModel

class IndividualResultAdapter(
    arrayList: ArrayList<ExpandableResultsModel>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mData: ArrayList<ExpandableResultsModel> = arrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ListGroupBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_group, parent, false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ResultViewHolder).bind(mData[position])
    }

    override fun getItemCount() = mData.size

    open class ResultViewHolder(val binding: ListGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ExpandableResultsModel) {
            binding.expandedListItem.text = data.competencyName
            if (data.isNipun == true) {
                setTextColor(R.color.green_500, binding.tvNipun)
                binding.tvNipun.text = binding.tvNipun.context.getString(R.string.nipun)
            } else if (data.isNipun == false) {
                setTextColor(R.color.red_500, binding.tvNipun)
                binding.tvNipun.text = binding.tvNipun.context.getString(R.string.not_nipun)
            } else {
                setTextColor(R.color.black, binding.tvNipun)
                binding.tvNipun.text =
                    binding.tvNipun.context.getString(R.string.session_not_completed)
            }
        }

        private fun setTextColor(colorResId: Int, tvAnswer: TextView) {
            tvAnswer.setTextColor(
                ContextCompat.getColor(
                    tvAnswer.context,
                    colorResId
                )
            )
        }
    }
}
