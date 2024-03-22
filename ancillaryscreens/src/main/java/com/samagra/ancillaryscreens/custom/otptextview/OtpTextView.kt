package com.samagra.ancillaryscreens.custom.otptextview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.samagra.ancillaryscreens.R
import com.samagra.ancillaryscreens.utils.UtilityFunctions
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern


class OtpTextView : FrameLayout {

    private var itemViews: MutableList<ItemView>? = null
    private var otpChildEditText: OTPChildEditText? = null
    var otpListener: OTPListener? = null

    private var length: Int = 0

    private val filter: InputFilter
        get() = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!Pattern.compile(
                        PATTERN
                    )
                        .matcher(source[i].toString())
                        .matches()
                ) {
                    return@InputFilter ""
                }
            }
            null
        }

    val otp: String?
        get() = otpChildEditText?.text?.toString()

    fun clearOtp() {
        otpChildEditText?.text?.clear()
    }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val styles = context.obtainStyledAttributes(attrs, R.styleable.OtpTextView)
        styleEditTexts(styles, attrs)
        styles.recycle()
    }

    private fun styleEditTexts(styles: TypedArray, attrs: AttributeSet?) {
        length = styles.getInt(R.styleable.OtpTextView_length, DEFAULT_LENGTH)
        generateViews(styles, attrs)
    }

    private fun generateViews(styles: TypedArray, attrs: AttributeSet?) {
        itemViews = ArrayList()
        if (length > 0) {
            val width: Int
            val height: Int
            val space: Int
            val spaceLeft: Int
            val spaceRight: Int
            val otp = styles.getString(R.styleable.OtpTextView_otp)
            val autoSizeEnabled =
                styles.getBoolean(R.styleable.OtpTextView_auto_size_enabled, false)
            if (autoSizeEnabled) {
                val windowManager =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val screenWidth = displayMetrics.widthPixels
                val offset = screenWidth / (4.75 + length)
                Timber.i("Screen Width - $screenWidth, Offset - $offset")
                width = offset.toInt()
                height = (offset * 1.25).toInt()
                space = ((offset * 0.75)/2).toInt()
                spaceLeft = (offset * 1.25).toInt()
                spaceRight = (offset * 1.25).toInt()
            } else {
                width = styles.getDimension(
                    R.styleable.OtpTextView_width, Utils.getPixels(
                        context,
                        DEFAULT_WIDTH
                    ).toFloat()
                ).toInt()
                height = styles.getDimension(
                    R.styleable.OtpTextView_height, Utils.getPixels(
                        context,
                        DEFAULT_HEIGHT
                    ).toFloat()
                ).toInt()
                space = styles.getDimension(
                    R.styleable.OtpTextView_box_margin, Utils.getPixels(
                        context,
                        DEFAULT_SPACE
                    ).toFloat()
                ).toInt()
                spaceLeft = styles.getDimension(
                    R.styleable.OtpTextView_box_margin_left, Utils.getPixels(
                        context,
                        DEFAULT_SPACE_LEFT
                    ).toFloat()
                ).toInt()
                spaceRight = styles.getDimension(
                    R.styleable.OtpTextView_box_margin_right, Utils.getPixels(
                        context,
                        DEFAULT_SPACE_RIGHT
                    ).toFloat()
                ).toInt()
            }
            val spaceTop = styles.getDimension(
                R.styleable.OtpTextView_box_margin_top, Utils.getPixels(
                    context,
                    DEFAULT_SPACE_TOP
                ).toFloat()
            ).toInt()
            val spaceBottom = styles.getDimension(
                R.styleable.OtpTextView_box_margin_bottom, Utils.getPixels(
                    context,
                    DEFAULT_SPACE_BOTTOM
                ).toFloat()
            ).toInt()
            val params = LinearLayout.LayoutParams(width, height)
            Timber.i("Width:$width , Height:$height, space:$space, spaceLeft:$spaceLeft, spaceRight: $spaceRight")
            if (space > 0) {
                params.setMargins(space, space, space, space)
            } else {
                params.setMargins(spaceLeft, spaceTop, spaceRight, spaceBottom)
            }

            val editTextLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            editTextLayoutParams.gravity = Gravity.CENTER
            otpChildEditText = OTPChildEditText(context)
            otpChildEditText?.imeOptions = EditorInfo.IME_ACTION_DONE
            otpChildEditText?.filters = arrayOf(filter, InputFilter.LengthFilter(length))
            setTextWatcher(otpChildEditText)
            addView(otpChildEditText, editTextLayoutParams)


            val linearLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val linearLayout = LinearLayout(context)

            addView(linearLayout, linearLayoutParams)

            for (i in 0 until length) {
                val itemView = ItemView(context, attrs)
                itemView.setViewState(ItemView.INACTIVE)
                linearLayout.addView(itemView, i, params)
                itemViews?.add(itemView)
            }
            if (otp != null) {
                setOTP(otp)
            } else {
                setOTP("")
            }
            setKeyboardDoneFunctionality(otpChildEditText!!)
        } else {
            throw IllegalStateException("Please specify the length of the otp view")
        }
    }

    fun setOnKeyboardDefaultButtonClickListener(otpListener: OTPListener) {
        this.otpListener = otpListener
    }

    private fun setKeyboardDoneFunctionality(otpChildEditText: OTPChildEditText) {
        otpChildEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                otpListener?.onKeyboardDefaultButtonClick(otpChildEditText.text.toString())
                true
            } else false
        }
    }

    private fun setTextWatcher(otpChildEditText: OTPChildEditText?) {
        otpChildEditText?.addTextChangedListener(object : TextWatcher {
            /**
             * @param s
             * @param start
             * @param count
             * @param after
             */
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            /**
             * @param s
             * @param start
             * @param before
             * @param count
             */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                otpListener?.let { otpListener ->
                    otpListener.onInteractionListener()
                    if (s.length == length) {
                        otpListener.onOTPComplete(s.toString())
                    }
                }
                setOTP(s)
                setFocus(s.length)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }


    private fun setFocus(length: Int) {
        itemViews?.let { itemViews ->
            for (i in itemViews.indices) {
                if (i == length) {
                    itemViews[i].setViewState(ItemView.ACTIVE)
                } else {
                    itemViews[i].setViewState(ItemView.INACTIVE)
                }
            }
            if (length == itemViews.size) {
                itemViews[itemViews.size - 1].setViewState(ItemView.ACTIVE)
            }
        }
    }

    fun setOTP(s: CharSequence) {
        itemViews?.let { itemViews ->
            for (i in itemViews.indices) {
                if (i < s.length) {
                    itemViews[i].setText(s[i].toString())
                } else {
                    itemViews[i].setText("")
                }
            }
        }
    }

    fun requestFocusOTP() {
        otpChildEditText?.requestFocus()
    }

    fun showError() {
        itemViews?.let { itemViews ->
            for (itemView in itemViews) {
                itemView.setViewState(ItemView.ERROR)
            }
        }
    }

    fun resetState() {
        otp?.let {
            setFocus(it.length)
        }
    }

    fun showSuccess() {
        itemViews?.let { itemViews ->
            for (itemView in itemViews) {
                itemView.setViewState(ItemView.SUCCESS)
            }
        }
    }

    fun setOTP(otp: String) {
        otpChildEditText?.setText(otp)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(l: OnTouchListener) {
        super.setOnTouchListener(l)
        otpChildEditText?.setOnTouchListener(l)
    }

    fun hideKeyboard(activity: Activity?) {

        UtilityFunctions.hideKeyboard(otpChildEditText?.context, activity)
    }

    companion object {

        private const val DEFAULT_LENGTH = 4
        private const val DEFAULT_HEIGHT = 48
        private const val DEFAULT_WIDTH = 48
        private const val DEFAULT_SPACE = -1
        private const val DEFAULT_SPACE_LEFT = 4
        private const val DEFAULT_SPACE_RIGHT = 4
        private const val DEFAULT_SPACE_TOP = 4
        private const val DEFAULT_SPACE_BOTTOM = 4

        private const val PATTERN = "[1234567890]*"
    }
}
