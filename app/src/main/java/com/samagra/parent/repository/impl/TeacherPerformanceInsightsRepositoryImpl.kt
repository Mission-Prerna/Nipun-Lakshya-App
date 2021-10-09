package com.samagra.parent.repository.impl

import com.data.db.dao.TeacherPerformanceInsightsDao
import com.data.db.models.TeacherPerformanceInsightsItem
import com.data.network.Result
import com.samagra.commons.AppPreferences
import com.samagra.commons.constants.Constants
import com.samagra.parent.network.TeacherInsightsService
import com.samagra.parent.repository.TeacherPerformanceInsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TeacherPerformanceInsightsRepositoryImpl @Inject constructor(
    private val service: TeacherInsightsService,
    private val teacherPerformanceInsightsDao: TeacherPerformanceInsightsDao
) : TeacherPerformanceInsightsRepository() {

    override suspend fun fetchTeacherPerformanceInsights(udise: String): Result<List<TeacherPerformanceInsightsItem>> {
        try {
            val response =
                service.getTeacherInsights(
                    udise,
                    Constants.BEARER_ + AppPreferences.getUserAuth(),
                    "hi"
                )
            if (response.isSuccessful) {
                val apiInsights = response.body()
                if (!apiInsights.isNullOrEmpty()) {
                    val dbInsights = getTeacherPerformanceInsights().first()
                    return if (apiHasLatestData(dbInsights, apiInsights)) {
                        teacherPerformanceInsightsDao.insert(apiInsights)
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
    private fun apiHasLatestData(dbInsights: List<TeacherPerformanceInsightsItem>, apiInsights: List<TeacherPerformanceInsightsItem>): Boolean {

        val maxDbUpdateAt = dbInsights.maxOfOrNull { it.updated_at } ?: Long.MIN_VALUE
        val hasLatestData = apiInsights.any { it.updated_at > maxDbUpdateAt }

        return hasLatestData
    }

    override fun getTeacherPerformanceInsights(): Flow<List<TeacherPerformanceInsightsItem>> {
        return teacherPerformanceInsightsDao.getTeacherPerformanceInsights()
    }

}