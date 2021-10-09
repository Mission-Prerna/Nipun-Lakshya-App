package com.samagra.commons.basemvvm

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.getBinding
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.R

abstract class NonViewModelBaseActivity<VB : ViewDataBinding> : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var mProgress: ProgressDialog
    protected open var savedInstanceState: Bundle? = null

    @LayoutRes
    protected abstract fun layoutRes(): Int
    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        binding = getViewBinding(layoutInflater)
        setContentView(binding.root)
        setProgressbar(this)
        CompositeDisposableHelper.initialize()
        onLoadData()
        loadFragment()
//        addFragment(savedInstanceState)
    }

    private fun setProgressbar(context: Context) {
        progressDialog = ProgressDialog(context)
        with(progressDialog) {
//            this.setTitle(getString(R.string.sending_the_request))
            this.setMessage(getString(R.string.please_wait))
            this.setCancelable(false)
            this.isIndeterminate = true
        }
    }

    abstract fun getViewBinding(layoutInflater: LayoutInflater): VB

    protected open fun onLoadData() {}

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showProgressBar() {
        if (progressDialog != null && !progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    fun hideProgressBar() {
        if (progressDialog != null && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    protected open fun loadFragment() {}

    override fun onDestroy() {
        super.onDestroy()
        CompositeDisposableHelper.destroyCompositeDisposable()
    }
}