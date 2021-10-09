package com.example.assets.uielements

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import com.example.assets.R
import com.google.android.material.button.MaterialButton

class CustomButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        // Set the background color
//        val backgroundColor = Color.parseColor() // Blue color
        val backgroundDrawable = createBackgroundDrawable(resources.getColor(R.color.blue_2e3192))
        background = backgroundDrawable

        // Set the elevation (shadow)
        val elevation = 15f
//        val elevation = resources.getDimension(R.dimen.button_elevation)
        this.elevation = elevation

        // Set the corner radius
        val cornerRadius = 30f
//        val cornerRadius = resources.getDimension(R.dimen.button_corner_radius)
        backgroundDrawable.cornerRadius = cornerRadius
    }

    private fun createBackgroundDrawable(backgroundColor: Int): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.setColor(backgroundColor)

        drawable.setStroke(0, Color.TRANSPARENT) // No border
        return drawable
    }
}
