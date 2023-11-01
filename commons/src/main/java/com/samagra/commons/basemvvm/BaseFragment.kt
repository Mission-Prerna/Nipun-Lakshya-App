package com.samagra.commons.basemvvm

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.samagra.commons.CompositeDisposableHelper
import com.samagra.commons.R
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.loadGif

abstract class BaseFragment<VB : ViewDataBinding, T : BaseViewModel> : Fragment() {

    @LayoutRes
    protected abstract fun layoutId(): Int
    private lateinit var progressDialog: ProgressDialog
    protected var appCompatActivity: AppCompatActivity? = null
    private var _binding: VB? = null
    val binding get() = _binding!!

    private var _viewModel: T? = null
    val viewModel: T
        get() = _viewModel!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        _viewModel = getBaseViewModel()
        _binding?.setVariable(getBindingVariable(), _viewModel)
        _binding?.lifecycleOwner = this
        _binding?.executePendingBindings()
        setProgressbar(appCompatActivity as Context)
        CompositeDisposableHelper.initialize()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CompositeDisposableHelper.destroyCompositeDisposable()
    }

    override fun onAttach(activity: Activity) {
        @Suppress("DEPRECATION")
        super.onAttach(activity)
        if (activity is AppCompatActivity)
            appCompatActivity = activity
    }

    open fun onBackPressed() {}

    fun firstTimeCreated(savedInstanceState: Bundle?) = savedInstanceState == null

    fun hideKeyboard() {
        val imm: InputMethodManager =
            appCompatActivity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = appCompatActivity?.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDetach() {
        appCompatActivity = null
        super.onDetach()
    }

    abstract fun getBaseViewModel(): T?

    abstract fun getBindingVariable(): Int

    private fun setProgressbar(context: Context) {
        progressDialog = ProgressDialog(context)
        with(progressDialog) {
//            this.setTitle(getString(R.string.sending_the_request))
            this.setMessage(getString(R.string.please_wait))
            this.setCancelable(false)
            this.isIndeterminate = true
        }
    }

    fun showProgressBar() {
        progressDialog.show()
    }

    fun hideProgressBar() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    //Chatbot
    protected fun showChatbot(
        animate: Boolean,
        botView: View,
        botIconView: ImageView,
        @DrawableRes imageIconRes: Int,
        @DrawableRes animationGifRes: Int,
        intentOnClick: Intent
    ) {
        botView.visibility = View.VISIBLE
        if (animate) {
            context?.loadGif(
                view = botIconView,
                gifResource = animationGifRes,
                placeHolder = imageIconRes,
                loopCount = 1
            )
        } else {
            Glide.with(this).load(imageIconRes).into(botIconView)
        }
        botView.setOnClickListener {
            startActivity(intentOnClick)
            logChatbotInitiate()
        }

    }

    private fun logChatbotInitiate() {
        val context = activity ?: return
        val properties = PostHogManager.createProperties(
            page = DASHBOARD_SCREEN,
            eventType = EVENT_TYPE_USER_ACTION,
            eid = EID_INTERACT,
            context = PostHogManager.createContext(
                id = APP_ID,
                pid = NL_APP_DASHBOARD,
                dataList = ArrayList()
            ),
            eData = Edata(NL_DASHBOARD, TYPE_CLICK),
            objectData = Object.Builder().id(BOT_INITIATION_BUTTON).type(OBJ_TYPE_UI_ELEMENT)
                .build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(
            context = context,
            eventName = EVENT_CHATBOT_INITIATE,
            properties = properties
        )

    }
}