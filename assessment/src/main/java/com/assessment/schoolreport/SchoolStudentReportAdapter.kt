package com.assessment.schoolreport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assessment.R
import com.assessment.databinding.SchoolStudentReportItemBinding
import com.assessment.schoolreport.SchoolStudentReportAdapter.SchoolStudentReportVH
import com.assessment.schoolreport.data.AssessmentStatus
import com.assessment.schoolreport.data.SchoolStudentReport
import com.samagra.commons.extensions.hide
import com.samagra.commons.extensions.show

class SchoolStudentReportAdapter : ListAdapter<SchoolStudentReport, SchoolStudentReportVH>(
    SchoolStudentReportDC()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SchoolStudentReportVH(
        SchoolStudentReportItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: SchoolStudentReportVH, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<SchoolStudentReport>) {
        submitList(data)
    }

    inner class SchoolStudentReportVH(val binding: SchoolStudentReportItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SchoolStudentReport) = with(binding) {
            tvName.text = item.name
            tvClass.text = item.grade
            tvRoll.text = item.rollNumber
            when (item.status) {
                AssessmentStatus.PENDING -> {
                    ivStatus.hide()
                    tvStatus.text = "----"
                    tvStatus.setTextColor(
                        ContextCompat.getColor(
                            tvStatus.context,
                            R.color.black_545454
                        )
                    )
                }

                AssessmentStatus.SUCCESSFUL -> {
                    ivStatus.show()
                    ivStatus.setImageResource(R.drawable.ic_report_success)
                    tvStatus.text = tvStatus.context.getString(R.string.is_successful)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(
                            tvStatus.context,
                            R.color.green_500
                        )
                    )
                }

                AssessmentStatus.UNSUCCESSFUL -> {
                    ivStatus.show()
                    ivStatus.setImageResource(R.drawable.ic_report_failure)
                    tvStatus.text = tvStatus.context.getString(R.string.is_unsuccessful)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(
                            tvStatus.context,
                            R.color.red_500
                        )
                    )
                }
            }
        }
    }

    private class SchoolStudentReportDC : DiffUtil.ItemCallback<SchoolStudentReport>() {
        override fun areItemsTheSame(
            oldItem: SchoolStudentReport,
            newItem: SchoolStudentReport
        ) = oldItem.rollNumber == newItem.rollNumber

        override fun areContentsTheSame(
            oldItem: SchoolStudentReport,
            newItem: SchoolStudentReport
        ) = oldItem == newItem
    }
}