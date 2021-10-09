package com.samagra.parent.ui.individualresultnl

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ListAdapter
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.constants.Constants
import com.samagra.commons.getPercentage
import com.samagra.commons.models.Results
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.*
import com.samagra.commons.utils.CommonConstants.ODK
import com.samagra.commons.utils.getNipunCriteria
import com.samagra.commons.utils.loadGif
import com.samagra.commons.utils.playMusic
import com.samagra.parent.AppConstants
import com.samagra.parent.BR
import com.samagra.parent.R
import com.samagra.parent.databinding.ActivityIndividualNlResultBinding
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData
import com.samagra.parent.ui.competencyselection.readonlycompetency.INDIVIDUAL_RESULT_REQUEST_CODE
import com.samagra.parent.ui.detailselection.setVisible
import com.samagra.parent.ui.finalresults.IndividualResultAdapter
import com.samagra.parent.ui.setTextOnUI
import org.odk.collect.android.utilities.SnackbarUtils
import org.odk.collect.android.utilities.UiUtils
import timber.log.Timber
import kotlin.math.roundToInt

@Suppress("UNUSED_PARAMETER")
class IndividualNlResultActivity :
    BaseActivity<ActivityIndividualNlResultBinding, ResultsVM>() {

    @LayoutRes
    override fun layoutRes() = R.layout.activity_individual_nl_result

    override fun getBaseViewModel() = viewModels

    override fun getBindingVariable() = BR.vm

    private lateinit var expandableResultToShowList: java.util.ArrayList<ExpandableResultsModel>
    private lateinit var prefs: CommonsPrefsHelperImpl
    private var schoolsData: SchoolsData? = null
    private lateinit var finalResultsList: java.util.ArrayList<StudentsAssessmentData>
    private lateinit var expandableListAdapter: CustomExpandableListAdapter
    private val viewModels: ResultsVM by viewModels()

    override fun onLoadData() {
        if (intent.hasExtra(AppConstants.INTENT_FINAL_RESULT_LIST).not()){
            SnackbarUtils.showShortSnackbar(binding.profileFooter, getString(R.string.error_generic_message))
            redirectToFinalResultScreen(true)
            return
        }
        getDataFromIntent()
        setObservers()
        initPrefs()
        parseResultsData()
        setupUi()
        setExpandableListView()
        setListeners()
    }

    /*
    * Getting list of bolo and odk results with multiple competencies,
    * */
    private fun parseResultsData() {
        var count = 0
        expandableResultToShowList = ArrayList()
        for (finalResultListIndex in finalResultsList.indices) {
            val resultsData = finalResultsList[finalResultListIndex]
            val studentResults = resultsData.studentResults
            val model = ExpandableResultsModel()
            model.competencyName = studentResults.competency
            if (resultsData.viewType == ODK) {
                resultsData.studentResults
                if (resultsData.studentResults.moduleResult.sessionCompleted) {
                    val nipunCriteria: Int = AppConstants.ODK_CRITERIA_KEY.getNipunCriteria(resultsData.studentResults.grade,resultsData.studentResults.subject )
                    val odkResultsData = resultsData.studentResults.odkResultsData
                    val percentage = getPercentage(
                        odkResultsData.totalMarks.toInt(),
                        odkResultsData.totalQuestions
                    )
                    model.isNipun = percentage >= nipunCriteria
                    model.studentResultList = studentResults.odkResultsData.results
                }
            } else {
                val nipunCriteria: Int = AppConstants.READ_ALONG_CRITERIA_KEY.getNipunCriteria(resultsData.studentResults.grade,resultsData.studentResults.subject )
                val boloResultsList: ArrayList<Results> = ArrayList()
                val achievement =
                    resultsData.studentResults.moduleResult.achievement
                val results = Results(
                    achievement.toString(),
                    getString(R.string.words_read_correct)
                )
                boloResultsList.add(results)
                model.isNipun = (achievement ?: 0) >= nipunCriteria
                model.studentResultList = boloResultsList
            }
            if (model.isNipun == true) {
                count++
            }
            expandableResultToShowList.add(model)
        }
        if (count == finalResultsList.size) {
            this.playMusic(R.raw.nipun_student_audio)
            this.loadGif(binding.ivBanner, R.drawable.ic_celebrating_bird, R.drawable.ic_celebrating_bird)
            //nipun case
            binding.tvSuccess.text =
                String.format(getString(R.string.Badhai_ho_nipun_student), finalResultsList[0].studentResults.grade.toString())
        } else {
            this.loadGif(binding.ivBanner, R.drawable.ic_flying_bird, R.drawable.ic_flying_bird)
            this.playMusic(R.raw.not_nipun_student_audio)
            //not nipun case
            if (prefs.assessmentType == AppConstants.NIPUN_ABHYAS
                || prefs.assessmentType == AppConstants.SUCHI_ABHYAS) {
                binding.tvSuccess.text =
                    getString(R.string.individual_student_result_fail_teacher)
            } else {
                binding.tvSuccess.text =
                    getString(R.string.individual_student_result_fail)
            }
        }
    }

    private fun initPrefs() {
        prefs = CommonsPrefsHelperImpl(this, "prefs")
    }

    private fun getDataFromIntent() {
        schoolsData = intent.getSerializableExtra(
            AppConstants.INTENT_SCHOOL_DATA
        ) as SchoolsData
        finalResultsList =
            intent.getSerializableExtra(AppConstants.INTENT_FINAL_RESULT_LIST) as ArrayList<StudentsAssessmentData>
    }

    /*
    * handle type : Nipun Lakshya, Nipun abhyas, Suchi abhyas
    *
    * */
    private fun setupUi() {
//        setSpannableTextWithImage()
        binding.tvExpandView.setVisible(false)
        setGrade()
        binding.mtlBtnNext.text = getString(R.string.finish_assessment)
        binding.tvRemarks.setVisible(false)
        binding.v.setVisible(false)
        binding.tvRemarks.text = getString(R.string.test_next_student)
        when (prefs.selectedUser) {
            AppConstants.USER_TEACHER -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_ABHYAS ->{
                        setHeaderUi()
                        setButton()
                    }
                }
            }
            AppConstants.USER_PARENT -> {
                binding.schoolInfo.root.visibility = View.GONE
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_LAKSHYA ->{
                        binding.schoolInfo.root.visibility = View.GONE
                        binding.mtlBtn1.visibility = View.GONE
                    }
                    AppConstants.NIPUN_ABHYAS ->{
                        setButton()
                    }
                }
            }
            AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                when (prefs.assessmentType) {
                    AppConstants.NIPUN_LAKSHYA ->{
                        setHeaderUi()
                        setButton()
                    }
                    AppConstants.NIPUN_ABHYAS ->{
                        setHeaderUi()
                        setButton()
                    }
                }
            }
            // No handling for examiner, We don't show individual results to examiner.
        }
    }

    private fun setGrade() {
        binding.incGradeNo.tvText.text =
            String.format(
                "%s %s",
                getString(R.string.nipun_lakshya_grade_),
                if (finalResultsList.isNotEmpty()) finalResultsList[0].studentResults.grade else 0
            )
        binding.incGradeNo.tvText.background =
            AppCompatResources.getDrawable(this, R.drawable.bg_rounded_blue_card)
    }

    private fun setSpannableTextWithImage() {
        val span: Spannable = SpannableString("दक्षताओं का विवरण देखने के लिए     दबाये")
        val myImage: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_arrow_drop_down)
        val background = ShapeDrawable()
        background.paint.color = ContextCompat.getColor(this,R.color.blue_2e3192)
        val layerDrawable = LayerDrawable(arrayOf(background, myImage))
        layerDrawable.setBounds(0, 0, 64, 64)
        val image = ImageSpan(layerDrawable, ImageSpan.ALIGN_BASELINE)
        span.setSpan(image, 32, 33, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        binding.tvExpandView.text = span
    }

    private fun setButton() {
        binding.mtlBtn1.visibility = View.VISIBLE
        binding.mtlBtnNext.visibility = View.VISIBLE
    }

    private fun setHeaderUi() {
        with(binding.schoolInfo) {
            root.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            udise.visibility = View.VISIBLE
            tvTime.visibility = View.GONE
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
                }
                else -> {
                    name.text =
                        setHeaderUiText(
                            R.string.school_name_top_banner,
                            schoolsData?.schoolName ?: ""
                        )
                    udise.text =
                        setHeaderUiText(R.string.udise_top_banner, schoolsData?.udise.toString())
                }
            }
        }
    }

    private fun setListeners() {
        binding.mtlBtnNext.setOnClickListener {
            // assessment end show final results
            redirectToFinalResultScreen(true)
        }

        binding.mtlBtn1.setOnClickListener {
            //assessment of next student
            redirectToFinalResultScreen(false)
            LogEventsHelper.addEventOnNextStudentSelection(
                prefs.assessmentType,
                this,
                INDIVIDUAL_NL_RESULT_SCREEN
            )
        }
    }

    private fun redirectToFinalResultScreen(showResults: Boolean) {
        val intentWithData = Intent()
        intentWithData.putExtra(AppConstants.SHOW_FINAL_RESULTS, showResults)
        setResult(INDIVIDUAL_RESULT_REQUEST_CODE, intentWithData)
        finish()
    }

    private fun setHeaderUiText(resValue: Int, text: String): String {
        return String.format(getString(resValue), text)
    }

    private fun setObservers() {
        with(viewModels) {
        }
    }

    override fun onBackPressed() {
        redirectToFinalResultScreen(true)
        super.onBackPressed()
    }

    private fun setExpandableListView() {
        binding.rvFinalResult.layoutManager = LinearLayoutManager(this)
        binding.rvFinalResult.adapter = IndividualResultAdapter(expandableResultToShowList)
        /*expandableListAdapter =
            CustomExpandableListAdapter(
                this,
                expandableResultToShowList
            )
        binding.elvResults.setAdapter(expandableListAdapter)
        getExpandableListViewSize(binding.elvResults);
        binding.elvResults.setOnGroupExpandListener { groupPosition ->
        }
        binding.elvResults.setOnGroupCollapseListener { groupPosition ->
        }
        binding.elvResults.setOnGroupCollapseListener { groupPosition ->
        }
        binding.elvResults.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            false
        }*/
    }

    private fun getExpandableListViewSize(myListView: ExpandableListView) {
        val myListAdapter: ListAdapter = myListView.adapter
                ?: //do nothing return null
                return
        //set listAdapter in loop for getting final size
        var totalHeight = 0
        for (size in 0 until myListAdapter.count) {
            val listItem: View = myListAdapter.getView(size, null, myListView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        //setting listview item in adapter
        val params: ViewGroup.LayoutParams = myListView.layoutParams
        params.height = (totalHeight +
                (UiUtils.convertDpToPixel((18 * myListAdapter.count).toFloat(), this))).roundToInt()
        myListView.layoutParams = params
        // print height of adapter on log
        Timber.i(totalHeight.toString())
    }
}
