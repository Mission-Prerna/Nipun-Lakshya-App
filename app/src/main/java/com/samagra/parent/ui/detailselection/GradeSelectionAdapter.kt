package com.samagra.parent.ui.detailselection

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.samagra.parent.R

class GradeSelectionAdapter(context: Context, resource: Int, objects: List<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        // Get the item at the specified position
        var item = getItem(position)

        item = context.getString(R.string.class_hindi) + " " + item
        // Set a tag to the view
        view.tag = item

        return view
    }
}