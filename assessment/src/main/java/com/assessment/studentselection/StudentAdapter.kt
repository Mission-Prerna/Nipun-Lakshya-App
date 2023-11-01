package com.assessment.studentselection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.assessment.R
import com.assessment.databinding.ItemStudentAnonymousBinding
import com.assessment.databinding.ItemStudentDetailsBinding
import com.data.db.models.helper.StudentWithAssessmentHistory
import com.data.models.submissions.StudentNipunStates
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StudentAdapter(
    private var currentList: List<StudentWithAssessmentHistory>,
    val isExaminer: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mListener: OnItemClickListener? = null

    val VIEW_STUDENT = 0
    val VIEW_ANONYMOUS_ASSESSMENT = 1

    fun addAll(list: List<StudentWithAssessmentHistory>) {
        currentList = list
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    inner class StudentViewHolder(val binding: ItemStudentDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class AnonymousStudentViewHolder(val binding: ItemStudentAnonymousBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_ANONYMOUS_ASSESSMENT) {
            return AnonymousStudentViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context), R.layout.item_student_anonymous,
                    parent,
                    false
                )
            )
        }
        return StudentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_student_details,
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position].isPlaceHolderStudent) VIEW_ANONYMOUS_ASSESSMENT else VIEW_STUDENT
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    private var onItemClickListener: ((StudentWithAssessmentHistory) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val studentItem = currentList[position]
        when (holder) {
            is StudentViewHolder -> {
                holder.binding.apply {
                    val context = root.context
                    tvStudentName.text = studentItem.name
                    tvStudentRollNo.text = "(".plus(studentItem.rollNo.toString()).plus(")")
                    tvStudentLastAssessmentDate.text =
                        "आखरी आकलन : ".plus(convertDateToString(studentItem.last_assessment_date))
                    cardColor.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    when (studentItem.status) {
                        StudentNipunStates.pending -> cardColor.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.color_e2e2e2
                            )
                        )

                        StudentNipunStates.pass -> cardColor.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.color_72ba86
                            )
                        )

                        StudentNipunStates.fail -> cardColor.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.color_c98a7a
                            )
                        )
                    }

                    val constraintSet = ConstraintSet()

                    if (studentItem.status.equals(StudentNipunStates.pending)) {
                        tvStudentLastAssessmentDate.visibility = View.GONE

                        constraintSet.clone(clStudentDetails)

                        constraintSet.connect(
                            tvStudentRollNo.id,
                            ConstraintSet.BOTTOM,
                            ConstraintSet.PARENT_ID,
                            ConstraintSet.BOTTOM
                        )
                        constraintSet.applyTo(clStudentDetails)

                    } else if (studentItem.status.equals(StudentNipunStates.pass) || studentItem.status.equals(
                            StudentNipunStates.fail
                        )
                    ) {
                        tvStudentLastAssessmentDate.visibility = View.VISIBLE

                        constraintSet.clone(clStudentDetails)

                        constraintSet.connect(
                            tvStudentRollNo.id,
                            ConstraintSet.BOTTOM,
                            R.id.tvStudentLastAssessmentDate,
                            ConstraintSet.TOP
                        )
                        constraintSet.applyTo(clStudentDetails)
                    }

                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH) + 1 // get current month number

                    if (isExaminer) {
                        btTakeAssessment.visibility =
                            if (studentItem.status == StudentNipunStates.pending) View.VISIBLE else View.GONE
                    } else {
                        if (studentItem.month == month) {
                            btTakeAssessment.visibility = View.VISIBLE
                        } else {
                            btTakeAssessment.visibility = View.INVISIBLE
                        }
                    }

                    btTakeAssessment.setOnClickListener {
                        mListener?.onItemClick(studentItem)
                    }
                    root.setOnClickListener {
                        onItemClickListener?.let { it(studentItem) }
                    }
                }
            }

            is AnonymousStudentViewHolder -> {
                holder.binding.takeAssessmentBtn.setOnClickListener {
                    mListener?.onItemClick(studentItem)
                }
            }
        }
    }

    fun convertDateToString(inputDate: Date?): String {
        val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)
        return outputFormat.format(inputDate)
    }

    fun setOnItemClickListener(listener: (StudentWithAssessmentHistory) -> Unit) {
        onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(Student: StudentWithAssessmentHistory)
    }
}