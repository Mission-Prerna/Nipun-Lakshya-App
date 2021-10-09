package com.data.repository

import com.data.db.models.entity.AssessmentSchoolHistory
import com.data.db.models.entity.AssessmentState
import com.data.db.models.entity.AssessmentSubmission
import com.data.db.models.helper.AssessmentStateDetails
import com.data.models.ui.ScorecardData
import com.data.network.Result
import com.samagra.commons.basemvvm.BaseRepository
import kotlinx.coroutines.flow.Flow

abstract class AssessmentsRepository : BaseRepository() {

    abstract fun getStates(): MutableList<AssessmentState>

    abstract fun observerStates(): Flow<List<AssessmentState>>

    abstract fun observerIncompleteStates(): Flow<List<AssessmentStateDetails>>

    abstract fun createStates(grade: Int): Result<Unit>

    abstract suspend fun clearStatesAsync()

    abstract fun clearStates()

    abstract fun insertAssessmentStates(state: MutableList<AssessmentState>): List<Long>

    abstract suspend fun updateState(state: AssessmentState)

    abstract suspend fun abandonFlow(state: AssessmentStateDetails)

    abstract suspend fun getResultsForScoreCard(): List<ScorecardData>

    abstract suspend fun convertStatesToSubmissions(udise : Long, cycleId : Int?, updateSchoolHistory : Boolean): Result<Unit>

    abstract fun insertAssessmentSubmission(submissions: MutableList<AssessmentSubmission>): List<Long>

    abstract suspend fun getSchoolAssessmentHistory(grade: List<Int>): Flow<MutableList<AssessmentSchoolHistory>>

    abstract fun fetchSchoolAssessmentHistory(udise: Long, grades: List<Int>): Result<Unit>

}
