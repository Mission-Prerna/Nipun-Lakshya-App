package com.samagra.commons.basemvvm

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.R

abstract class BaseActivity<VB : ViewDataBinding, T : BaseViewModel> : AppCompatActivity() {

    val viewModel: T
        get() = _viewModel!!
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mProgress: ProgressDialog
    protected open var savedInstanceState: Bundle? = null
    private var _binding: VB? = null
    val binding get() = _binding!!

    private var _viewModel: T? = null

    @LayoutRes
    protected abstract fun layoutRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        _binding = DataBindingUtil.setContentView(this, layoutRes())
        _viewModel = getBaseViewModel()
        _binding?.setVariable(getBindingVariable(), _viewModel)
        _binding?.lifecycleOwner = this
        _binding?.executePendingBindings()
        _binding?.let {
            setProgressbar(it.root.context)
        }
        CompositeDisposableHelper.initialize()
        onLoadData()
        //Before loading fragment always check for bundle, if there is app process killed then this
        //will help to not load another fragment in memory.
        if (savedInstanceState == null) {
            loadFragment()
        }
    }

    private fun setProgressbar(context: Context) {
        progressDialog = ProgressDialog(context)
        with(progressDialog) {
//            this.setTitle(getString(R.string.sending_the_request))
            this.setMessage(getString(R.string.please_wait))
            this.setCancelable(false)
            this.setProgressStyle(R.style.ProgressDialogStyle)
            this.isIndeterminate = true
        }
    }

    abstract fun getBaseViewModel(): T?

    abstract fun getBindingVariable(): Int

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
        if (this::progressDialog.isInitialized && !progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    fun hideProgressBar() {
        if (this::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    protected open fun loadFragment() {}

    override fun onDestroy() {
        super.onDestroy()
        CompositeDisposableHelper.destroyCompositeDisposable()
    }

    protected fun showToast(@StringRes strRes: Int) {
        showToast(getString(strRes))
    }

    fun showToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }
}