package com.assessment.schoolhistory

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessment.R
import com.assessment.databinding.ActivitySchoolHistoryBinding
import com.assessment.flow.workflowengine.AppConstants
import com.assessment.flow.workflowengine.UtilityFunctions
import com.assessment.studentselection.GradesStates
import com.samagra.commons.CommonUtilities
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SchoolHistoryActivity : BaseActivity<ActivitySchoolHistoryBinding, SchoolHistoryVM>() {

    private lateinit var schoolsData: SchoolsData
    private lateinit var historyAdapter: HistoryAdapter
    private var gradesList: List<Int> = mutableListOf()
    private val gradeViews: MutableList<TextView> = mutableListOf()
    private var selectedClassIndex = 0
    private var selectedTextView: TextView? = null

    override fun layoutRes(): Int {
        return R.layout.activity_school_history
    }

    override fun getBaseViewModel(): SchoolHistoryVM {
        val viewModel: SchoolHistoryVM by viewModels()
        return viewModel
    }

    override fun getBindingVariable(): Int {
        return 0
    }

    override fun onLoadData() {
        setupToolbar()
        getDataFromIntent()
        setClickListeners()
        setupRecyclerView()
        setObservers()
        viewModel.getGradesList()
    }

    private fun setObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.gradesListState.collect {
                when (it) {
                    is GradesStates.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvError.visibility = View.GONE
                    }

                    is GradesStates.Error -> {
                        hideProgressBar()
                        binding.progressBar.visibility = View.GONE
                        if (it.t.message.equals("yet to sync submission")) {
                            showToast("Cannot sync at the moment")
                        } else {
                            binding.tvError.visibility = View.VISIBLE
                        }
                    }

                    is GradesStates.Success -> {
                        hideProgressBar()
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.visibility = View.GONE
                        gradesList = it.gradesList
                        createAndDistributeTextViews(binding.clClassesButtons, gradesList)
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.schoolHistoryState.collect {
                when (it) {
                    is SchoolHistoryStates.Error -> {
                        hideProgressBar()
                        binding.progressBar.visibility = View.GONE
                        if (it.t.message.equals("yet to sync submission")) {
                            showToast("Cannot sync at the moment")
                        }
                    }
                    is SchoolHistoryStates.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvError.visibility = View.GONE
                    }
                    is SchoolHistoryStates.Success -> {
                        hideProgressBar()
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.visibility = View.GONE
                        historyAdapter.setItems(it.studentsAssessmentHistories)
                    }
                }
            }
        }
    }

    private fun createAndDistributeTextViews(
        constraintLayout: ConstraintLayout,
        gradesList: List<Int>
    ) {
        // Clear existing TextViews
        constraintLayout.removeAllViews()
        gradeViews.clear()
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        val chainIds = mutableListOf<Int>()
        val screenWidth = CommonUtilities.getScreenWidth(this)
        val availableWidth = screenWidth - (gradesList.size) * 20
        val itemWidth = availableWidth / (gradesList.size + 1)

        for (i in 1..gradesList.size) {
            //all distinct grades in student listing api - get from db
            setupView("कक्षा ${gradesList[i - 1]}", constraintSet, constraintLayout, chainIds, i, itemWidth)
        }
        setupView("सभी कक्षा", constraintSet, constraintLayout, chainIds, gradesList.size + 1, itemWidth)
        // Apply the constraints
        constraintSet.applyTo(constraintLayout)
        if (gradeViews.size > 0) {
            handleTextViewClick(gradeViews[selectedClassIndex])
        } else {
            selectedClassIndex = 0;
        }

    }

    private fun setupView(
        text: String,
        constraintSet: ConstraintSet,
        constraintLayout: ConstraintLayout,
        chainIds: MutableList<Int>,
        currentIndex: Int,
        dpWidth: Int
    ) {
        val classTextView = createTextView(text)
        classTextView.background.setTint(ContextCompat.getColor(this, R.color.white))
        classTextView.setTextColor(ContextCompat.getColor(this, R.color.blue_31328f))
        val dpHeight = 42
        // Convert dp to pixels
        val pixelsWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpWidth.toFloat(), resources.displayMetrics
        ).toInt()
        val pixelsHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpHeight.toFloat(), resources.displayMetrics
        ).toInt()

        // Set the constraints for the TextViews
        constraintSet.constrainWidth(classTextView.id, pixelsWidth)
        constraintSet.constrainHeight(classTextView.id, pixelsHeight)

        constraintLayout.addView(classTextView)

        // Add the TextView's ID to the chain
        chainIds.add(classTextView.id)
        gradeViews.add(classTextView)

        if (currentIndex > 1) {
            // Create a horizontal chain
            constraintSet.createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                chainIds.toIntArray(),
                null,
                ConstraintSet.CHAIN_SPREAD
            )
        }

        // If there's only one TextView, center it horizontally
        if (gradesList.size == 1) {
            constraintSet.centerHorizontally(chainIds[0], ConstraintSet.PARENT_ID)
        }

        classTextView.setOnClickListener {
            handleTextViewClick(classTextView)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        binding.toolbar.title.text = getString(R.string.school_assessment_history)
        binding.toolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(AppConstants.INTENT_SCHOOL_DATA)) {
            schoolsData =
                intent.getSerializableExtra(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
    }

    private fun setClickListeners() {

    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()

        binding.rvSchoolHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@SchoolHistoryActivity)
        }
    }

    private fun createTextView(text: String): TextView {
        val textView = TextView(this)
        textView.id = View.generateViewId()
        textView.text = text
        textView.gravity = Gravity.CENTER
        textView.background =
            ContextCompat.getDrawable(this, R.drawable.ic_rect_border_select_class)
        textView.setTextColor(ContextCompat.getColor(this, R.color.blue_2e3192))
        textView.textSize = 16F
        textView.setTypeface(null, Typeface.BOLD)
        return textView
    }

    private fun handleTextViewClick(textView: TextView) {

        // Reset last text view clicked state
        selectedTextView?.let {
            it.background.setTint(ContextCompat.getColor(this, R.color.white))
            it.setTextColor(ContextCompat.getColor(this, R.color.blue_31328f))
        }

        textView.background.setTint(ContextCompat.getColor(this, R.color.blue_31328f))
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Store the currently selected TextView
        selectedTextView = textView

        val classText = textView.text.toString()
        if (classText.contains("1")) {
            selectedClassIndex = 0;
            viewModel.getStudentsAssessmentHistory(
                schoolsData.udise!!,
                mutableListOf(1),
            )
        } else if (classText.contains("2")) {
            selectedClassIndex = 1;
            viewModel.getStudentsAssessmentHistory(
                schoolsData.udise!!,
                mutableListOf(2)
            )
        } else if (classText.contains("3")) {
            selectedClassIndex = 2;
            viewModel.getStudentsAssessmentHistory(
                schoolsData.udise!!,
                mutableListOf(3)
            )
        } else if (classText.contains("4")) {
            selectedClassIndex = 3;
            viewModel.getStudentsAssessmentHistory(
                schoolsData.udise!!,
                mutableListOf(4)
            )
        } else if (classText.contains("5")) {
            selectedClassIndex = 4;
            viewModel.getStudentsAssessmentHistory(
                schoolsData.udise!!,
                mutableListOf(5)
            )
        } else {
            selectedClassIndex = 0;
            viewModel.getStudentsAssessmentHistory(
                schoolsData.udise!!,
                mutableListOf(1,2,3)
            )
        }
        viewModel.sendTelemetry("nl-school-historyscreen-class-selected",selectedClassIndex + 1, schoolsData, this)
    }

}