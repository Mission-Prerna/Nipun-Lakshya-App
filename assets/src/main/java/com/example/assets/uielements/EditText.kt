package com.example.assets.uielements

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.text.HtmlCompat

private val beforeTextChangedStub = { _: CharSequence?, _: Int, _: Int, _: Int -> Unit }
private val onTextChangedStub = { _: CharSequence?, _: Int, _: Int, _: Int -> Unit }
private val afterTextChanged = { _: Editable? -> Unit }

//fun EditText.addTextWatcher(
//        beforeTextChange: (var1: CharSequence?, var2: Int, var3: Int, var4: Int) -> Unit = beforeTextChangedStub,
//        onTextChange: (var1: CharSequence?, var2: Int, var3: Int, var4: Int) -> Unit = onTextChangedStub,
//        afterTextChange: (var1: Editable?) -> Unit = afterTextChanged
//) {
//    addTextChangedListener(object : TextWatcher {
//        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            beforeTextChange(p0, p1, p2, p3)
//        }
//
//        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            onTextChange(p0, p1, p2, p3)
//        }
//
//        override fun afterTextChanged(p0: Editable?) {
//            afterTextChange(p0)
//        }
//
//    })
//}

fun EditText.setBoldText(value: String) {
    val text1 = "${this.text}$value"

    val values = text1.split("+")

    if (values[0].trim().length >= 11) {
        return
    }

    val fgf = values[0]
    val text11 = HtmlCompat.fromHtml("<b>$fgf</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
    this.setText(text11)

//    if (values.isNotEmpty()) {
//        val lastValue = values.last()
//        val lastValueList = lastValue.trim().split(".")
//
//        if (this.text.split("+").last().trim().isEmpty() && value.trim() == "0") {
//            return
//        }
//        if (lastValueList[0].trim().length >= 9) {
//            return
//        }
//        if (lastValueList.size > 1 && lastValueList[1].trim().length >= 3) {
//            return
//        }
//
//        var valuesExceptLast = ""
//        for (i in 0..(values.size - 2)) {
//            valuesExceptLast += "${values[i]} + "
//        }

//    }
}

fun EditText.deleteBoldText() {
    val text1 = this.text.toString().trim().substring(0, this.text.trim().length - 1).trim()

    val values = text1.split("+")

    if (values.isNotEmpty()) {
        val lastValue = values.last()
        var valuesExceptLast = ""
        for (i in 0..(values.size - 2)) {
            valuesExceptLast += "${values[i]} + "
        }

        val text = HtmlCompat.fromHtml("$valuesExceptLast<b>$lastValue</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        this.setText(text)
    }
}

fun EditText.setSpaceText(value: String) {
    var number = this.text.toString()
    number = if (this.text.length == 5) "$number $value" else "$number$value"
    this.setText(number)
    this.setSelection(this.text?.length ?: 0)
}

fun EditText.deleteSpaceText() {
    var number = this.text.toString().replace(" ".toRegex(), "")
    if (number.isEmpty()) return
    number = number.substring(0, number.trim().length - 1).trim()
    var num = ""
    for (i in 0 until number.length) {
        num = if (i == 5) "$num ${number[i]}" else "$num${number[i]}"
    }
    this.setText(num)
    this.setSelection(this.text?.length ?: 0)
}

fun EditText.setNormalText(value: String) {
    var number = this.text.toString()
    number = "$number$value"
    this.setText(number)
    this.setSelection(this.text?.length ?: 0)
}

fun EditText.deleteNormalText() {
    var number = this.text.toString().trim()
    if (number.isEmpty()) return
    number = number.substring(0, number.trim().length - 1).trim()
    this.setText(number)
    this.setSelection(this.text?.length ?: 0)
}