package com.assessment.submission

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.assessment.databinding.AbsentStudentsDialogFragmentBinding

class AbsentStudentsDialogFragment : DialogFragment() {

    private var studentList: List<String> = mutableListOf()
    private var positiveClickListener: OnClickListener? = null
    private var negativeClickListener: OnClickListener? = null
    private lateinit var binding: AbsentStudentsDialogFragmentBinding
    private val adapter = AbsentStudentAdapter()

    interface OnClickListener {
        fun onClick()
    }

    companion object {
        fun newInstance(studentList: List<String>): AbsentStudentsDialogFragment {
            val fragment = AbsentStudentsDialogFragment()
            fragment.studentList = studentList//.subList(0,3)
            return fragment
        }
    }

    fun setPositiveClickListener(listener: OnClickListener) {
        positiveClickListener = listener
    }

    fun setNegativeClickListener(listener: OnClickListener) {
        negativeClickListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AbsentStudentsDialogFragmentBinding.inflate(inflater, container, false)

        val recyclerView = binding.rvAbsentStudents
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.submitList(studentList)

        val positiveButton = binding.ctaButton1
        val negativeButton = binding.ctaButton2

        positiveButton.apply {
            setOnClickListener {
                positiveClickListener?.onClick()
                dismiss()
            }
            text = "हाँ ! ${studentList.size}  विद्यार्थी अनुपस्तिथ है !"
        }

        negativeButton.apply {
            setOnClickListener {
                negativeClickListener?.onClick()
                dismiss()
            }
            text = "नहीं ! पीछे जाये !"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        adjustRecyclerViewHeight(binding.rvAbsentStudents)
    }


    override fun onStart() {
        super.onStart()
        dialog?.let {
//            val screenHeight = resources.displayMetrics.heightPixels
//            val maxHeightPercentage = 0.5 // Adjust this percentage as needed (e.g., 0.5 for 50% of the screen height)
//            val maxHeight = (screenHeight * maxHeightPercentage).toInt()
//            val width = ViewGroup.LayoutParams.WRAP_CONTENT
//            it.window?.setLayout(width, maxHeight)
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun adjustRecyclerViewHeight(recyclerView: RecyclerView) {
        val displayMetrics = resources.displayMetrics
        val height = displayMetrics.heightPixels
        val maxHeight = height / 2
        val layoutParams = recyclerView.layoutParams
        layoutParams.height = maxHeight
        recyclerView.layoutParams = layoutParams
    }
}
