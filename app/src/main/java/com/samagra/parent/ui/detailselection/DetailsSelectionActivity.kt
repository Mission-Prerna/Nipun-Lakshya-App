package com.samagra.parent.ui.detailselection

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assets.uielements.CustomMessageDialog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.GeofencingHelper
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.constants.Constants
import com.samagra.commons.helper.GatekeeperHelper
import com.samagra.commons.models.metadata.CompetencyModel
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.*
import com.samagra.parent.AppConstants.INTENT_SELECTED_GRADE
import com.samagra.parent.AppConstants.INTENT_SELECTED_SUBJECT
import com.samagra.parent.databinding.ActivitySelectionBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.SpinnerFieldWidget
import com.samagra.parent.ui.competencyselection.CompetencySelectionActivity
import com.samagra.parent.ui.competencyselection.readonlycompetency.ReadOnlyCompetencyActivity
import com.samagra.parent.ui.logout.GeofenceUI
import com.samagra.workflowengine.workflow.WorkflowUtils
import com.samagra.workflowengine.workflow.model.WorkflowConfig
import org.odk.collect.android.utilities.SnackbarUtils
import org.odk.collect.android.utilities.ToastUtils
import timber.log.Timber

const val REQUEST_CHECK_SETTINGS: Int = 122

open class DetailsSelectionActivity : BaseActivity<ActivitySelectionBinding, DetailsSelectionVM>() {

    private lateinit var hashMap2: java.util.HashMap<String, SubjectModel>
    private lateinit var subjectSpnList: java.util.ArrayList<String>
    private lateinit var hashMap1: java.util.HashMap<String, String>
    private lateinit var assessmentTypeSpnList: java.util.ArrayList<String>
    private var selectedStudentCount: String? = "0"
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var schoolsData: SchoolsData? = null
    private var selectedSubject: String = ""
    private var selectedGrade: String = ""
    private var selectedAssessmentType: String = ""
    private val viewModels: DetailsSelectionVM by viewModels()
    private val geofencingConfig by lazy {
        GeofencingHelper.parseGeofencingConfig()
    }
    private var rvSubjects: RecyclerView? = null
    private var mConfigMapper: Map<Int, Set<String>>? = null
    private var subAdapter: SubjectAdapter? = null
    private var classAdapter: ClassAdapter? = null
    private lateinit var workflowConfig: WorkflowConfig
    private lateinit var arrayList: ArrayList<String>
    private lateinit var hashMap: HashMap<String, ClassModel>
    private var geofencingRadius: Int? = 0
    private var startLocationMatchDialog: CustomMessageDialog? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrElse(Manifest.permission.ACCESS_FINE_LOCATION) { false } -> {
                changeLocationSetting()
            }

            permissions.getOrElse(Manifest.permission.ACCESS_COARSE_LOCATION) { false } -> {
                changeLocationSetting()
            }

            else -> {
                //handle denied permissions
                finish()
            }
        }
    }

    @LayoutRes
    override fun layoutRes() = R.layout.activity_selection

    override fun getBaseViewModel(): DetailsSelectionVM {
        val dataSyncRepo = DataSyncRepository()
        val viewModelProviderFactory = ViewModelProviderFactory(this.application, dataSyncRepo)
        return ViewModelProvider(
            this, viewModelProviderFactory
        )[DetailsSelectionVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkGeofencingEnabled()
            && schoolsData?.geofencingEnabled == true
        ) {
            launchGeofencingFlow()
        } else {
            //do something when required
        }
    }

    override fun getBindingVariable() = BR.selectionVm

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getDataFromIntent()
    }

    override fun onResume() {
        super.onResume()
        getDataFromIntent()
    }

    override fun onLoadData() {
        initPreferences()
        getDataFromIntent()
        // todo by neeraj
        setupUINew()
        setupToolbar()
        setClickListeners()
        setSpinnerWithStudentCount()
        setObservers()
        viewModels.getCompetenciesData(prefs)
        GatekeeperHelper.assess(
            context = this,
            actor = prefs.selectedUser
        )
    }

    //FIX for https://console.firebase.google.com/project/mission-prerna/crashlytics/app/android:org.samagra.missionPrerna/issues/8bb6124f12df07fc858fc7a21884471b
    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun setSpinner() {
        arrayList = ArrayList()
        hashMap = HashMap()
        val arrayListForClassModel: ArrayList<ClassModel> =
            UtilityFunctions.getClassData(mConfigMapper) as ArrayList<ClassModel>
        arrayList.add(CommonConstants.SELECT)
        arrayListForClassModel.sortBy { it.value }
        if (arrayListForClassModel?.size > 0) {
            for (classmodel: ClassModel in arrayListForClassModel) {
                arrayList.add(
                    getString(R.string.class_hindi) + " " + classmodel.title.substring(
                        classmodel.title.length - 1
                    )
                )
                hashMap[getString(R.string.class_hindi) + " " + classmodel.title.substring(
                    classmodel.title.length - 1
                )] = classmodel
            }
        }
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this@DetailsSelectionActivity,
            R.layout.support_simple_spinner_dropdown_item,
            arrayList
        )
        binding.spBalvatika4to8.adapter = arrayAdapter
    }

    private fun setSpinnerSubject() {
        subjectSpnList = arrayListOf()
        hashMap2 = HashMap()

        val arrayListForClassModel: ArrayList<SubjectModel> =
            UtilityFunctions.getSubjectData(mConfigMapper, null) as ArrayList<SubjectModel>
        subjectSpnList.add(CommonConstants.SELECT)

        if (arrayListForClassModel?.size > 0) {
            for (subjectModel: SubjectModel in arrayListForClassModel) {
                subjectSpnList.add(getSubjectInHindi(subjectModel))
                hashMap2[getSubjectInHindi(subjectModel)] = subjectModel
            }
        }
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this@DetailsSelectionActivity,
            R.layout.support_simple_spinner_dropdown_item,
            subjectSpnList
        )
        binding.spSubject.adapter = arrayAdapter
    }

    private fun getSubjectInHindi(subjectModel: SubjectModel): String {
        return if (subjectModel.title.equals("hindi", true)) {
            getString(R.string.bhasha)
        } else {
            getString(R.string.math)
        }
    }

    private fun setSpinnerAssessmentType() {
        hashMap1 = HashMap()
        assessmentTypeSpnList = arrayListOf()
        assessmentTypeSpnList.apply {
            add(CommonConstants.SELECT)
            add(getString(R.string.nipun_abhyas_text))
            add(getString(R.string.suchi_abhyas_text))
        }
        hashMap1[getString(R.string.nipun_abhyas_text)] = AppConstants.NIPUN_ABHYAS
        hashMap1[getString(R.string.suchi_abhyas_text)] = AppConstants.SUCHI_ABHYAS
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this@DetailsSelectionActivity,
            R.layout.support_simple_spinner_dropdown_item,
            assessmentTypeSpnList
        )
        binding.spAssessmentType.adapter = arrayAdapter
    }

    private fun setSpinnerWithStudentCount() {
        val list = ArrayList<String>()
        for (i in 1..5) {
            list.add(i.toString())
        }
        val spinner: SpinnerFieldWidget = setSpinnerLists(list)
        setSpinnerListener(spinner)
    }

    private fun setSpinnerListener(spinner: SpinnerFieldWidget) {
        spinner.setSelectionCallback { item, _ ->
            selectedStudentCount = item
        }
    }

    private fun initPreferences() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun setSpinnerLists(list: ArrayList<String>): SpinnerFieldWidget {
        val spinner = SpinnerFieldWidget(this)
        binding.llSpnStudents.addView(spinner)
        spinner.setListData(
            list.distinct().toTypedArray(), getString(R.string.students), false, 0
        )
        return spinner
    }

    private fun getAndParseFlowConfig() {
        mConfigMapper = UtilityFunctions.parseFlowConfig(workflowConfig)
    }

    private fun setupUINew() {
        hideSpinner()
        binding.llInfo.root.setVisible(false)
        binding.viewLineDownFromGrade.setVisible(false)
        binding.tvBothTitle.text = "${getString(R.string.bhasha)}-${getString(R.string.math)}"
        if (prefs.selectedUser == AppConstants.USER_PARENT) {
            selectedStudentCount = "1"
            setVisibilityOfSpinners(
                gradeSpin = true,
                AssessmentTypeSpin = false,
                subjectSpin = false
            )
            setVisibilityOfSpinnerTitle(
                gradeSpin = true,
                AssessmentTypeSpin = false,
                subjectSpin = false
            )
            setupUiNipunAbhyas()
        } else if (prefs.selectedUser == AppConstants.USER_TEACHER) {
            viewModels.getInfoNote()
            setVisibilityOfSpinners(
                gradeSpin = true,
                AssessmentTypeSpin = true,
                subjectSpin = false
            )
            setVisibilityOfSpinnerTitle(
                gradeSpin = true,
                AssessmentTypeSpin = true,
                subjectSpin = false
            )
            setupUiNipunAbhyas()
        } else if (prefs.selectedUser == AppConstants.USER_MENTOR) {
            prefs.saveAssessmentType(AppConstants.NIPUN_ABHYAS)
            setVisibilityOfSpinners(
                gradeSpin = true,
                AssessmentTypeSpin = false,
                subjectSpin = false
            )
            setVisibilityOfSpinnerTitle(
                gradeSpin = true,
                AssessmentTypeSpin = false,
                subjectSpin = false
            )
            setupUiNipunAbhyas()
        } else if (prefs.selectedUser == Constants.USER_DIET_MENTOR) {
            when (prefs.saveSelectStateLedAssessment) {
                AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT -> {
                    setVisibilityOfSpinners(
                        gradeSpin = true,
                        AssessmentTypeSpin = false,
                        subjectSpin = false
                    )
                    setVisibilityOfSpinnerTitle(
                        gradeSpin = true,
                        AssessmentTypeSpin = false,
                        subjectSpin = false
                    )
                    setUiForDietMentorStateLedAssessmentAndExaminer()
                }
                else -> {
                    when (prefs.assessmentType) {
                        AppConstants.NIPUN_LAKSHYA -> {
                        }
                        AppConstants.NIPUN_SUCHI -> {
                            binding.llLakshyaContainer.visibility = View.GONE
                        }
                    }
                }
            }
        } else if (prefs.selectedUser == AppConstants.USER_EXAMINER) {
            setVisibilityOfSpinners(
                gradeSpin = true,
                AssessmentTypeSpin = false,
                subjectSpin = false
            )
            setVisibilityOfSpinnerTitle(
                gradeSpin = true,
                AssessmentTypeSpin = false,
                subjectSpin = false
            )
            setUiForDietMentorStateLedAssessmentAndExaminer()
        }
    }

    private fun setUiForDietMentorStateLedAssessmentAndExaminer() {
        binding.llLakshyaContainer.visibility = View.GONE
        binding.rvSubjects.visibility = View.GONE
        binding.hintForChosenSubject.visibility = View.GONE
        binding.chooseClasses.visibility = View.GONE
        binding.hintForDietAndExamier.visibility = View.VISIBLE
        binding.hintForDietAndExamier.text = getString(R.string.choose_instruction)
    }

    private fun setupUiNipunAbhyas() {
        binding.llLakshyaContainer.visibility = View.GONE
        binding.chooseClasses.visibility = View.VISIBLE
        val topParam = binding.chooseClasses.layoutParams as ViewGroup.MarginLayoutParams
        topParam.setMargins(0, 10, 80, 0)
        binding.rvSubjects.visibility = View.GONE
        binding.chooseClasses.layoutParams = topParam
        binding.hintForChosenSubject.visibility = View.GONE
        binding.hintForDietAndExamier.visibility = View.GONE
    }

    private fun setVisibilityOfSpinners(
        gradeSpin: Boolean,
        AssessmentTypeSpin: Boolean,
        subjectSpin: Boolean
    ) {
        binding.llBalvatika4to8.setVisible(gradeSpin)
        binding.rlAssessmentType.setVisible(AssessmentTypeSpin)
        binding.rlSubject.setVisible(subjectSpin)
    }

    private fun setVisibilityOfSpinnerTitle(
        gradeSpin: Boolean,
        AssessmentTypeSpin: Boolean,
        subjectSpin: Boolean
    ) {
        binding.txvTitle.setTextVisible(gradeSpin)
        binding.txvTitle1.setTextVisible(AssessmentTypeSpin)
        binding.txvTitle2.setTextVisible(subjectSpin)
    }

    private fun hideSpinner() {
        binding.llSpnStudents.visibility = View.GONE
        binding.noOfStudent.visibility = View.GONE
        selectedStudentCount = "1"
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(AppConstants.INTENT_SCHOOL_DATA)) {
            schoolsData =
                intent.getSerializableExtra(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.nipun_lakshya_app)
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun getConfigSettingsFromRemoteConfig(): String {
        return RemoteConfigUtils.getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.ASSESSMENT_WORKFLOW_CONFIG)
        //return AppConstants.flowConfig
    }

    private fun getConfigSettingsFromRemoteConfigExaminer(): String {
        return RemoteConfigUtils.getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.ASSESSMENT_WORKFLOW_CONFIG_EXAMINER)
    }

    private fun setObservers() {
        with(viewModel) {
            observe(competenciesList, ::handleCompetencyData)
            observe(infoNotesLiveData, ::handleInfoNote)
        }
    }

    private fun handleInfoNote(infoNote: String?) {
        binding.llInfo.root.setVisible(true)
        binding.viewLineDownFromGrade.setVisible(true)
        binding.llInfo.tvInfoNote.text = infoNote
    }

    private fun handleCompetencyData(competencyList: ArrayList<CompetencyModel>?) {
        competencyList?.let {
            setupFlowConfig(it)
            getAndParseFlowConfig()
            initClassAdapter()
            setSpinner()
            setSpinnerAssessmentType()
            initSubjectAdapter(null)
        }
    }

    private fun setupFlowConfig(competencyList: ArrayList<CompetencyModel>) {
        when (prefs.selectedUser) {
            AppConstants.USER_TEACHER -> {
                val workflowConfigStr = getConfigSettingsFromRemoteConfig()
                workflowConfig = Gson().fromJson(workflowConfigStr, WorkflowConfig::class.java)
                workflowConfig.flowConfigs =
                    WorkflowUtils.getWorkflowConfigForCompetencies(competencyList, prefs)
            }
            else -> {
                val workflowConfigStr = getConfigSettingsFromRemoteConfigExaminer()
                workflowConfig = Gson().fromJson(workflowConfigStr, WorkflowConfig::class.java)
//                competencyList.add(CompetencyModel(0, false, 4, 1, "Nipun Lakshya Math 1", "", 101, 1))
//                competencyList.add(CompetencyModel(0, false, 5, 2, "Nipun Lakshya Hindi 3", "", 102, 1))
//                competencyList.add(CompetencyModel(0, false, 6, 1, "Nipun Lakshya Math 2", "", 103, 1))
//                competencyList.add(CompetencyModel(0, false, 7, 2, "Nipun Lakshya Hindi 4", "", 104, 1))
//                competencyList.add(CompetencyModel(0, false, 8, 2, "Nipun Lakshya Hindi 5", "", 105, 1))
                workflowConfig.flowConfigs =
                    WorkflowUtils.getWorkflowConfigForCompetencies(competencyList, prefs)
            }
        }
    }

    private fun setClickListeners() {
        binding.rlBoth.setOnClickListener {
            binding.rlBoth.isSelected = true
            selectedSubject = "Hindi-Math"
            Timber.e("Selected subject : $selectedSubject")
            initSubjectAdapter(null)
            setBothButtonColor()
        }

        binding.mtlBtnSetupAssessment.setOnClickListener {
//            val redirectionIntent = Intent()
            setPostHogEventDetailsSelection()
            when (prefs.selectedUser) {
                AppConstants.USER_EXAMINER -> {
                    validateFieldsAbhyas()
                }
                AppConstants.USER_PARENT -> {
                    validateFieldsAbhyas()
                }
                /*
                * DIET mentor flows - Spot assessment, State led assessment
                *
                * */
                //todo check diet mentor by shashank
                AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                    if ((prefs.selectedUser.equals(Constants.USER_DIET_MENTOR, true))
                        && prefs.saveSelectStateLedAssessment == AppConstants.DIET_MENTOR_STATE_LED_ASSESSMENT
                    ) {
                        validateFieldsAbhyas()
                    } else {
                        when (prefs.assessmentType) {
                            AppConstants.NIPUN_LAKSHYA -> {
                                validateFieldsSpotAssessment()
                            }
                            AppConstants.NIPUN_SUCHI -> {
                                redirectToCompetencySelectionScreen()
                            }
                            AppConstants.NIPUN_ABHYAS -> {
                                validateFieldsAbhyas()
                            }
                        }
                    }
                }
                AppConstants.USER_TEACHER -> {
                    if (validateAssessmentType() && prefs.assessmentType == AppConstants.NIPUN_ABHYAS) {
                        validateFieldsAbhyas()
                    } else if (validateAssessmentType() && prefs.assessmentType == AppConstants.SUCHI_ABHYAS) {
                        redirectToCompetencySelectionScreen()
                    }
                }
            }
        }
        binding.includeToolbar.toolbar.setNavigationOnClickListener { finish() }

        binding.spBalvatika4to8.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (!selectedItem.equals(
                        CommonConstants.SELECT,
                        ignoreCase = false
                    )
                ) {
                    hashMap[selectedItem]?.let { classModel ->
                        selectedGrade = classModel.title
                        setSpinnerColor(binding.llBalvatika4to8, binding.spBalvatika4to8, true)
                    }
                } else {
                    selectedGrade = selectedItem
                    setSpinnerColor(binding.llBalvatika4to8, binding.spBalvatika4to8, false)
                }
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.spAssessmentType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (!selectedItem.equals(
                        CommonConstants.SELECT,
                        ignoreCase = false
                    )
                ) {
                    val itemValue = hashMap1[selectedItem] ?: return
                    selectedAssessmentType = itemValue
                    if (selectedAssessmentType.equals(AppConstants.SUCHI_ABHYAS, true)) {
                        setSpinnerSubject()
                        setVisibilityOfSpinners(
                            gradeSpin = true,
                            AssessmentTypeSpin = true,
                            subjectSpin = true
                        )
                        setVisibilityOfSpinnerTitle(
                            gradeSpin = true,
                            AssessmentTypeSpin = true,
                            subjectSpin = true
                        )
                    } else {
                        setVisibilityOfSpinners(
                            gradeSpin = true,
                            AssessmentTypeSpin = true,
                            subjectSpin = false
                        )
                        setVisibilityOfSpinnerTitle(
                            gradeSpin = true,
                            AssessmentTypeSpin = true,
                            subjectSpin = false
                        )
                    }
                    prefs.saveAssessmentType(itemValue)

                    setSpinnerColor(binding.rlAssessmentType, binding.spAssessmentType, true)
                } else {
                    selectedAssessmentType = selectedItem
                    setSpinnerColor(binding.rlAssessmentType, binding.spAssessmentType, false)
                }
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        binding.spSubject.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (!selectedItem.equals(
                        CommonConstants.SELECT,
                        ignoreCase = false
                    )
                ) {
                    hashMap2[selectedItem]?.let { classModel ->
                        selectedSubject = classModel.title
                        setSpinnerColor(binding.rlSubject, binding.spSubject, true)
                    }
                } else {
                    selectedSubject = selectedItem
                    setSpinnerColor(binding.rlSubject, binding.spSubject, false)
                }
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun setSpinnerColor(
        view: RelativeLayout,
        spinner: Spinner,
        spinnerSelected: Boolean
    ) {
        view.background = ActivityCompat.getDrawable(
            this@DetailsSelectionActivity,
            if (spinnerSelected) R.drawable.balbhatika_card_spinner_blue else R.drawable.balbhatika_card_spinner
        )
        val v = spinner.selectedView
        if (v is TextView) {
            v.setTextColor(
                if (spinnerSelected)
                    resources.getColor(R.color.white)
                else
                    resources.getColor(R.color.black)
            )
        }
    }

    private fun redirectToCompetencySelectionScreen() {
        if (validateGrade() && validateSubject()) {
            val redirectionIntent = Intent(this, CompetencySelectionActivity::class.java)
            setIntentValuesAndStartScreen(redirectionIntent)
        } else {
            ToastUtils.showShortToast(getString(R.string.warning_please_select_details_first))
        }
    }

    private fun validateFieldsAbhyas() {
        if (validateGrade()) {
            redirectToReadOnlyCompetency()
        } else {
            ToastUtils.showShortToast(getString(R.string.warning_please_select_details_first))
        }
    }

    private fun validateFieldsSpotAssessment() {
        if (validateGrade() && validateSubject()) {
            redirectToReadOnlyCompetency()
        } else {
            ToastUtils.showShortToast(getString(R.string.warning_please_select_details_first))
        }
    }

    private fun validateSubject(): Boolean {
        return (selectedSubject.isNotEmpty() && selectedSubject.equals(
            CommonConstants.SELECT, true
        ).not())
    }

    private fun validateGrade(): Boolean {
        return selectedGrade.isNotEmpty() && selectedGrade.equals(
            CommonConstants.SELECT, true
        ).not()
    }

    private fun validateAssessmentType(): Boolean {
        return selectedAssessmentType.isNotEmpty() && selectedAssessmentType.equals(
            CommonConstants.SELECT, true
        ).not()
    }

    private fun redirectToReadOnlyCompetency() {
        val redirectionIntent = Intent(this, ReadOnlyCompetencyActivity::class.java)
        setIntentValuesAndStartScreen(redirectionIntent)
    }

    private fun showSnackBar(message: String) {
        SnackbarUtils.showShortSnackbar(binding.scroll.rootView, message)
    }

    private fun setIntentValuesAndStartScreen(intent: Intent) {
        /*if (selectedStudentCount != null && selectedStudentCount != "0") {
            intent.putExtra(AppConstants.INTENT_SELECTED_STUDENTS, selectedStudentCount)
        }*/
        intent.putExtra(INTENT_SELECTED_GRADE, selectedGrade)
        intent.putExtra(INTENT_SELECTED_SUBJECT, selectedSubject)
        Timber.e("selected details : $selectedStudentCount")
        if (schoolsData != null) {
            intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setPostHogEventDetailsSelection() {
        val cDataList = ArrayList<Cdata>()
        if (selectedGrade != "") {
            cDataList.add(Cdata("selectedGrade", selectedGrade))
        }
        if (selectedSubject != "") {
            cDataList.add(Cdata("selectedSubject", selectedSubject))
        }
        val properties = PostHogManager.createProperties(
            DETAILS_SELECTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_DETAIL_SELECTION, cDataList),
            Edata(NL_DETAIL_SELECTION, TYPE_CLICK),
            Object.Builder().id(SELECT_DETAILS_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, EVENT_GRADE_SUBJECT_SELECTION, properties)
//        Log.e(POST_HOG_LOG_TAG, "details selection screen $properties $EVENT_GRADE_SUBJECT_SELECTION")
    }

    private fun initClassAdapter() {
        //get
        classAdapter = ClassAdapter(UtilityFunctions.getClassData(mConfigMapper), this)
        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0 || classAdapter!!.isLastRowAndShouldBeCentered(position)) {
                    2
                } else {
                    1
                }
            }
        }
        /* rvClasses = findViewById(R.id.rv_classes)
         rvClasses?.adapter = classAdapter
         classAdapter?.setItemSelectionListener(object : ItemSelectionListener<ClassModel?> {
             override fun onSelectionChange(pos: Int, item: ClassModel?) {
                 selectedGrade = item?.title ?: ""
                 selectedSubject = ""
                 initSubjectAdapter(item?.value ?: 0)
                 binding.rlBoth.isSelected = false
                 setBothButtonColor()
             }
         })*/
    }

    private fun initSubjectAdapter(clazz: Int?) {
        if (subAdapter == null) {
            subAdapter = SubjectAdapter(UtilityFunctions.getSubjectData(mConfigMapper, clazz), this)
            val linearLayoutManager = LinearLayoutManager(this)
            rvSubjects = findViewById(R.id.rv_subjects)
            rvSubjects!!.adapter = subAdapter
            rvSubjects!!.layoutManager = linearLayoutManager
            subAdapter!!.setItemSelectionListener(object : ItemSelectionListener<SubjectModel> {
                override fun onSelectionChange(pos: Int, item: SubjectModel) {
                    selectedSubject = item.title
                    binding.rlBoth.isSelected = false
                    setBothButtonColor()
                }
            })
        } else {
            subAdapter!!.setData(UtilityFunctions.getSubjectData(mConfigMapper, clazz))
        }
    }

    private fun setBothButtonColor() {
        if (binding.rlBoth.isSelected) {
            binding.tvBothTitle.setTextColor(
                ContextCompat.getColor(
                    this@DetailsSelectionActivity, R.color.white
                )
            )
            binding.ivBothIcon.setImageResource(R.drawable.ic_hindi)
            binding.ivBothIcon1.setImageResource(R.drawable.ic_math)
            setDrawableTint(binding.ivBothIcon, R.color.white)
            setDrawableTint(binding.ivBothIcon1, R.color.white)
        } else {
            binding.tvBothTitle.setTextColor(
                ContextCompat.getColor(
                    this@DetailsSelectionActivity, R.color.blue_2e3192
                )
            )
            binding.ivBothIcon.setImageResource(R.drawable.ic_hindi)
            binding.ivBothIcon1.setImageResource(R.drawable.ic_math)
            setDrawableTint(binding.ivBothIcon, R.color.blue_2e3192)
            setDrawableTint(binding.ivBothIcon1, R.color.blue_2e3192)
        }
    }

    private fun setDrawableTint(view: ImageView, color: Int) {
        DrawableCompat.setTint(
            DrawableCompat.wrap(view.drawable), ContextCompat.getColor(view.context, color)
        )
    }

    private fun launchGeofencingFlow() {
        GeofencingHelper.checkShowRequestPermissionRationale(
            activity = this,
            launchGeofence = {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            launchSettings = {
                GeofenceUI.allowLocationPermissionDialog(this, {
                    finish()
                }, {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        data = Uri.fromParts("package", packageName, null)
                    })
                })
            }
        )
    }

    private fun changeLocationSetting() {
        GeofencingHelper.changeLocationSettings(
            activity = this,
            success = {
                getCurrentLocation()
                setStartMatchingLocationDialog()
            },
            failure = { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        startIntentSenderForResult(
                            exception.resolution.intentSender,
                            REQUEST_CHECK_SETTINGS,
                            null,
                            0,
                            0,
                            0,
                            null
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        )
    }

    private fun checkGeofencingEnabled(): Boolean {
        if (prefs.selectedUser.equals(AppConstants.USER_PARENT, ignoreCase = true)) {
            return false
        }
        val enabled: Boolean = if (geofencingConfig?.enabled == true) {
            geofencingConfig?.actorsDisabled?.contains(prefs.mentorDetailsData.actorId)?.not()
                ?: false
        } else {
            false
        }
        if (enabled) {
            this.geofencingRadius = geofencingConfig?.geofencingInitials?.fencingRadius
        }
        Timber.d("enabled : $enabled")
        return enabled
    }

    private fun setStartMatchingLocationDialog() {
        startLocationMatchDialog = GeofenceUI.startLocationMatchDialog(this)
        startLocationMatchDialog?.setOnFinishListener {
            startLocationMatchDialog?.dismiss()
            finish()
        }
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        GeofencingHelper.getFusedLocationProviderClient(this)
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            GeofencingHelper.getLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Timber.d("location updates removed!")
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            Timber.d("onLocationAvailability: $p0")
        }

        override fun onLocationResult(locationResult: LocationResult) =
            GeofencingHelper.compareUserLocationWithGivenRange(
                userLatitude = locationResult.lastLocation.latitude,
                userLongitude = locationResult.lastLocation.longitude,
                schoolData = schoolsData!!,
                geofencingRadius = geofencingRadius,
                listener = object : GeofencingHelper.SetMatchLocationListener {
                    override fun onLocationRangeMatched(distance: Float) {
                        stopLocationUpdates()
                        hideLocationDialog()
                        performAction()
                    }

                    override fun onLocationOutOfRange(distance: Float) {
                        stopLocationUpdates()
                        hideLocationDialog()
                        showOutOfRangePrompt()
                    }

                    override fun onLocationLatLongNull() {
                        stopLocationUpdates()
                        hideLocationDialog()
                        ToastUtils.showShortToast("School lat long is null!")
                    }
                }
            )
    }

    private fun getCurrentLocation() {
        startLocationUpdates()
    }

    private fun hideLocationDialog() {
        if (startLocationMatchDialog?.isShowing == true)
            startLocationMatchDialog?.dismiss()
    }

    private fun showOutOfRangePrompt() {
        GeofenceUI.locationMatchFailureDialog(this, geofencingConfig?.dialogProps) {
            finish()
        }
    }

    private fun performAction() {
        GeofenceUI.locationMatchedDialog(this) {
            it.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                launchGeofencingFlow()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                GeofenceUI.enableLocationToStartFlow(this, {
                    finish()
                }, {
                    launchGeofencingFlow()
                })
            } else {
                //handle edge case if required
                ToastUtils.showShortToast(getString(R.string.enable_location))
            }
        }
    }
}

fun View.setVisible(isVisible: Boolean) {
    if (isVisible) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun TextView.setTextVisible(isVisible: Boolean) {
    if (isVisible) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}
