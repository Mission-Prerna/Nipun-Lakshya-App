package com.example.assets.uielements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.assets.R
import com.google.android.material.button.MaterialButton

class PrimaryMaterialButton : MaterialButton {
    private lateinit var shadowPaint: Paint
    private lateinit var backgroundRect: RectF
    private lateinit var backgroundPaint: Paint
    private var borderPaint: Paint? = null
    private var borderColor = 0
    private var borderWidth = 0f

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        backgroundRect = RectF()
        setupBackground(context)
        setupShadow(context)
    }

    private fun setupBackground(context: Context) {
        backgroundPaint = Paint()
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = ContextCompat.getColor(context, R.color.blue_2e3192)
    }

    private fun setupShadow(context: Context) {
        shadowPaint = Paint()
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.setShadowLayer(
            20f,
            0f,
            15f,
            ContextCompat.getColor(context, R.color.blue_shadow)
        )
        setLayerType(View.LAYER_TYPE_SOFTWARE, shadowPaint)

        // Increase the padding to accommodate the shadow
        // Increase the padding to accommodate the shadow
//        val padding = (15f + 15f).toInt() // Adjust the padding as needed
//        setPadding(padding,0,padding,padding)
    }

    private fun updateBorderPaint() {
        borderPaint?.strokeWidth = borderWidth
        borderPaint?.color = borderColor
    }

    override fun onDraw(canvas: Canvas) {
        backgroundRect[0f, 0f, width.toFloat()] = height.toFloat()
        // Draw background
        val heightHalf = height / 2f
        background = null
        canvas.drawRoundRect(
            backgroundRect, heightHalf,
            heightHalf, shadowPaint
        )
        canvas.drawRoundRect(
            backgroundRect, heightHalf,
            heightHalf, backgroundPaint
        );
        super.onDraw(canvas)
    }

    override fun isEnabled(): Boolean {
        if (super.isEnabled()) {
            Log.e(
                "-->>", "button is enabled!"
            )
        } else {
            Log.e("-->>", "button is disabled!")
        }
        return super.isEnabled()
    }
}