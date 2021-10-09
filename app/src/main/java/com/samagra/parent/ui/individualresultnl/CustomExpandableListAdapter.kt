package com.samagra.parent.ui.individualresultnl

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.samagra.commons.models.Results
import com.samagra.parent.R
import java.util.*

class CustomExpandableListAdapter(
    private val context: Context,
    private val expandableResultToShowList: ArrayList<ExpandableResultsModel>
) : BaseExpandableListAdapter() {
    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
//        return expandableListDetail[expandableListTitle[listPosition]]!![expandedListPosition]
        return expandableResultToShowList[listPosition].studentResultList[expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View? {
        var convertView = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as Results
        if (convertView == null) {
            val layoutInflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_item, null)
        }
        val tvQuestion = convertView?.findViewById<View>(R.id.tv_question) as TextView
        val tvAnswer = convertView.findViewById<View>(R.id.tv_result) as TextView
        tvQuestion.text = expandedListText.question
        val result = if (expandedListText.answer == "0") {
            setTextColor(R.color.red_500, tvAnswer)
            context.getString(R.string.wrong)
        } else if (expandedListText.answer == "1") {
            setTextColor(R.color.green_500, tvAnswer)
            context.getString(R.string.correct)
        } else {
            setTextColor(R.color.black, tvAnswer)
            expandedListText.answer
        }
        tvAnswer.text = result
        return convertView
    }

    private fun setTextColor(colorResId: Int, tvAnswer: TextView) {
        tvAnswer.setTextColor(
            ContextCompat.getColor(
                context,
                colorResId
            )
        )
    }

    override fun getChildrenCount(listPosition: Int): Int {
//        return expandableListDetail[expandableListTitle[listPosition]]!!.size
        return expandableResultToShowList[listPosition].studentResultList.size
    }

    override fun getGroup(listPosition: Int): Any {
//        return expandableListTitle[listPosition]
        return expandableResultToShowList[listPosition]
    }

    override fun getGroupCount(): Int {
//        return expandableListTitle.size
        return expandableResultToShowList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View? {
        var convertView: View? = convertView
        val listTitle = getGroup(listPosition) as ExpandableResultsModel
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_group, null)
        }
        val tvCompetencyName = convertView?.findViewById<View>(R.id.expandedListItem) as TextView
        val tvNipun = convertView?.findViewById<View>(R.id.tv_nipun) as TextView
        tvCompetencyName.setTypeface(null, Typeface.BOLD)
        tvCompetencyName.text = listTitle.competencyName
        tvCompetencyName.compoundDrawablePadding = 0
        if (!listTitle.studentResultList.isNullOrEmpty()) {
            if (listTitle.isNipun == true) {
                setTextColor(R.color.green_500, tvNipun)
                tvNipun.text = context.getString(R.string.nipun)
            } else {
                setTextColor(R.color.red_500, tvNipun)
                tvNipun.text = context.getString(R.string.not_nipun)
            }
        }else{
            setTextColor(R.color.red_500, tvNipun)
            tvNipun.text = context.getString(R.string.session_not_completed)
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return false
    }
}