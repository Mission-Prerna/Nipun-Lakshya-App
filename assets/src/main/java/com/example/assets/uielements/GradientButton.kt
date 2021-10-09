package com.example.assets.uielements

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.IntDef
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.assets.R
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

/**
 * A [Button] that has a gradient background or stroke.
 *
 * Created by bartbergmans on 07/01/2017.
 */
class GradientButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.buttonStyle
) :
    AppCompatButton(context, attrs, defStyleAttr) {
    private val isCircular: Boolean
    private val isFilled: Boolean
    private val mStroke: Int
    private var mRippleColor = 0
    private val mBackgroundColor: Int

    @Orientation
    private val mOrientation: Int
    private val mGradient: IntArray

    init {
        setDefaultRippleColor(context)
        val a = context.obtainStyledAttributes(attrs, R.styleable.GradientButton)
        isFilled = a.getBoolean(R.styleable.GradientButton_filled, false)
        isCircular = a.getBoolean(R.styleable.GradientButton_circular, false)
        mStroke = a.getDimensionPixelSize(R.styleable.GradientButton_stroke, 0)
        mBackgroundColor = a.getColor(R.styleable.GradientButton_fill_color, Color.TRANSPARENT)
        @Orientation val orientation = a.getInt(R.styleable.GradientButton_orientation, TOP_BOTTOM)
        mOrientation = orientation
        if (a.hasValue(R.styleable.GradientButton_gradient)) {
            val id = a.getResourceId(R.styleable.GradientButton_gradient, 0)
            val values: IntArray = getResources().getIntArray(id)
            mGradient = IntArray(values.size)
            for (i in values.indices) {
                mGradient[i] = ContextCompat.getColor(context, values[i])
            }
        } else {
            mGradient = intArrayOf(
                a.getColor(R.styleable.GradientButton_start_color, Color.BLUE),
                a.getColor(R.styleable.GradientButton_end_color, Color.GREEN)
            )
        }
        a.recycle()
    }

    private fun setDefaultRippleColor(context: Context) {
        val attrs = intArrayOf(R.attr.colorControlHighlight)
        val a = context.obtainStyledAttributes(attrs)
        mRippleColor = a.getColor(0, Color.RED)
        a.recycle()
    }

    protected override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        setBackground(createBackgroundDrawable(width, height))
    }

    private fun createBackgroundDrawable(width: Int, height: Int): Drawable {
        var width = width
        var height = height
        if (isCircular && height > width) {
            width = height
        } else if (isCircular && width > height) {
            height = width
        }
        val content = createContentDrawable(width, height)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(
                TAG,
                "RIPPLE APPLIED, with color: $mRippleColor"
            )
            val mask = createMaskDrawable(width, height)
            val stateList = ColorStateList.valueOf(mRippleColor)
            RippleDrawable(stateList, content, mask)
        } else {
            content
        }
    }

    private fun createMaskDrawable(width: Int, height: Int): Drawable {
        val outerRadii = FloatArray(8)
        Arrays.fill(outerRadii, (height / 2).toFloat())
        val shape = RoundRectShape(outerRadii, null, null)
        return ShapeDrawable(shape)
    }

    private fun createContentDrawable(width: Int, height: Int): Drawable {
        val radius = height / 2
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.shader = createGradient(width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()+25f),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        if (!isFilled) {
            val background = Paint()
            background.isAntiAlias = true
            background.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            background.color = mBackgroundColor
            val innerRadius = (height - mStroke) / 2
            canvas.drawRoundRect(
                RectF(
                    mStroke.toFloat(),
                    mStroke.toFloat(), (width - mStroke).toFloat(), (height - mStroke).toFloat()+25f
                ),
                innerRadius.toFloat(), innerRadius.toFloat(), background
            )
        }
        return BitmapDrawable(getResources(), bitmap)
    }

    private fun createGradient(width: Int, height: Int): LinearGradient {
        val mode = Shader.TileMode.CLAMP
        return when (mOrientation) {
            TOP_BOTTOM -> LinearGradient(width / 2f, 0f, width / 2f, height.toFloat(), mGradient, null, mode)
            TR_BL -> LinearGradient(width.toFloat(), 0f, 0f, height.toFloat(), mGradient, null, mode)
            RIGHT_LEFT -> LinearGradient(width.toFloat(), height / 2f, 0f, height / 2f, mGradient, null, mode)
            BR_TL -> LinearGradient(width.toFloat(), height.toFloat(), 0f, 0f, mGradient, null, mode)
            BOTTOM_TOP -> LinearGradient(width / 2f, height.toFloat(), width / 2f, 0f, mGradient, null, mode)
            BL_TR -> LinearGradient(0f, height.toFloat(), width.toFloat(), 0f, mGradient, null, mode)
            LEFT_RIGHT -> LinearGradient(0f, height / 2f, width.toFloat(), height / 2f, mGradient, null, mode)
            TL_BR -> LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), mGradient, null, mode)
            else -> LinearGradient(width / 2f, 0f, width / 2f, height.toFloat(), mGradient, null, mode)
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(*[TOP_BOTTOM, TR_BL, RIGHT_LEFT, BR_TL, BOTTOM_TOP, BL_TR, LEFT_RIGHT, TL_BR])
    annotation class Orientation
    companion object {
        const val TOP_BOTTOM = 0
        const val TR_BL = 1
        const val RIGHT_LEFT = 2
        const val BR_TL = 3
        const val BOTTOM_TOP = 4
        const val BL_TR = 5
        const val LEFT_RIGHT = 6
        const val TL_BR = 7
        private val TAG = GradientButton::class.java.simpleName
    }
}