package com.samagra.parent.ui.assessmentsetup

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.parent.R
import com.samagra.parent.databinding.ItemSchoolListBinding
import com.samagra.parent.ui.bind

class SchoolListAdapter(
    val context: Context,
    var list: ArrayList<SchoolsData>,
    private val onSchoolSelected: (SchoolsData) -> Unit
) : RecyclerView.Adapter<SchoolListAdapter.ViewHolder>() {

    class ViewHolder(private val bind: ItemSchoolListBinding) : RecyclerView.ViewHolder(bind.root) {

        fun onBind(
            list: ArrayList<SchoolsData>,
            position: Int,
            onSchoolSelected: (SchoolsData) -> Unit
        ) {
            val serialNo = position + 1
            bind.tvSerialNo.text = "$serialNo"

            if(list[position].visitStatus!!){
                bind.tvVisit.text = bind.tvVisit.context.getString(R.string.visited)
            }else{
                bind.tvVisit.text = bind.tvVisit.context.getString(R.string.not_visited)
            }
            bind.listData = list[position]
            bind.executePendingBindings()

            bind.clSchoolClicked.setOnClickListener {
                onSchoolSelected(list[position])
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val bind = parent.bind<ItemSchoolListBinding>(R.layout.item_school_list)
        return ViewHolder(bind)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list, position, onSchoolSelected)
    }

    override fun getItemCount() = list.size

    fun updateAdapter(schoolList: ArrayList<SchoolsData>) {
//        list.clear()
        this.list = schoolList
        notifyDataSetChanged()
    }

}
