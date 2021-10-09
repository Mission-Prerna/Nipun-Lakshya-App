package com.samagra.parent.ui.student_learning

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Spinner

class KeyboardHandler(var isDropDownOpen: Boolean, var isUDISEKeyboardShowing: Boolean, var spinner: Spinner?, var activity: Activity) {
    fun closeDropDown() {
        // If DROPDOWN and UDISE clicked, close DROPDOWN
        if (isDropDownOpen) hideSpinnerDropDown()
        isDropDownOpen = false
    }

    fun closeUDISEKeyboard() {
        // If UDISE and DROPDOWN clicked, close UDISE
        if (isUDISEKeyboardShowing) hideKeyboard(activity)
        isUDISEKeyboardShowing = false
    }

    fun hideSpinnerDropDown() {
        try {
            val method = Spinner::class.java.getDeclaredMethod("onDetachedFromWindow")
            method.isAccessible = true
            method.invoke(spinner)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
