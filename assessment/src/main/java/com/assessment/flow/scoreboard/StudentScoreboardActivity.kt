package com.assessment.flow.scoreboard

import android.annotation.SuppressLint
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessment.R
import com.assessment.common.AssessmentHeaderModel
import com.assessment.databinding.StudentScoreboardBinding
import com.assessment.flow.AssessmentConstants
import com.data.db.models.entity.School
import com.data.models.ui.ScorecardData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.commons.AppPreferences
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.utils.loadGif
import com.samagra.commons.utils.playMusic
import java.text.SimpleDateFormat
import java.util.Date

private lateinit var scoreboardAdapter: ScoreboardAdapter
private const val STUDENT = 0
class StudentScoreboardActivity : BaseActivity<StudentScoreboardBinding, ScoreboardVM>() {

    private var studentName = ""
    private var grade = -1

    companion object {
        const val SCORECARD_LIST = "scorecardList"
    }

    override fun layoutRes() = R.layout.student_scoreboard

    override fun onLoadData() {
        setupViews()
        getExtras()
        showHeaderView()
    }

    override fun getBaseViewModel(): ScoreboardVM {
        val vm: ScoreboardVM by viewModels()
        return vm
    }

    override fun getBindingVariable() = 0

    private fun getExtras() {
        val json = intent.getStringExtra(SCORECARD_LIST)
        val listType = object : TypeToken<MutableList<ScorecardData>>() {}.type

        val receivedScorecardDataList: MutableList<ScorecardData> = Gson().fromJson(json, listType)
        showStudentScorecard(receivedScorecardDataList)
        showRelevantNipunStatusIcon(receivedScorecardDataList)

        grade = intent.getIntExtra(AssessmentConstants.KEY_GRADE, -1)
        studentName = intent.getStringExtra(AssessmentConstants.KEY_STUDENT_NAME) ?: ""
        val studentId = intent.getStringExtra(AssessmentConstants.KEY_STUDENT_ID) ?: ""
        val schoolData = if (intent.hasExtra(AssessmentConstants.KEY_SCHOOL_DATA)) {
            intent.getSerializableExtra(AssessmentConstants.KEY_SCHOOL_DATA) as School
        } else {
            null
        }
        viewModel.setScorecardLoadedEvent(ctx = this, schoolData = schoolData, studentId = studentId, grade =  grade.toString())
    }

    @SuppressLint("SimpleDateFormat")
    private fun showHeaderView(){
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val dateToShow = dateFormat.format(currentDate)

        val assessorName = AppPreferences.getUser()?.officer_name ?: ""
        val assessorType = AppPreferences.getSelectedUserType() ?: ""
        binding.header.apply {
            val header = AssessmentHeaderModel(
                studentName,
                grade.toLong(),
                dateToShow,
                assessorType,
                assessorName
            )
            bind(header, STUDENT)
        }
    }

    private fun showRelevantNipunStatusIcon(list: MutableList<ScorecardData>) {
        var isNipun = true
        for (scorecard in list) {
            if (!scorecard.isPassed) {
                isNipun = false
            }
        }
        if (isNipun) {
            binding.ivNipunBird.setImageResource(R.drawable.nipun_banner)
            loadGif(binding.gifIv, R.drawable.ic_celebrating_bird, R.drawable.ic_celebrating_bird)
            playMusic(R.raw.nipun_student_audio)
        } else {
            binding.ivNipunBird.setImageResource(R.drawable.not_nipun_banner)
            loadGif(binding.gifIv, R.drawable.ic_flying_bird, R.drawable.ic_flying_bird)
            playMusic(R.raw.not_nipun_student_audio)
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun setupViews() {
        scoreboardAdapter = ScoreboardAdapter()
        binding.rvFinalResult.apply {
            adapter = scoreboardAdapter
            layoutManager = LinearLayoutManager(this@StudentScoreboardActivity)
        }

        binding.cta.setOnClickListener {
            finish()
        }
    }

    private fun showStudentScorecard(list: MutableList<ScorecardData>) {
        scoreboardAdapter.differ.submitList(list)
    }

}