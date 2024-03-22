package com.samagra.parent.ui.finalresults

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.samagra.commons.getPercentage
import com.samagra.commons.utils.CommonConstants.BOLO
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemBoloFinalResultBinding
import com.samagra.parent.databinding.ItemCombinedFinalResultBinding
import com.samagra.parent.databinding.ItemOdkFinalResultBinding
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData


class FinalResultsAdapter(
    arrayList: ArrayList<StudentsAssessmentData>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_BOLO: Int = 0
    private val TYPE_ODK: Int = 1
    private val TYPE_COMBINED: Int = 2
    private var mData: ArrayList<StudentsAssessmentData> = arrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_BOLO -> {
                val binding: ItemBoloFinalResultBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_bolo_final_result, parent, false
                )
                BOLOViewHolder(binding)
            }
            TYPE_ODK -> {
                val binding: ItemOdkFinalResultBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_odk_final_result, parent, false
                )
                ODKViewHolder(binding)
            }
            TYPE_COMBINED -> {
                val binding: ItemCombinedFinalResultBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_combined_final_result, parent, false
                )
                CombinedViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (mData[position].viewType) {
            BOLO -> {
                (holder as BOLOViewHolder).bind(mData[position])
            }
            ODK -> {
                (holder as ODKViewHolder).bind(mData[position])
            }
            "combined" -> {
                (holder as CombinedViewHolder).bind(mData[position])
            }
        }
    }

    override fun getItemCount() = mData.size

    open class ODKViewHolder(val binding: ItemOdkFinalResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: StudentsAssessmentData) {
            val moduleResult = data.studentResults.moduleResult
            setTextColor(R.color.black)
            val studentNumber = data.studentResults.studentName.substringAfter(" ").trim()
            binding.tvQuestion.text = "${binding.tvResult.context.getString(R.string.student)} $studentNumber"
            if (moduleResult.sessionCompleted) {
                val percentage = getPercentage(moduleResult.achievement, moduleResult.totalQuestions)
                binding.tvResult.text =
                    "${moduleResult.achievement}/${moduleResult.totalQuestions} ($percentage%)"
            } else {
                binding.tvResult.text =
                    binding.tvResult.context.getString(R.string.session_not_completed)
            }
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

    open class BOLOViewHolder(val binding: ItemBoloFinalResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StudentsAssessmentData) {
            val moduleResult = data.studentResults.moduleResult
            val studentNumber = data.studentResults.studentName.substringAfter(" ").trim()
            binding.tvStudent.text = "${binding.tvStudent.context.getString(R.string.student)} $studentNumber"
            if (moduleResult.sessionCompleted) {
                binding.tvResult.text = moduleResult.achievement.toString()
            } else {
                binding.tvResult.text =
                    binding.tvResult.context.getString(R.string.session_not_completed)
            }
            if (moduleResult.achievement >= moduleResult.successCriteria) {
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(
                        binding.tvResult.context,
                        R.color.green_500
                    )
                )
            } else {
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(
                        binding.tvResult.context,
                        R.color.dark_yellow_bf9000
                    )
                )
            }
            /*Log.e(
                "-->>",
                "final result adapter data 1: ${data.studentResults.studentName}, 2: ${moduleResult.achievement} "
            )*/
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

    open class CombinedViewHolder(val binding: ItemCombinedFinalResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: StudentsAssessmentData) {
            val moduleResult = data.studentResults.moduleResult
            val moduleOdk = data.studentResultsOdk?.moduleResult
            /*if (moduleResult.achievement == 0) {
                setTextColor(R.color.red_500)
            } else {
                setTextColor(R.color.green_500)
            }*/
            setTextColorR1(R.color.black)
            setTextColorR2(R.color.black)
// result1 is odk and result2 is read along
            if (data.studentResultsOdk?.studentName == AppConstants.COMBINED_STUDENT) {
                binding.tvStudent.text = "#"
                binding.tvResult1.text =
                    binding.tvResult1.context.getString(R.string.correct_answers_of_questions)
                binding.tvResult2.text =
                    binding.tvResult1.context.getString(R.string.words_read_correct)
            } else {
                val studentNumber = data.studentResults.studentName.substringAfter(" ").trim()
                binding.tvStudent.text = "${binding.tvStudent.context.getString(R.string.student)} $studentNumber"
                if (moduleResult.sessionCompleted && moduleOdk?.sessionCompleted == true) {
                    val percentage = getPercentage(
                        moduleOdk.achievement.toInt(),
                        moduleOdk.totalQuestions
                    )
                    binding.tvResult1.text =
                        "${moduleOdk.achievement}/${moduleOdk.totalQuestions} ($percentage%)"
                    binding.tvResult2.visibility = View.VISIBLE
                    binding.tvResult2.text = "${moduleResult.achievement}"
                    /*binding.tvResult1.text =
                        "${moduleOdk?.achievement}/${moduleOdk?.totalQuestions}"
                    binding.tvResult2.text = "${moduleResult.achievement}"*/
                } else {
                    binding.tvResult2.visibility = View.VISIBLE
                    binding.tvResult1.text =
                        binding.tvResult1.context.getString(R.string.session_not_completed)
                    binding.tvResult2.text =
                        binding.tvResult2.context.getString(R.string.session_not_completed)
                }
            }
        }

        private fun setTextColorR1(@ColorRes colorResId: Int) {
            binding.tvResult1.setTextColor(
                ContextCompat.getColor(
                    binding.tvResult1.context,
                    colorResId
                )
            )
        }

        private fun setTextColorR2(@ColorRes colorResId: Int) {
            binding.tvResult1.setTextColor(
                ContextCompat.getColor(
                    binding.tvResult1.context,
                    colorResId
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (mData[position].viewType) {
            BOLO -> TYPE_BOLO
            ODK -> TYPE_ODK
            "combined" -> TYPE_COMBINED
            else -> -1
        }
    }
}
