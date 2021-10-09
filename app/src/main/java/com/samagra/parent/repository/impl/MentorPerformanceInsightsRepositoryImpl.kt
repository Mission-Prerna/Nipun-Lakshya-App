package com.samagra.parent.repository.impl

import com.data.db.dao.MentorPerformanceInsightsDao
import com.data.db.models.MentorInsight
import com.data.db.models.MentorPerformanceInsightsItem
import com.data.network.Result
import com.samagra.commons.AppPreferences
import com.samagra.commons.constants.Constants
import com.samagra.parent.network.MentorInsightsService
import com.samagra.parent.repository.MentorPerformanceInsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class MentorPerformanceInsightsRepositoryImpl @Inject constructor(
    private val service: MentorInsightsService,
    private val mentorPerformanceInsightsDao: MentorPerformanceInsightsDao
) : MentorPerformanceInsightsRepository() {

    override suspend fun fetchMentorPerformanceInsights(): Result<MentorPerformanceInsightsItem> {
        try {
            val response =
                service.getMentorInsights(
                    token = Constants.BEARER_ + AppPreferences.getUserAuth(),
                    lang = "hi"
                )
            if (response.isSuccessful) {
                val apiInsights = response.body()
                if (apiInsights!=null) {
                    val dbInsights = getMentorPerformanceInsights().first()
                    return if (apiHasLatestData(dbInsights, apiInsights)) {
                        mentorPerformanceInsightsDao.insert(apiInsights)
                        Result.Success(apiInsights)
                    } else {
                        Result.Success(dbInsights)
                    }
                }
            }
            return Result.Error(Exception(response.errorBody()?.string() ?: response.message()))
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    private fun apiHasLatestData(
        dbInsights: MentorPerformanceInsightsItem?,
        apiInsights: MentorPerformanceInsightsItem?
    ): Boolean {

        return if (dbInsights==null) {
            true
        } else {
            (apiInsights?.updated_at ?: Long.MIN_VALUE) > dbInsights.updated_at
        }
    }

    override fun getMentorPerformanceInsights(): Flow<MentorPerformanceInsightsItem> {
        return mentorPerformanceInsightsDao.getMentorPerformanceInsights()
    }

    override suspend fun insert(dummyStats: MentorPerformanceInsightsItem) {
        mentorPerformanceInsightsDao.insert(dummyStats)
    }

}