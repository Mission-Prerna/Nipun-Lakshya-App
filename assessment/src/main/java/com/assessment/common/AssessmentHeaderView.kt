package com.assessment.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.assessment.R
import com.assessment.databinding.ViewAssessmentHeaderBinding
import timber.log.Timber

private const val STUDENT = 0
private const val SCHOOL = 1
private const val PARENT = 2
class AssessmentHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: ViewAssessmentHeaderBinding

    init {
        binding = ViewAssessmentHeaderBinding.inflate(
            /* inflater = */ LayoutInflater.from(context),
            /* root = */ this,
            /* attachToRoot = */ false
        )
        addView(binding.root)
    }

    fun bind(model: AssessmentHeaderModel, cardType: Int) {
        Timber.d("bind: $model")
        binding.apply {
            if (cardType == SCHOOL) {
                tvName.text = resources.getString(R.string.school_is, model.name)
                tvDate.text = resources.getString(R.string.date_is, model.date)
                tvIdentifier.text =
                    resources.getString(R.string.udise_is, model.identifier.toString())
                tvActor.text = String.format("%s : %s", model.mentorType, model.mentorName)
            } else if (cardType == STUDENT){
                tvName.text = resources.getString(R.string.name_is, model.name)
                tvDate.text = resources.getString(R.string.date_is, model.date)
                tvIdentifier.text =
                    resources.getString(R.string.grade_is, model.identifier)
                tvActor.text = String.format("%s : %s", model.mentorType, model.mentorName)
            } else if(cardType == PARENT){
                tvName.text = resources.getString(R.string.grade_is, model.identifier)
                tvDate.text = resources.getString(R.string.date_is, model.date)
                tvIdentifier.visibility = View.GONE
                tvActor.visibility = View.GONE
            }
        }
    }

}