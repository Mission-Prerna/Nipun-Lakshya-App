package com.assessment.submission

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assessment.databinding.AbsentStudentItemBinding

class AbsentStudentAdapter : ListAdapter<String, AbsentStudentAdapter.StudentViewHolder>(
    StudentDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = AbsentStudentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val studentName = getItem(position)
        holder.bind("${position+1}. ".plus(studentName))
    }

    inner class StudentViewHolder(private val binding: AbsentStudentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(studentName: String) {
            binding.tvStudentName.text = studentName
        }
    }
}

class StudentDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}

