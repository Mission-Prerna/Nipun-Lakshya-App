package com.samagra.ancillaryscreens.utils

import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.BindingAdapter

object BindingUtils {

    @JvmStatic
    @BindingAdapter("textRes")
    fun getString(button: AppCompatButton, stringResourceId: Int) {
        if (stringResourceId > 0) {
            button.text = button.context.resources.getString(stringResourceId)
        }
    }

}