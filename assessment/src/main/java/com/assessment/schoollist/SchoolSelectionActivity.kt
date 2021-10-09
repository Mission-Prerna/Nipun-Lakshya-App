package com.assessment.schoollist

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessment.R
import com.assessment.databinding.ActivitySchoolSelectionBinding
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.flow.workflowengine.spinner.SpinnerFieldWidget
import com.assessment.schoollist.model.SchoolUiModel
import com.assessment.studentselection.ExaminerStudentSelectionActivity
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.data.helper.ObjectConvertor.toSchool
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.SnackbarUtils
import com.samagra.commons.basemvvm.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SchoolSelectionActivity : BaseActivity<ActivitySchoolSelectionBinding, SchoolSelectionVM>() {

    private lateinit var selectedSchoolData: SchoolDetailsWithReportHistory
    private lateinit var prefs: CommonsPrefsHelperImpl
    private lateinit var districtSpinner: SpinnerFieldWidget
    private lateinit var blockSpinner: SpinnerFieldWidget
    private lateinit var npSpinner: SpinnerFieldWidget

    private lateinit var visitList: ArrayList<SchoolDetailsWithReportHistory>
    private lateinit var mainSchoolList: ArrayList<SchoolDetailsWithReportHistory>
    private lateinit var blockFilterSchoolList: ArrayList<SchoolDetailsWithReportHistory>
    private lateinit var npFilterSchoolList: ArrayList<SchoolDetailsWithReportHistory>
    private lateinit var districtFilterSchoolList: ArrayList<SchoolDetailsWithReportHistory>
    private lateinit var searchList: ArrayList<SchoolDetailsWithReportHistory>
    private lateinit var schoolList: ArrayList<SchoolDetailsWithReportHistory>
    private lateinit var schoolListAdapter: SchoolListAdapter
    private lateinit var districtList: ArrayList<String>
    private lateinit var blockList: ArrayList<String>
    private lateinit var npList: ArrayList<String>
    private var isValidCycle = false

    @LayoutRes
    override fun layoutRes() = R.layout.activity_school_selection

    override fun getBaseViewModel(): SchoolSelectionVM {
        val viewModel: SchoolSelectionVM by viewModels()
        return viewModel
    }

    override fun getBindingVariable() = 0

    override fun onLoadData() {
        setupToolbar()
        setListeners()
        showProgressBar()
        prefs = CommonsPrefsHelperImpl(this, "prefs")
        initList()

        setUdiseSearchWatcher()
        binding.checkboxVisit.isChecked = false
        binding.checkboxVisit.setOnCheckedChangeListener { _, isChecked ->
            val list = viewModel.onCheckboxVisitStateChanged(isChecked, getList())
            schoolListAdapter.updateAdapter(list, isValidCycle)
        }
        setObservers()
        viewModel.getSchools(this@SchoolSelectionActivity)
    }

    private fun setListeners() {
        binding.includeToolbar.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupToolbar() {
        prefs = CommonsPrefsHelperImpl(this, "")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.select_school)
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun getList(): ArrayList<SchoolDetailsWithReportHistory> {
        return if (binding.etSearch.text.isNotEmpty() && districtSpinner.selectedItem.isNullOrEmpty()) {
            searchList
        } else {
            mainSchoolList
        }
    }

    //TODO Tech Debt: Charanpreet -> Move this to VM
    private fun initList() {
        districtList = ArrayList()
        blockList = ArrayList()
        npList = ArrayList()
        searchList = ArrayList()
        visitList = ArrayList()
        mainSchoolList = ArrayList()
        setRvSchoolList(null)
    }

    private fun setUdiseSearchWatcher() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchList.clear()
                mainSchoolList.forEachIndexed { _, schoolsData ->
                    if (schoolsData.udise.toString().contains(s!!)) {
                        searchList.add(schoolsData)
                    }
                }
                binding.checkboxVisit.isChecked = false
                if (this@SchoolSelectionActivity::schoolListAdapter.isInitialized) {
                    Log.i("update adapter", "onTextChanged")
                    schoolListAdapter.updateAdapter(searchList, isValidCycle)
                } else {
                    setRvSchoolList(searchList)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun setObservers() {
        viewModel.schoolSelectionState.observe(this) { state ->
            when (state) {
                is SchoolSelectionState.Error -> handleError(state.error)
                is SchoolSelectionState.Success -> handleSuccessState(state)
            }
        }
    }

    private fun handleError(error: Throwable) {
        Timber.e(error, "handleError: School Selection error")
        showToast(R.string.invalid_cycle)
        finish()
    }

    private fun handleSuccessState(state: SchoolSelectionState.Success) {
        if (state.hideLoader) {
            hideProgressBar()
        } else {
            showProgressBar()
        }

        val listData = state.schoolsReportHistory
        districtFilterSchoolList =
            listData.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>
        schoolList = listData.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>
        mainSchoolList = listData.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>
        isValidCycle = state.validCyle

        if (!listData.isNullOrEmpty()) {
            districtList.clear()
            listData.forEach {
                if (!districtList.equals(it.district)) {
                    districtList.add(it.district!!)
                }

            }
        }
        blockList.add("Select")
        npList.add("Select")
        schoolListAdapter.updateAdapter(mainSchoolList, isValidCycle)
        setSpinners()
        hideProgressBar()
    }

    private fun setSpinners() {
        if (!this@SchoolSelectionActivity::districtSpinner.isInitialized) {
            districtSpinner = setSpinnerLists(
                districtList.distinct().toTypedArray(),
                "Select district",
                R.string.district_hindi
            )
            setSpinnerClickCallbacks(districtSpinner)
            districtSpinner.selectedPosition = if (districtSpinner.count > 1) 1 else 0
            districtSpinner.performClick()
        }
        setupBlockSpinnerData()
    }

    private fun setupBlockSpinnerData() {
        blockList.clear()
        npList.clear()
        if (this@SchoolSelectionActivity::blockSpinner.isInitialized) {
            blockSpinner.selectedPosition = 0
        }
        if (this@SchoolSelectionActivity::npSpinner.isInitialized) {
            npSpinner.selectedPosition = 0
        }
        val selectedDistrict = getSelectedDistrict()
        val selectedBlock = getSelectedBlock()
        val allSchoolList =
            districtFilterSchoolList.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>

        allSchoolList.forEach {
            if (selectedDistrict.isNullOrEmpty() || it.district == selectedDistrict) {
                if (!blockList.equals(it?.block)) {
                    blockList.add(it.block ?: "")
                }
            }

            if ((selectedDistrict.isNullOrEmpty() || it.district == selectedDistrict)
                && (selectedBlock.isNullOrEmpty() || it.block == selectedBlock)
            ) {
                if (!npList.equals(it?.nyayPanchayat)) {
                    npList.add(it.nyayPanchayat ?: "")
                }
            }
        }


        if (!this@SchoolSelectionActivity::blockSpinner.isInitialized) {
            blockSpinner = setSpinnerLists(
                blockList.distinct().toTypedArray(),
                "Select block",
                R.string.block_hindi
            )
            setBlockSelectionListener()
        } else {
            resetSpinnerData(
                blockSpinner, blockList, "Select block",
                R.string.block_hindi
            )
        }

        if (!this@SchoolSelectionActivity::npSpinner.isInitialized) {
            npSpinner = setSpinnerLists(
                npList.distinct().toTypedArray(),
                "Select NP",
                R.string.nyay_panchayat_hindi
            )
            if (::blockFilterSchoolList.isInitialized)
                setNPSelectionListener()
        } else {
            resetSpinnerData(
                npSpinner, npList, "Select NP",
                R.string.nyay_panchayat_hindi
            )
        }

    }

    private fun getSelectedDistrict(): String? {
        return if (this@SchoolSelectionActivity::districtSpinner.isInitialized) {
            districtSpinner.selectedItem;
        } else {
            null
        }
    }

    private fun getSelectedBlock(): String? {
        if (this@SchoolSelectionActivity::blockSpinner.isInitialized) {
            return blockSpinner.selectedItem;
        } else {
            return null
        }
    }

    private fun setSpinnerLists(
        list: Array<String>,
        label: String,
        textResId: Int
    ): SpinnerFieldWidget {
        val districtSpinner = SpinnerFieldWidget(this)
        districtSpinner.setListData(list, label, true, textResId)
        binding.llView.addView(districtSpinner)
        return districtSpinner
    }

    private fun setSpinnerClickCallbacks(
        districtSpinner: SpinnerFieldWidget
    ) {
        districtSpinner.setSelectionCallback { item, _ ->
            if (!item.contains("Select")) {
                districtFilterSchoolList.clear()
                districtFilterSchoolList =
                    schoolList.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>
                setupBlockSpinnerData()
                addDataInMainList(viewModel.districtFilterSchoolList(districtFilterSchoolList, item))
            } else {
                SnackbarUtils.showShortSnackbar(
                    binding.llView,
                    getString(R.string.no_district_found_sync_again)
                )
            }
        }
    }

    private fun setBlockSelectionListener() {
        blockSpinner.setSelectionCallback { item, _ ->
            if (!item.contains("Select")) {
                blockFilterSchoolList =
                    districtFilterSchoolList.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>
                val selectedDistrict = getSelectedDistrict()

                npList.clear()
                blockFilterSchoolList.forEachIndexed { _, schoolsData ->
                    if (schoolsData.block == item && schoolsData.district == selectedDistrict) {
                        npList.add(schoolsData.nyayPanchayat!!)
                    }
                }
                resetSpinnerData(npSpinner, npList, "Select NP", R.string.nyay_panchayat_hindi)
                addDataInMainList(viewModel.blockFilterSchoolList(blockFilterSchoolList,item, selectedDistrict))
                setNPSelectionListener()
            } else {
                SnackbarUtils.showShortSnackbar(
                    binding.llView,
                    getString(R.string.select_district_first_prompt)
                )
            }

        }
    }

    private fun setNPSelectionListener() {
        npSpinner.setSelectionCallback { item, _ ->
            if (item != "Select") {
                npFilterSchoolList =
                    blockFilterSchoolList.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>
                val selectedDistrict = getSelectedDistrict()
                val selectedBlock = getSelectedBlock()
                addDataInMainList(viewModel.npFilterSchoolList(npFilterSchoolList,item, selectedDistrict, selectedBlock))
            } else {
                SnackbarUtils.showShortSnackbar(
                    binding.llView,
                    getString(R.string.select_block_first_prompt)
                )
            }
        }
    }

    private fun addDataInMainList(list: ArrayList<SchoolDetailsWithReportHistory>) {
        if (binding.etSearch.text.isNotEmpty()) {
            binding.etSearch.text.clear()
        }
        mainSchoolList.clear()
        mainSchoolList = list.toMutableList() as ArrayList<SchoolDetailsWithReportHistory>
        Log.i("update adapter", "addDataInMainList")
        schoolListAdapter.updateAdapter(mainSchoolList, isValidCycle)
        binding.checkboxVisit.isChecked = false
    }

    private fun resetSpinnerData(
        spinner: SpinnerFieldWidget,
        spinnerList: ArrayList<String>,
        label: String,
        textResId: Int
    ) {
        spinner.setListData(spinnerList.distinct().toTypedArray(), label, true, textResId)
    }

    private fun setRvSchoolList(listData: List<SchoolDetailsWithReportHistory>?) {
        binding.rvSchoolList.layoutManager = LinearLayoutManager(this)
        var list: List<SchoolUiModel>? = null
        if (listData != null) {
            list = listData.map { SchoolUiModel(it, isValidCycle) }
        }
        schoolListAdapter = SchoolListAdapter(this, list) {
            selectedSchoolData = it
            Timber.d("Selected school is : $selectedSchoolData")
            viewModel.setPostHogEventSelectSchool(it, this)
            if (this::selectedSchoolData.isInitialized) {
                redirectToStudentSelectionScreen(selectedSchoolData)
            }
        }
        binding.rvSchoolList.adapter = schoolListAdapter
    }

    private fun redirectToStudentSelectionScreen(schoolsData: SchoolDetailsWithReportHistory) {
        val intent = Intent(this, ExaminerStudentSelectionActivity::class.java)
        intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData.toSchool())
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}