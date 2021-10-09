package com.samagra.workflowengine.odk

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.samagra.commons.models.Results
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemOdkResultBinding

class ODKResultsAdapter(arrayList: ArrayList<Results>) :
    RecyclerView.Adapter<ODKResultsAdapter.ViewHolder>() {
    private var mData: ArrayList<Results> = arrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemOdkResultBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_odk_result, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    override fun getItemCount() = mData.size

    open class ViewHolder(val binding: ItemOdkResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Results) {
            val questionNumber = data.question.substringAfter(" ").trim()
            binding.tvQuestion.text = "${binding.tvQuestion.context.getString(R.string.question)} $questionNumber"
            val result = if (data.answer == "0") {
                setTextColor(R.color.red_500)
                binding.tvQuestion.context.getString(R.string.wrong)
            } else {
                setTextColor(R.color.green_500)
                binding.tvResult.context.getString(R.string.correct)
            }
            binding.tvResult.text = result
        }

        private fun setTextColor(@ColorRes colorResId: Int) {
            binding.tvResult.setTextColor(
                ContextCompat.getColor(
                    binding.tvResult.context,
                    colorResId
                )
            )
        }
    }
}
