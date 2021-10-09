package com.samagra.parent.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.ui.assessmenthome.DrawerAdapter
import com.samagra.parent.ui.assessmenthome.DrawerItem

fun DrawerLayout.setBody(context: Context, prefs: CommonsPrefsHelperImpl, onItemClick : (item : DrawerItem) -> Unit){
    val options  = ArrayList<DrawerItem>()
    // true if need drawer options enabled
    val drawerItems = if (prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
        addItemsForTeachers(options, context)
    } else {
        addItems(options, context)
    }
    val recyclerView = findViewById<RecyclerView>(R.id.body)
    val drawerAdapter = DrawerAdapter(drawerItems, context) {
        onItemClick(it)
    }
    recyclerView.apply {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = drawerAdapter
    }
    findViewById<TextView>(R.id.privacy_policy_tv).setOnClickListener { onItemClick(DrawerItem(context.getString(R.string.privacy_policy), 0, DrawerOptions.PRIVACY_POLICY, true)) }
}

fun addItems(options: java.util.ArrayList<DrawerItem>, context: Context): java.util.ArrayList<DrawerItem> {
    options.add(DrawerItem(context.getString(R.string.knowledge_section), R.drawable.ic_knowledge, DrawerOptions.KNOWLEDGE, false))
    options.add(DrawerItem(context.getString(R.string.help), R.drawable.ic_help, DrawerOptions.HELP, true))
    options.add(DrawerItem(context.getString(R.string.logout),R.drawable.ic_logout,DrawerOptions.LOGOUT,true))
    return options
}

fun addItemsForTeachers(options: ArrayList<DrawerItem>, context: Context): ArrayList<DrawerItem> {
    options.add(DrawerItem(context.getString(R.string.logout),R.drawable.ic_logout,DrawerOptions.LOGOUT,true))
    return options
}

fun DrawerLayout.setBodyForTeacher(context: Context, onItemClick : (item : DrawerItem) -> Unit){
    val options  = ArrayList<DrawerItem>()
    // true if need drawer options enabled
    options.add(DrawerItem(context.getString(R.string.logout),R.drawable.ic_logout,DrawerOptions.LOGOUT,true))
    val recyclerView = findViewById<RecyclerView>(R.id.body)
    val drawerAdapter = DrawerAdapter(options, context){
        onItemClick(it)
    }
    recyclerView.apply {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        adapter = drawerAdapter
    }
    findViewById<TextView>(R.id.privacy_policy_tv).setOnClickListener { onItemClick(DrawerItem(context.getString(R.string.privacy_policy), 0, DrawerOptions.PRIVACY_POLICY, true)) }
}

enum class DrawerOptions {
    KNOWLEDGE,
    HELP,
    LOGOUT,
    PRIVACY_POLICY
}

inline fun <T : Fragment> T.withArgs(argsBuilder: Bundle.() -> Unit): T =
    this.apply {
        arguments = Bundle().apply(argsBuilder)
    }

fun <T : ViewDataBinding> ViewGroup.bind(layoutId: Int): T {
    return DataBindingUtil.inflate(LayoutInflater.from(this.context), layoutId, this, false)
}

fun View.setTextOnUI(text: String) {
    if (this is TextView) {
        this.text = text
    } else if (this is AppCompatTextView) {
        this.text = text
    }
}

fun View.setImageOnUI(image: Drawable) {
    if (this is ImageView) {
        this.setImageDrawable(image)
    } else if (this is AppCompatImageView) {
        this.setImageDrawable(image)
    }
}

val Number.toPx get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics)

fun String.getHtmlSpanString(): Spanned? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(this)
    }
}

fun CommonsPrefsHelperImpl.getBearerAuthToken(): String {
    return Constants.BEARER_ + this.authToken
}