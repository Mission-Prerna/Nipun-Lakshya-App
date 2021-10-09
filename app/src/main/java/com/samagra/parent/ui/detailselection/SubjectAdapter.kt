package com.samagra.parent.ui.detailselection

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SubjectAdapter(recyclerDataArrayList: ArrayList<SubjectModel>?, mcontext: Context) :
    RecyclerView.Adapter<SubjectAdapter.RecyclerViewHolder>() {
    private var mData: ArrayList<SubjectModel>?
    private val mContext: Context
    private lateinit var mListener: ItemSelectionListener<SubjectModel>
    private var selectedSubject: SubjectModel? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView = SubjectItemView(parent.context)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        itemView.setLayoutParams(lp)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        // Set the data to textview and imageview.
        val recyclerData: SubjectModel = mData!![position]
        (holder.itemView as SubjectItemView).setData(recyclerData.title, recyclerData.imgid)
        holder.itemView.isSelected = recyclerData.isSelected
        holder.itemView.setOnClickListener { updateSelection(position) }

//        if (isLastRowAndShouldBeCentered(position)) {
//            GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) holder.containerView.getLayoutParams();
//            int marginSize = this.containerWidth / 4;
//            layoutParams.leftMargin = marginSize;
//            layoutParams.rightMargin = marginSize;
//            holder.containerView.setLayoutParams(layoutParams);
//
//        }
    }

    override fun getItemCount(): Int {
        // this method returns the size of recyclerview
        return if (mData == null) 0 else mData!!.size
    }

    fun setData(data: ArrayList<SubjectModel>?) {
        mData = data
        notifyDataSetChanged()
    }

    private fun updateSelection(position: Int) {
        if (mData != null) {
            for (i in mData!!.indices) {
                mData!![i].isSelected = i == position
            }
            notifyDataSetChanged()
            if (mListener != null) {
                selectedSubject = if (position < 0) {
                    null
                } else {
                    mData!![position]
                }
                selectedSubject?.let {
                    mListener.onSelectionChange(position, it)
                }

            }
        }
    }

    fun resetSelection() {
        updateSelection(-1)
    }

    fun getSelectedSubject(): SubjectModel? {
        return selectedSubject
    }

    // View Holder Class to handle Recycler View.
    inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemView: SubjectItemView=itemView as SubjectItemView
    }

    fun isLastRowAndShouldBeCentered(pos: Int): Boolean {
        return if (pos == mData!!.size - 1 && mData!!.size % 2 != 0) {
            true
        } else {
            false
        }
    }

    fun setItemSelectionListener(listener: ItemSelectionListener<SubjectModel>) {
        mListener = listener
    }

    init {
        mData = recyclerDataArrayList
        mContext = mcontext
    }
}