package com.samagra.workflowengine.odk

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants
import com.samagra.commons.getPercentage
import com.samagra.commons.models.OdkResultData
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.LogEventsHelper
import com.samagra.commons.posthog.ODK_INDIVIDUAL_RESULT_SCREEN
import com.samagra.commons.travel.BroadcastAction
import com.samagra.commons.travel.BroadcastActionSingleton
import com.samagra.commons.travel.BroadcastEvents
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.getNipunCriteria
import com.samagra.commons.utils.loadGif
import com.samagra.commons.utils.playMusic
import com.samagra.parent.AppConstants
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.ViewModelProviderFactory
import com.samagra.parent.databinding.FragmentOdkResultBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.competencyselection.CompetencySelectionRepository
import com.samagra.parent.ui.competencyselection.CompetencySelectionVM
import com.samagra.parent.ui.competencyselection.SuchiAbhyasIndividualResultActivity
import com.samagra.parent.ui.setTextOnUI
import com.samagra.parent.ui.withArgs
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult
import com.samagra.workflowengine.workflow.model.stateresult.ModuleResult
import timber.log.Timber

class ODKResultFragment : Fragment() {

    private var subject: String = ""
    private var grade: Int? = 0
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var totalTime: Long = 0
    private var boloResult: AssessmentStateResult? = null
    private var competencyName: String = ""
    private var odkResultsData: OdkResultData? = null
    private var studentCount: Int? = 0
    private var schoolsData: SchoolsData? = SchoolsData()
    private lateinit var binding: FragmentOdkResultBinding
    private var odkStartTime: Long = 0
    private var odkEndTime: Long = 0
    private lateinit var viewModel: CompetencySelectionVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_odk_result, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = CommonsPrefsHelperImpl(activity as Context, "prefs")
        initViewModel()
        getDataFromArgs()
        setupUI()
        setListeners()
        setResultAdapter()
        }

    private fun initViewModel() {
        val repository = CompetencySelectionRepository()
        val dataSyncRepo = DataSyncRepository()
        val viewModelProviderFactory =
            activity?.application?.let {
                ViewModelProviderFactory(
                    it,
                    repository,
                    dataSyncRepo
                )
            }
        viewModel = ViewModelProvider(
            activity!!,
            viewModelProviderFactory!!
        )[CompetencySelectionVM::class.java]
    }

    private fun getDataFromArgs() {
        if (arguments?.containsKey(AppConstants.INTENT_SCHOOL_DATA) == true) {
            schoolsData = arguments?.getSerializable(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }

        odkResultsData =
            arguments?.getSerializable(AppConstants.INTENT_ODK_RESULT) as OdkResultData?

        if (arguments?.containsKey(AppConstants.INTENT_BOLO_RESULT) == true
            && arguments?.getSerializable(AppConstants.INTENT_BOLO_RESULT) != null
        ) {
            boloResult =
                arguments?.getSerializable(AppConstants.INTENT_BOLO_RESULT) as AssessmentStateResult
        }
        studentCount = arguments?.getInt(AppConstants.INTENT_STUDENT_COUNT, 0)
        competencyName = arguments?.getString(AppConstants.INTENT_COMPETENCY_NAME) as String
        grade = arguments?.getInt(AppConstants.INTENT_SELECTED_GRADE, 0)
        subject = arguments?.getString(AppConstants.INTENT_SELECTED_SUBJECT) as String
        odkStartTime = arguments?.getLong(AppConstants.ODK_START_TIME, 0) as Long
    }

    private fun setupUI() {
        if (boloResult != null) {
            binding.boloGroup.visibility = View.VISIBLE
            binding.scoreView.setResultData(
                boloResult!!.moduleResult.achievement, boloResult!!.moduleResult.successCriteria
            )
            binding.incSelectedCompetency.tvText.text = boloResult!!.competency

            val timeTakenBolo = UtilityFunctions.getTimeDifferenceMilis(
                boloResult!!.moduleResult.startTime,
                boloResult!!.moduleResult.endTime
            )
            totalTime += timeTakenBolo
            Timber.d(
                " total time taken before bolo: $totalTime, bolo total time $timeTakenBolo, total time taken : $totalTime"
            )
        } else {
            binding.boloGroup.visibility = View.GONE
        }
        binding.incSelectedCompetency1.tvText.text = competencyName
        setupSchoolsInfoAndRemarks()
        if (prefs.selectedUser.equals(
                AppConstants.USER_TEACHER, true)
        ) {
            binding.mtlBtn1.text = getString(R.string.start_suchi_abhyas_for_next_student)
            binding.ivBanner.visibility = View.VISIBLE
            setTextNipun()
        } else {
            binding.tvSuccess.visibility = View.INVISIBLE
            binding.ivBanner.visibility = View.INVISIBLE
        }
    }

    private fun setGif(drawableResource: Int) {
        context?.loadGif(binding.ivBanner, drawableResource, drawableResource)
    }

    private fun setTextNipun() {
        val nipunCriteria = AppConstants.ODK_CRITERIA_KEY.getNipunCriteria(grade ?: 0, subject)
        var percentage = 0
        odkResultsData?.let {
            percentage = getPercentage(it.totalMarks.toInt(), it.totalQuestions)
        }
        binding.tvSuccess.visibility = View.VISIBLE
        if (percentage >= nipunCriteria) {
            binding.tvSuccess.text =
                "बधाई हो, आपके बालक/बालिका ने कक्षा $grade गणित/भाषा के निपुण लक्ष्य को हासिल कर लिया है।"
            setGif(R.drawable.ic_celebrating_bird)
            context?.playMusic(R.raw.nipun_student_audio)
        } else {
            setGif(R.drawable.ic_flying_bird)
            context?.playMusic(R.raw.not_nipun_student_audio)
            when (prefs.selectedUser) {
                AppConstants.USER_TEACHER -> {
                    when (prefs.assessmentType) {
                        AppConstants.SUCHI_ABHYAS -> {
                            binding.tvSuccess.text =
                                getString(R.string.individual_student_result_fail_teacher)
                        }
                    }
                }
                else -> {
                    binding.tvSuccess.text =
                        getString(R.string.individual_student_result_fail)
                }
            }
        }
    }

    private fun setupSchoolsInfoAndRemarks() {
        when (prefs.selectedUser) {
            AppConstants.USER_PARENT -> {
                binding.schoolInfo.root.visibility = View.GONE
                binding.tvRemarks.visibility = View.INVISIBLE
                binding.v.visibility = View.INVISIBLE
            }
            AppConstants.USER_TEACHER -> {
                setHeaderUi()
                binding.tvRemarks.visibility = View.INVISIBLE
                binding.v.visibility = View.INVISIBLE
            }
            AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                setHeaderUi()
                binding.tvRemarks.text = getString(R.string.test_next_student)
                binding.tvRemarks.visibility = View.VISIBLE
                binding.v.visibility = View.VISIBLE
            }
        }
    }

    private fun setHeaderUi() {
        with(binding.schoolInfo) {
            root.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            udise.visibility = View.VISIBLE
            tvTime.visibility = View.VISIBLE
            address.visibility = View.GONE
            when (prefs.selectedUser) {
                AppConstants.USER_TEACHER -> {
                    name.setTextOnUI(
                        setHeaderUiText(
                            R.string.school_name_top_banner,
                            prefs.mentorDetailsData?.schoolName ?: ""
                        )
                    )
                    udise.setTextOnUI(
                        setHeaderUiText(
                            R.string.udise_top_banner,
                            prefs.mentorDetailsData?.udise.toString()
                        )
                    )
                    tvTime.setTextOnUI(
                        setHeaderUiText(
                            R.string.total_time_taken,
                            calculateTotalTimeTaken() ?: ""
                        )
                    )
                }
                else -> {
                    name.setTextOnUI(setHeaderUiText(
                        R.string.school_name_top_banner,
                        schoolsData?.schoolName ?: ""
                    ))
                    udise.setTextOnUI(setHeaderUiText(
                        R.string.school_name_top_banner,
                        schoolsData?.udise.toString()
                    ))
                    tvTime.setTextOnUI(
                        setHeaderUiText(
                            R.string.total_time_taken,
                            calculateTotalTimeTaken() ?: ""
                        )
                    )
                }
            }
        }
    }

    private fun setHeaderUiText(resValue: Int, text: String): String {
        return String.format(getString(resValue), text)
    }

    private fun calculateTotalTimeTaken(): String? {
        odkEndTime = UtilityFunctions.getTimeMilis()
        val odkTimeTaken = UtilityFunctions.getTimeDifferenceMilis(odkStartTime, odkEndTime)
        totalTime += odkTimeTaken
        return UtilityFunctions.getTimeString(totalTime)
    }

    private fun setListeners() {
        binding.mtlBtnNext.setOnClickListener {
            (context as SuchiAbhyasIndividualResultActivity).showSubmitResults()
        }

        binding.mtlBtn1.setOnClickListener {
            activity?.let { activity ->
                LogEventsHelper.addEventOnNextStudentSelection(
                    prefs.assessmentType,
                    activity,
                    ODK_INDIVIDUAL_RESULT_SCREEN
                )
            }
            (context as SuchiAbhyasIndividualResultActivity).proceedWithNextStudent()
        }
    }

    private fun setResultAdapter() {
        odkResultsData?.results?.let {
            val adapter = ODKResultsAdapter(it)
            val layoutManager = LinearLayoutManager(context)
            binding.rvResult.layoutManager = layoutManager
            binding.rvResult.adapter = adapter
        }
    }

    private fun processResult() {
        val assessmentResult = AssessmentStateResult()
        val nipunCriteria =
            AppConstants.ODK_CRITERIA_KEY.getNipunCriteria(grade?:0, subject)
        val module = ModuleResult(ODK, nipunCriteria)
        module.totalQuestions = odkResultsData?.totalQuestions ?: 0
        module.achievement = odkResultsData?.totalMarks?.toInt()
        val percentage = getPercentage(
            odkResultsData?.totalMarks?.toInt() ?: 0,
            odkResultsData?.totalQuestions ?: 0
        )
        module.isPassed = percentage >= nipunCriteria
        module.isNetworkActive = NetworkStateManager.instance?.networkConnectivityStatus ?: false
        module.sessionCompleted = true
        module.appVersionCode = UtilityFunctions.getVersionCode()
        module.startTime = odkStartTime
        module.endTime = odkEndTime
        module.statement = "Test ODK"
        assessmentResult.moduleResult = module
        BroadcastActionSingleton.getInstance().liveAppAction.value = BroadcastAction(
            assessmentResult,
            BroadcastEvents.ODK_SUCCESS
        )
    }

    fun onBackPressed() {
        processResult()
        activity?.finish()
    }

    companion object {
        fun newInstance(
            schoolData: SchoolsData,
            studentCount: Int,
            odkResult: OdkResultData?,
            competencyName: String,
            boloResult: AssessmentStateResult?,
            startTime: Long,
            grade: Int,
            subject: String,
        ): ODKResultFragment =
            ODKResultFragment().withArgs {
                putSerializable(AppConstants.INTENT_SCHOOL_DATA, schoolData)
                putSerializable(AppConstants.INTENT_BOLO_RESULT, boloResult)
                putSerializable(AppConstants.INTENT_ODK_RESULT, odkResult)
                putString(AppConstants.INTENT_COMPETENCY_NAME, competencyName)
                putInt(AppConstants.INTENT_STUDENT_COUNT, studentCount)
                putString(AppConstants.INTENT_SELECTED_SUBJECT, subject)
                putInt(AppConstants.INTENT_SELECTED_GRADE, grade)
                putLong(AppConstants.ODK_START_TIME, startTime)
            }
    }
}