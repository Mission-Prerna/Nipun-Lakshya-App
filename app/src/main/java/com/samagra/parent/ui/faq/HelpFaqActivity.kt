package com.samagra.parent.ui.faq

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.constants.Constants
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.utils.CommonWebViewActivity
import com.samagra.parent.BR
import com.samagra.parent.R
import com.samagra.parent.ViewModelProviderFactory
import com.samagra.parent.data.KnowledgeResourceData
import com.samagra.parent.databinding.ActivityHelpFaqBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.assessmenthome.AssessmentHomeVM
import timber.log.Timber

class HelpFaqActivity : BaseActivity<ActivityHelpFaqBinding, AssessmentHomeVM>(),
    RecyclerResourcesItemListener {
    private lateinit var helpFaqJson: String
    private var urlFromConfig: String = ""
    private var knowledgeResourceAdapter: KnowledgeResourceAdapter? = null
    private var onUserCallbackListener: RecyclerResourcesItemListener? = null

    @LayoutRes
    override fun layoutRes() = R.layout.activity_help_faq

    override fun getBaseViewModel(): AssessmentHomeVM {
        val setupRepository = DataSyncRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(this.application, setupRepository)
        return ViewModelProvider(
            this,
            viewModelProviderFactory
        )[AssessmentHomeVM::class.java]
    }

    override fun getBindingVariable() = BR.assessmentHomeVm

    override fun onLoadData() {
        setupToolbar()
        setFooterCTA()
        onUserCallbackListener = this
        getFaqListFromFirebase()
        setObservers()
    }

    private fun setObservers() {
        with(viewModel) {
            observe(helpFaqList, ::handleHelpFaqList)
            observe(helpFaqFormUrl, ::handleHelpFaqFormUrl)
        }
    }

    private fun handleHelpFaqFormUrl(formUrl: String?) {
        formUrl?.let {
            urlFromConfig = it
            Timber.e("form url $it")
        }
    }

    private fun handleHelpFaqList(faqList: String?) {
        faqList?.let {
            helpFaqJson = it
            setupAdapter()
        }
    }

    private fun setupAdapter() {
        var knowledgeResourceList: List<KnowledgeResourceData> = ArrayList()
        if (helpFaqJson.isNotEmpty()) {
            try {
                val gson = Gson()
                val type = object : TypeToken<List<KnowledgeResourceData?>?>() {}.type
                knowledgeResourceList = gson.fromJson(helpFaqJson, type)
            } catch (e: Exception) {
                Timber.e("$e")
            }
        }
        if (!(knowledgeResourceList.isNullOrEmpty())) {
            binding.reEmpty.visibility = View.GONE
            binding.rvFaq.visibility = View.VISIBLE
            val layoutManager = LinearLayoutManager(this)
            binding.rvFaq.layoutManager = layoutManager
            binding.rvFaq.setHasFixedSize(true)
            knowledgeResourceAdapter =
                KnowledgeResourceAdapter(knowledgeResourceList, onUserCallbackListener)
            binding.rvFaq.adapter = knowledgeResourceAdapter
            knowledgeResourceAdapter?.notifyDataSetChanged()
        } else {
            binding.reEmpty.visibility = View.VISIBLE
            binding.rvFaq.visibility = View.GONE
        }
    }

    private fun getFaqListFromFirebase() {
        viewModel.getHelpFaqList()
        viewModel.getHelpFaqFormUrl()
    }

    private fun setFooterCTA() {
        binding.mtlBtnLink.text = getString(R.string.helpline_form)
        binding.mtlBtnLink.setOnClickListener {
            val intent = Intent(this, CommonWebViewActivity::class.java)
            if (urlFromConfig.isEmpty()) {
                urlFromConfig = "https://www.google.co.in/"
                Timber.e("help section url empty so open google.com")
            }
            intent.putExtra(Constants.OPEN_URL, urlFromConfig)
            intent.putExtra(Constants.OPEN_URL_TITLE, "FAQ")
            startActivity(intent)
        }
    }

    fun setupToolbar() {
        try {
            binding.incToolbar.toolbar.title = getString(R.string.help)
            setSupportActionBar(binding.incToolbar.toolbar)
            binding.incToolbar.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            binding.incToolbar.toolbar.setNavigationOnClickListener { v: View? -> finish() }
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
            binding.incToolbar.toolbar.setNavigationOnClickListener { view: View? -> finish() }
        } catch (e: Exception) {
            Timber.e("Help FAQ screen toolbar exception : $e")
        }
    }

    override fun openResourcesDetails(
        resourcesItem: KnowledgeResourceData,
        faqTitle: TextView,
        faqDocumentLink: TextView
    ) {
        //adapter listener
    }
}