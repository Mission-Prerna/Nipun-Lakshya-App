package com.samagra.parent.repository

import com.data.db.models.MentorPerformanceInsightsItem
import com.data.network.Result
import com.samagra.commons.basemvvm.BaseRepository
import kotlinx.coroutines.flow.Flow

abstract class MentorPerformanceInsightsRepository : BaseRepository() {

    abstract suspend fun fetchMentorPerformanceInsights(): Result<MentorPerformanceInsightsItem>

    abstract fun getMentorPerformanceInsights(): Flow<MentorPerformanceInsightsItem?>

    abstract suspend fun insert(dummyStats: MentorPerformanceInsightsItem)

}