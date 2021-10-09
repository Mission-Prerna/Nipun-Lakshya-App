package com.samagra.parent.ui.formlite.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.samagra.parent.R;
import com.samagra.parent.ui.formlite.model.DropdownOption;

import timber.log.Timber;

public class AppSpinnerAdapter extends ArrayAdapter<DropdownOption> {
    private List<DropdownOption> mData;
    private Map<String, String> mDictionary;
    private boolean isDefaultSelected;
    private String defaultText;

    public AppSpinnerAdapter(@NonNull Context context,
                             Map<String, String> dictionary, boolean isDefaultSelected) {
        super(context, R.layout.layout_spinner_item);
        this.mDictionary = dictionary;
        this.isDefaultSelected = isDefaultSelected;
    }

    public AppSpinnerAdapter(@NonNull Context context,
                             Map<String, String> dictionary, boolean isDefaultSelected, String defaultText) {
        super(context, R.layout.layout_spinner_item);
        this.mDictionary = dictionary;
        this.isDefaultSelected = isDefaultSelected;
        this.defaultText = defaultText;
        mData = new ArrayList<>();
        mData.add(new DropdownOption(defaultText, defaultText));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView listItem = (TextView) convertView;
        if (listItem == null) {
            listItem = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_spinner_item, parent, false);
        }
        String value = mDictionary.get(mData.get(position).toString());
        if(TextUtils.isEmpty(value)){
            value = mData.get(position).toString();
        }
        listItem.setText(value);

        if (!isDefaultSelected && position == 0) {
            listItem.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.hint_gray));
        } else {
            listItem.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_014D3F));
        }
        Timber.i(" getview ::" + position);
        return listItem;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView listItem = (TextView) convertView;
        if (listItem == null)
            listItem = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_spinner_item, parent, false);
        String value = mDictionary.get(mData.get(position).toString());
        if(TextUtils.isEmpty(value)){
            value = mData.get(position).toString();
        }
        listItem.setText(value);
        Timber.i(" getDropDownView ::" + position);
        if (!isDefaultSelected && position == 0) {
            listItem.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.hint_gray));
        } else {
            listItem.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_014D3F));
        }
        return listItem;
    }

    @Override
    public void setDropDownViewResource(int resource) {
        super.setDropDownViewResource(R.layout.layout_spinner_item);
    }

    @Override
    public boolean isEnabled(int position) {
        return (position != 0 || isDefaultSelected);
    }

    public void setData(List<DropdownOption> dataList) {
        modifyList(dataList);
        mData = dataList;
        notifyDataSetChanged();
    }

    private void modifyList(List<DropdownOption> dataList) {
        Timber.i("AppSpinnerAdapter : modifyList" + defaultText);
        String text = defaultText == null ? "Select" : defaultText;
        dataList.add(0, new DropdownOption(text, text));
    }

    public List<DropdownOption> getData() {
        return mData;
    }

    @Nullable
    @Override
    public DropdownOption getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

}
