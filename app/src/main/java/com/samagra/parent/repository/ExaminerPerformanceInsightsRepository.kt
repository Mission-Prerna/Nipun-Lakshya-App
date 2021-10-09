package com.samagra.parent.repository

import com.data.db.models.ExaminerPerformanceInsightsItem
import com.data.network.Result
import com.samagra.commons.basemvvm.BaseRepository
import com.data.db.models.TeacherPerformanceInsightsItem
import kotlinx.coroutines.flow.Flow


abstract class ExaminerPerformanceInsightsRepository : BaseRepository() {

    abstract suspend fun fetchExaminerPerformanceInsights(): Result<List<ExaminerPerformanceInsightsItem>>

    abstract fun getExaminerPerformanceInsights(): Flow<List<ExaminerPerformanceInsightsItem>>

}