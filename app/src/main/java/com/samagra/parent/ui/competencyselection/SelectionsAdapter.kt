package com.samagra.parent.ui.competencyselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemSelectionBinding

class SelectionsAdapter(arrayList: ArrayList<String>) :
    RecyclerView.Adapter<SelectionsAdapter.ViewHolder>() {
    private var mData: ArrayList<String> = arrayList

    open class ViewHolder(val binding: ItemSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSelectionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_selection, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvSelection.text = mData.get(position)
    }

    override fun getItemCount() = mData.size


    fun isLastRowAndShouldBeCentered(pos: Int): Boolean {
        return if (pos == mData.size - 1 && mData.size % 2 != 0) {
            true
        } else {
            false
        }
    }

}
