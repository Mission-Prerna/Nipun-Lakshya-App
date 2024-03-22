package com.samagra.commons.extensions

import android.os.SystemClock
import android.view.View


fun View.show(): View {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
    return this
}

/**
 * Show the view if [condition] returns true
 * (visibility = View.VISIBLE)
 */
inline fun View.showIf(condition: () -> Boolean): View {
    if (visibility != View.VISIBLE && condition()) {
        visibility = View.VISIBLE
    }
    return this
}

/**
 * Remove the view (visibility = View.GONE)
 */
fun View.hide(): View {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
    return this
}

/**
 * Remove the view if [predicate] returns true
 * (visibility = View.GONE)
 */
inline fun View.hideIf(predicate: () -> Boolean): View {
    if (visibility != View.GONE && predicate()) {
        visibility = View.GONE
    }
    return this
}

fun View.setDebounceClickListener(
    debounceTime: Long = 600L,
    action: () -> Unit
) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}