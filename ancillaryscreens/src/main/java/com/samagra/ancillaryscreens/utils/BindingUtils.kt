package com.samagra.ancillaryscreens.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
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

    @JvmStatic
    @BindingAdapter("underlinedText")
    fun setUnderlinedText(view: TextView, text: String?) {
        if (text != null) {
            val spannableString = SpannableString(text)
            val firstSpaceIndex = text.indexOf(' ')

            if (firstSpaceIndex >= 0) {
                val underlineSpan = UnderlineSpan()
                spannableString.setSpan(underlineSpan, 0, firstSpaceIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            view.text = spannableString
        } else {
            view.text = ""
        }
    }

}