package com.samagra.parent.ui.newresultafterdietmentor


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
import com.samagra.commons.utils.getNipunCriteria
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemCombinedFinalResultBinding
import com.samagra.parent.databinding.ItemNewAfterDietMentorResultBinding
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData
import com.samagra.parent.ui.competencyselection.readonlycompetency.StateLedResultModel

class ResultNewAfterDietMentorAdapter(arrayList: java.util.ArrayList<StateLedResultModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_BOLO: Int = 0
    private val TYPE_ODK: Int = 1
    private val TYPE_COMBINED: Int = 2
    private var mData: java.util.ArrayList<StateLedResultModel> = arrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //val binding: ItemNewAfterDietMentorResultBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_new_after_diet_mentor_result, parent, false)
        // return ODKViewHolder(binding)
        return when (viewType) {
            TYPE_BOLO -> {
                val binding: ItemNewAfterDietMentorResultBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_new_after_diet_mentor_result, parent, false)
                BOLOViewHolder(binding)
            }
            TYPE_ODK -> {
                val binding: ItemNewAfterDietMentorResultBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_new_after_diet_mentor_result, parent, false)
                ODKViewHolder(binding)
            }
            TYPE_COMBINED -> {
                val binding: ItemCombinedFinalResultBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_combined_final_result, parent, false)
                CombinedViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid type")
        }
    }

    open class ODKViewHolder(val binding: ItemNewAfterDietMentorResultBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: StateLedResultModel) {
            setTextColor(R.color.black)
            val studentNumber = data.studentName.substringAfter(" ").trim()
            binding.tvQuestion.text = "${binding.tvResult.context.getString(R.string.student)} $studentNumber"
            if (data.isSessionCompleted) {
                if (data.isNipun) {
                    binding.tvResult.text = binding.tvResult.context.getString(R.string.student_nipun)
                    binding.tvResult.setTextColor(ContextCompat.getColor(binding.tvResult.context, R.color.green_500))
                    binding.smvImage.setImageResource(R.drawable.ic_check_mark)
                } else {
                    binding.tvResult.text = binding.tvResult.context.getString(R.string.student_not_nipun)
                    binding.tvResult.setTextColor(ContextCompat.getColor(binding.tvResult.context, R.color.red_500))
                    binding.smvImage.setImageResource(R.drawable.ic_remove)
                }
            } else {
                binding.smvImage.setImageResource(R.drawable.ic_remove)
                binding.tvResult.text = binding.tvResult.context.getString(R.string.session_not_completed)
            }
        }

        private fun setTextColor(@ColorRes colorResId: Int) {
            binding.tvResult.setTextColor(ContextCompat.getColor(binding.tvResult.context, colorResId))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (ODK) {
            /*BOLO -> {
                (holder as BOLOViewHolder).bind(mData[position])
            }*/
            ODK -> {
                (holder as ODKViewHolder).bind(mData.get(position))
            }
            /*"combined" -> {
                (holder as CombinedViewHolder).bind(mData[position])
            }*/
        }
    }

    open class BOLOViewHolder(val binding: ItemNewAfterDietMentorResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StudentsAssessmentData) {
            val nipunCriteriaValue = AppConstants.READ_ALONG_CRITERIA_KEY.getNipunCriteria(
                data.studentResults.grade,
                data.studentResults.subject
            )
            val moduleResult = data.studentResults.moduleResult
            setTextColor(R.color.black)
            val studentNumber = data.studentResults.studentName.substringAfter(" ").trim()
            binding.tvQuestion.text = "${binding.tvResult.context.getString(R.string.student)} $studentNumber"
            binding.tvQuestion.text = data.studentResults.studentName
            if (moduleResult.sessionCompleted) {
                if ((moduleResult.achievement ?: 0) >= nipunCriteriaValue) {
                    binding.tvResult.text = binding.tvResult.context.getString(R.string.student_nipun)
                    binding.tvResult.setTextColor(ContextCompat.getColor(binding.tvResult.context, R.color.green_500))
                    binding.smvImage.background = binding.smvImage.context.getDrawable(R.drawable.p_report_card)
                }else{
                    binding.tvResult.text = binding.tvResult.context.getString(R.string.student_not_nipun)
                    binding.tvResult.setTextColor(ContextCompat.getColor(binding.tvResult.context, R.color.red_500))
                    binding.smvImage.background = binding.smvImage.context.getDrawable(R.drawable.f_report_card)
                }
            } else {
                binding.tvResult.text = binding.tvResult.context.getString(R.string.session_not_completed)
            }
        }

        private fun setTextColor(@ColorRes colorResId: Int) {
            binding.tvResult.setTextColor(ContextCompat.getColor(binding.tvResult.context, colorResId))
        }

    }
    open class CombinedViewHolder(val binding: ItemCombinedFinalResultBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: StudentsAssessmentData) {
            val moduleResult = data.studentResults.moduleResult
            val moduleOdk = data.studentResultsOdk?.moduleResult
            setTextColorR1(R.color.black)
            setTextColorR2(R.color.black)
// result1 is odk and result2 is read along
            if (data.studentResultsOdk?.studentName == AppConstants.COMBINED_STUDENT) {
                binding.tvStudent.text = "#"
                binding.tvResult1.text = binding.tvResult1.context.getString(R.string.correct_answers_of_questions)
                binding.tvResult2.text = binding.tvResult1.context.getString(R.string.words_read_correct)
            } else {
                val studentNumber = data.studentResults.studentName.substringAfter(" ").trim()
                binding.tvStudent.text = "${binding.tvStudent.context.getString(R.string.student)} $studentNumber"
                if (moduleResult.sessionCompleted && moduleOdk?.sessionCompleted == true) {
                    val percentage = getPercentage(moduleOdk.achievement, moduleOdk.totalQuestions)
                    binding.tvResult1.text = "${moduleOdk.achievement}/${moduleOdk.totalQuestions} ($percentage%)"
                    binding.tvResult2.visibility = View.VISIBLE
                    binding.tvResult2.text = "${moduleResult.achievement}"
                } else {
                    binding.tvResult2.visibility = View.VISIBLE
                    binding.tvResult1.text = binding.tvResult1.context.getString(R.string.session_not_completed)
                    binding.tvResult2.text = binding.tvResult2.context.getString(R.string.session_not_completed)
                }
            }
        }

        private fun setTextColorR1(@ColorRes colorResId: Int) {
            binding.tvResult1.setTextColor(ContextCompat.getColor(binding.tvResult1.context, colorResId))
        }

        private fun setTextColorR2(@ColorRes colorResId: Int) {
            binding.tvResult1.setTextColor(ContextCompat.getColor(binding.tvResult1.context, colorResId))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (ODK) {
            BOLO -> TYPE_BOLO
            ODK -> TYPE_ODK
            "combined" -> TYPE_COMBINED
            else -> -1
        }
    }

    override fun getItemCount() = mData.size
}