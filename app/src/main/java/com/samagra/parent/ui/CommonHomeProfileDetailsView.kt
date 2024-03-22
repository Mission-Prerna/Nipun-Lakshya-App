package com.samagra.parent.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.samagra.parent.databinding.CommonHomeProfileDetailsViewBinding
import com.samagra.parent.ui.assessmenthome.AssessmentHomeVM

private var binding: CommonHomeProfileDetailsViewBinding? = null

class CommonHomeProfileDetailsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    fun setViewModel(myViewModel: AssessmentHomeVM, hideUdise: Boolean) {
        if (binding == null) {
            binding = CommonHomeProfileDetailsViewBinding.inflate(LayoutInflater.from(context), this, true)
        }
        binding?.assessmentHomeVm = myViewModel
        if (hideUdise) {
            binding?.tvDesignation?.visibility = View.GONE
        } else {
            binding?.tvDesignation?.visibility = View.VISIBLE
        }
    }

    fun setBlockVisibility(visibility: Int) {
        binding?.groupBlock?.visibility = visibility
    }

    fun setBindingToNull(){
        binding = null
    }
}