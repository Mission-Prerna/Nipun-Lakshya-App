package com.samagra.parent.ui.assessmentsetup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseFragment
import com.samagra.commons.constants.Constants
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.parent.AppConstants
import com.samagra.parent.BR
import com.samagra.parent.R
import com.samagra.parent.ViewModelProviderFactory
import com.samagra.parent.databinding.FragmentAssessmentSetupBinding
import com.samagra.parent.ui.SpinnerFieldWidget
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import com.samagra.parent.ui.dietmentorassessmenttype.DIETAssessmentTypeActivity
import com.samagra.parent.ui.withArgs
import org.odk.collect.android.utilities.SnackbarUtils
import timber.log.Timber
import java.util.*

class AssessmentSetupFragment : BaseFragment<FragmentAssessmentSetupBinding, AssessmentSetupVM>() {

    private lateinit var selectedSchoolData: SchoolsData
    private lateinit var prefs: CommonsPrefsHelperImpl
    private lateinit var districtSpinner: SpinnerFieldWidget
    private lateinit var visitList: ArrayList<SchoolsData>
    private lateinit var mainSchoolList: ArrayList<SchoolsData>
    private lateinit var blockFilterSchoolList: ArrayList<SchoolsData>
    private lateinit var npFilterSchoolList: ArrayList<SchoolsData>
    private lateinit var districtFilterSchoolList: ArrayList<SchoolsData>
    private lateinit var searchList: ArrayList<SchoolsData>
    private lateinit var schoolList: ArrayList<SchoolsData>
    private lateinit var schoolListAdapter: SchoolListAdapter
    private lateinit var districtList: ArrayList<String>
    private lateinit var blockList: ArrayList<String>
    private lateinit var npList: ArrayList<String>

    @LayoutRes
    override fun layoutId() = R.layout.fragment_assessment_setup

    override fun getBaseViewModel(): AssessmentSetupVM {
        val repository = AssessmentSetupRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(appCompatActivity!!.application, repository)
        return ViewModelProvider(
            this,
            viewModelProviderFactory
        )[AssessmentSetupVM::class.java]
    }

    override fun getBindingVariable() = BR.assessmentSetupVm

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressBar()
        prefs = CommonsPrefsHelperImpl(activity as Context, "prefs")
        initList()
        setObservers()
        viewModel.getSchoolsData()
        setUdiseSearchWatcher()
        binding.checkboxVisit.isChecked = false

        binding.checkboxVisit.setOnCheckedChangeListener { _, isChecked ->
            run {
                val list = getList()

                if (isChecked) {
                    visitList.clear()
                    visitList = list.toMutableList() as ArrayList<SchoolsData>
                    val iterator = visitList.iterator()
                    while (iterator.hasNext()) {
                        val dis = iterator.next()
                        if (dis.visitStatus!!) {
                            iterator.remove()
                        }
                        schoolListAdapter.updateAdapter(visitList)
                    }
                    Timber.d(" checktrue last visit list and size ${visitList.size} \n $visitList ")
                } else {
                    Timber.d("check false  main list list and size ${mainSchoolList.size} \n $mainSchoolList ")
                    schoolListAdapter.updateAdapter(list)
                }
            }
        }
    }

    private fun getList(): ArrayList<SchoolsData> {
        return if (binding.etSearch.text.isNotEmpty() && districtSpinner.selectedItem.isNullOrEmpty()) {
            searchList
        } else {
            mainSchoolList
        }
    }

    private fun initList() {
        districtList = ArrayList()
        blockList = ArrayList()
        npList = ArrayList()
        searchList = ArrayList()
        visitList = ArrayList()
        mainSchoolList = ArrayList()
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
                if (this@AssessmentSetupFragment::schoolListAdapter.isInitialized) {
                    schoolListAdapter.updateAdapter(searchList)
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
        viewModel.schoolDataList.observe(viewLifecycleOwner) { listData ->
            districtFilterSchoolList = listData.toMutableList() as ArrayList<SchoolsData>
            schoolList = listData.toMutableList() as ArrayList<SchoolsData>
            mainSchoolList = listData.toMutableList() as ArrayList<SchoolsData>

            if (!listData.isNullOrEmpty()) {
                districtList.clear()
                listData.forEach {
                    if (!districtList.equals(it.district)) {
                        districtList.add(it.district!!)
                    }
                }
            } else {
                districtList.add("Select")
            }

            blockList.add("Select")
            npList.add("Select")

            setRvSchoolList(listData)
            setSpinners()
            hideProgressBar()
        }
    }

    private fun setSpinners() {
        districtSpinner = setSpinnerLists(
            districtList.distinct().toTypedArray(),
            districtList.distinct()[0],
            R.string.district_hindi
        )
        var blockLabel = ""
        setupBlockSpinnerData()
        districtFilterSchoolList.forEach {
            if (it.districtId == prefs.mentorDetailsData.district_id) {
                blockList.add(it.block ?: "")
            }
            if (blockLabel.isEmpty() && prefs.mentorDetailsData.block_id == it.blockId) {
                blockLabel = blockList[0]
            }
        }
        val blockSpinner = setSpinnerLists(
            blockList.distinct().toTypedArray(),
            blockLabel,
            R.string.block_hindi
        )
        val npSpinner = setSpinnerLists(
            npList.distinct().toTypedArray(),
            "",
            R.string.nyay_panchayat_hindi
        )
        setSpinnerClickCallbacks(districtSpinner, blockSpinner, npSpinner)
    }

    private fun setupBlockSpinnerData() {
        if (prefs.mentorDetailsData.block_id > 0) {
            blockList.clear()
            npList.clear()
            blockFilterSchoolList =
                districtFilterSchoolList.toMutableList() as ArrayList<SchoolsData>
            blockFilterSchoolList.forEachIndexed { _, schoolsData ->
                if (schoolsData.blockId == prefs.mentorDetailsData.block_id) {
                    npList.add(schoolsData.nyayPanchayat ?: "")
                }
            }
            val iterator = blockFilterSchoolList.iterator()
            while (iterator.hasNext()) {
                val dis = iterator.next()
                if (dis.blockId != prefs.mentorDetailsData.block_id) {
                    iterator.remove()
                }
            }
            addDataInMainList(blockFilterSchoolList)
        }
    }

    private fun setSpinnerLists(
        list: Array<String>,
        label: String,
        textResId: Int
    ): SpinnerFieldWidget {
        val districtSpinner = SpinnerFieldWidget(appCompatActivity as Context)
        districtSpinner.setListData(list, label, true, textResId)
        binding.llView.addView(districtSpinner)
        return districtSpinner
    }

    private fun setSpinnerClickCallbacks(
        districtSpinner: SpinnerFieldWidget,
        blockSpinner: SpinnerFieldWidget,
        npSpinner: SpinnerFieldWidget
    ) {

        districtSpinner.setSelectionCallback { item, _ ->
            if (item != "Select") {
                districtFilterSchoolList.clear()
                districtFilterSchoolList = schoolList.toMutableList() as ArrayList<SchoolsData>
                blockList.clear()
                districtFilterSchoolList.forEachIndexed { _, schoolsData ->
                    if (schoolsData.district == item) {
                        blockList.add(schoolsData.block!!)
                    }
                }
                var labal = ""
                if (prefs.mentorDetailsData.block_id > 0) {
                    labal = blockList[0]
                }
                resetSpinnerData(blockSpinner, blockList, labal, R.string.block_hindi)
                resetSpinnerData(npSpinner, npList, "", R.string.nyay_panchayat_hindi)
                val iterator = districtFilterSchoolList.iterator()
                while (iterator.hasNext()) {
                    val dis = iterator.next()
                    if (dis.district != item) {
                        iterator.remove()
                    }
                }
                addDataInMainList(districtFilterSchoolList)
            } else {
                SnackbarUtils.showShortSnackbar(
                    binding.llView,
                    getString(R.string.no_district_found_sync_again)
                )
            }
        }

        blockSpinner.setSelectionCallback { item, _ ->
            if (item != "Select") {
                blockFilterSchoolList =
                    districtFilterSchoolList.toMutableList() as ArrayList<SchoolsData>

                npList.clear()
                blockFilterSchoolList.forEachIndexed { _, schoolsData ->
                    if (schoolsData.block == item) {
                        npList.add(schoolsData.nyayPanchayat!!)
                    }
                }

                resetSpinnerData(npSpinner, npList, "", R.string.nyay_panchayat_hindi)
                val iterator = blockFilterSchoolList.iterator()
                while (iterator.hasNext()) {
                    val dis = iterator.next()
                    if (dis.block != item) {
                        iterator.remove()
                    }
                }
                addDataInMainList(blockFilterSchoolList)

            } else {
                SnackbarUtils.showShortSnackbar(
                    binding.llView,
                    getString(R.string.select_district_first_prompt)
                )
            }

        }

        npSpinner.setSelectionCallback { item, _ ->
            if (item != "Select") {
                npFilterSchoolList = blockFilterSchoolList.toMutableList() as ArrayList<SchoolsData>
                val iterator = npFilterSchoolList.iterator()
                while (iterator.hasNext()) {
                    val dis = iterator.next()
                    if (dis.nyayPanchayat != item) {
                        iterator.remove()
                    }
                }
                addDataInMainList(npFilterSchoolList)

            } else {
                SnackbarUtils.showShortSnackbar(
                    binding.llView,
                    getString(R.string.select_block_first_prompt)
                )
            }
        }
    }

    private fun addDataInMainList(list: ArrayList<SchoolsData>) {
        if (binding.etSearch.text.isNotEmpty()) {
            binding.etSearch.text.clear()
        }
        mainSchoolList.clear()
        mainSchoolList = list.toMutableList() as ArrayList<SchoolsData>
        schoolListAdapter.updateAdapter(mainSchoolList)
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

    private fun setRvSchoolList(listData: ArrayList<SchoolsData>) {
        binding.rvSchoolList.layoutManager = LinearLayoutManager(appCompatActivity)
        schoolListAdapter = SchoolListAdapter(appCompatActivity as Context, listData) {
            selectedSchoolData = it
            Timber.d("Selected school is : $selectedSchoolData")
            setPostHogEventSelectSchool(it)
            handleRedirections()
        }
        binding.rvSchoolList.adapter = schoolListAdapter
    }

    private fun handleRedirections() {
        if (this::selectedSchoolData.isInitialized) {
            when (prefs.selectedUser) {
                AppConstants.USER_EXAMINER, AppConstants.USER_MENTOR -> {
                    redirectToDetailsSelectionScreen(selectedSchoolData)
                }
                Constants.USER_DIET_MENTOR -> {
                    if (prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT) {
                        redirectToDetailsSelectionScreen(selectedSchoolData)
                    } else if (prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_SPOT_ASSESSMENT) {
                        redirectToDetailsSelectionScreen(selectedSchoolData)
                    } else {
                        redirectionToAssessmentSelectionScreen(selectedSchoolData)
                    }
                }
                else -> {
                    redirectToDetailsSelectionScreen(selectedSchoolData)
                }
            }
        }
    }

    /*
    * Spot assessment, State led assessment, Nipun Abhyas flow selection
    * */
    private fun redirectionToAssessmentSelectionScreen(schoolsData: SchoolsData) {
        val intent = Intent(activity, DIETAssessmentTypeActivity::class.java)
        intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
        activity?.startActivity(intent)
    }

    private fun redirectToDetailsSelectionScreen(schoolsData: SchoolsData) {
        val intent = Intent(activity, DetailsSelectionActivity::class.java)
        intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
        activity?.startActivity(intent)
    }

    private fun setPostHogEventSelectSchool(schoolsData: SchoolsData) {
        val cDataList = ArrayList<Cdata>()
        if (schoolsData.schoolName != null) {
            cDataList.add(Cdata("schoolName", schoolsData.schoolName))
        }
        if (schoolsData.visitStatus != null) {
            cDataList.add(Cdata("isVisited", "${schoolsData.visitStatus}"))
        }
        if (schoolsData.udise != null) {
            cDataList.add(Cdata("UDISE", schoolsData.udise.toString()))
        }
        cDataList.add(Cdata("dateOfSelection", Date().time.toString()))
        val properties = PostHogManager.createProperties(
            page = SCHOOLS_SELECTION_SCREEN,
            eventType = EVENT_TYPE_USER_ACTION,
            eid = EID_INTERACT,
            context = PostHogManager.createContext(APP_ID, NL_APP_SCHOOL_SELECTION, cDataList),
            eData = Edata(NL_SCHOOL_SELECTION, TYPE_CLICK),
            objectData = Object.Builder().id(SELECT_SCHOOL_BUTTON).type(OBJ_TYPE_UI_ELEMENT)
                .build(),
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context!!, EVENT_SCHOOL_SELECTION, properties)
//        Log.e(POST_HOG_LOG_TAG, "assessment setup screen $properties $EVENT_SCHOOL_SELECTION")

    }

    companion object {
        fun newInstance(): AssessmentSetupFragment = AssessmentSetupFragment().withArgs {
            putString("KeyConstants.PHONE_NUMBER", "mobileNo")
        }
    }
}