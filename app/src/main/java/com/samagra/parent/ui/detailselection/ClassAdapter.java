package com.samagra.parent.ui.detailselection;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.samagra.parent.R;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.RecyclerViewHolder> {

    private List<ClassModel> mData;
    private Context mContext;
//    int containerWidth;
    private ItemSelectionListener<ClassModel> mListener;
    private ClassModel mSelectedClass;


    public ClassAdapter(List<ClassModel> recyclerDataArrayList, Context mcontext) {
        this.mData = recyclerDataArrayList;
        this.mContext = mcontext;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ClassItemView itemView = new ClassItemView(parent.getContext());
//        this.containerWidth = parent.getMeasuredWidth();
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        // Set the data to textview and imageview.
        ClassModel recyclerData = mData.get(holder.getAdapterPosition());
        holder.itemView.setData(mContext.getString(R.string.class_hindi)+" "+recyclerData.getTitle().substring(recyclerData.getTitle().length()-1));

        holder.itemView.setSelected(recyclerData.isSelected());
        if(mListener != null && recyclerData.isSelected()){
            mSelectedClass = mData.get(holder.getAdapterPosition());
            mListener.onSelectionChange(holder.getAdapterPosition(), mSelectedClass);
        }

        holder.itemView.setOnClickListener(v -> updateSelection(holder.getAdapterPosition()));
    }

    private void updateSelection(int position) {
        if (mData != null) {
            for (int i = 0; i < mData.size(); i++) {
                mData.get(i).setSelected(i == position);
            }
            notifyDataSetChanged();
            if(mListener != null){
                if(position < 0){
                    mSelectedClass = null;
                }else{
                    mSelectedClass = mData.get(position);
                }
                mListener.onSelectionChange(position, mSelectedClass);
            }
        }
    }

    public void resetSelection(){
        updateSelection(-1);
    }

    public ClassModel getSelectedItem() {
        return mSelectedClass;
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return mData.size();
    }

    // View Holder Class to handle Recycler View.
    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private ClassItemView itemView;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = (ClassItemView) itemView;

        }
    }

    public boolean isLastRowAndShouldBeCentered(int pos) {
        if (pos == mData.size() - 1 && mData.size() % 2 != 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setItemSelectionListener(ItemSelectionListener<ClassModel> listener){
        this.mListener = listener;
    }
}