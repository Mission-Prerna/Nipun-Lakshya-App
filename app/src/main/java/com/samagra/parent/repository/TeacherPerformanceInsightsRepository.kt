package com.samagra.parent.repository

import com.data.network.Result
import com.samagra.commons.basemvvm.BaseRepository
import com.data.db.models.TeacherPerformanceInsightsItem
import kotlinx.coroutines.flow.Flow


abstract class TeacherPerformanceInsightsRepository : BaseRepository() {

    abstract suspend fun fetchTeacherPerformanceInsights(udise: String): Result<List<TeacherPerformanceInsightsItem>>

    abstract fun getTeacherPerformanceInsights(): Flow<List<TeacherPerformanceInsightsItem>>

}