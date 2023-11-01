package com.assessment.schoollist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.assessment.R
import com.assessment.databinding.SchoolListBinding
import com.assessment.schoollist.model.SchoolUiModel
import com.assessment.schoolreport.SchoolReportActivity
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.samagra.commons.extensions.hide
import com.samagra.commons.extensions.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SchoolListAdapter(
    val context: Context,
    var list: List<SchoolUiModel>?,
    private val onSchoolSelected: (SchoolDetailsWithReportHistory) -> Unit
) : RecyclerView.Adapter<SchoolListAdapter.ViewHolder>() {

    private var isValidCycle: Boolean = false

    class ViewHolder(private val bind: SchoolListBinding) : RecyclerView.ViewHolder(bind.root) {

        fun onBind(
            list: List<SchoolUiModel>,
            position: Int,
            onSchoolSelected: (SchoolDetailsWithReportHistory) -> Unit,
            isValidCycle: Boolean
        ) {
            val ctx = bind.root.context
            val schoolUiModel = list[position]
            val serialNo = position + 1
            bind.tvSerialNo.text = "$serialNo"
            bind.tvVisit.apply {
                if (schoolUiModel.showActionButton) {
                    show()
                    text = ctx.getString(schoolUiModel.actionBtnTextId)
                    background = if (schoolUiModel.isVisitTaken) {
                        null
                    } else {
                        ContextCompat.getDrawable(ctx, R.drawable.ic_rect_border_student_item)
                    }
                    setTextColor(ContextCompat.getColor(ctx, schoolUiModel.actionBtnTextColor))
                } else {
                    hide()
                }
            }
            bind.listData = schoolUiModel.schoolStatusHistory
            bind.executePendingBindings()
            bind.clSchoolClicked.setOnClickListener {
                if (schoolUiModel.isVisitTaken.not()) {
                    if (isValidCycle) {
                        onSchoolSelected(schoolUiModel.schoolStatusHistory)
                    }
                } else {
                    SchoolReportActivity.start(
                        context = ctx,
                        schoolUdise = schoolUiModel.schoolStatusHistory.udise
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val bind = DataBindingUtil.inflate<SchoolListBinding>(
            LayoutInflater.from(this.context),
            R.layout.item_school,
            parent,
            false
        )
        return ViewHolder(bind)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list?.let { holder.onBind(it, position, onSchoolSelected, isValidCycle) }
    }

    override fun getItemCount() = if (list.isNullOrEmpty()) 0 else list!!.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(
        schoolList: ArrayList<SchoolDetailsWithReportHistory>,
        isValidCycle: Boolean
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            this@SchoolListAdapter.list = schoolList.map { SchoolUiModel(it, isValidCycle) }
            this@SchoolListAdapter.isValidCycle = isValidCycle
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

}
