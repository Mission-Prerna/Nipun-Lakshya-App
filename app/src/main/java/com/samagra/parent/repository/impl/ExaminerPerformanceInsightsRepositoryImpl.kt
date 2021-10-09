package com.samagra.parent.repository.impl

import com.data.db.dao.ExaminerPerformanceInsightsDao
import com.data.db.dao.TeacherPerformanceInsightsDao
import com.data.db.models.ExaminerPerformanceInsightsItem
import com.data.network.Result
import com.data.repository.CycleDetailsRepository
import com.samagra.commons.AppPreferences
import com.samagra.commons.constants.Constants
import com.samagra.parent.network.ExaminerInsightsService
import com.samagra.parent.network.TeacherInsightsService
import com.samagra.parent.repository.ExaminerPerformanceInsightsRepository
import com.samagra.parent.repository.TeacherPerformanceInsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExaminerPerformanceInsightsRepositoryImpl @Inject constructor(
    private val service: ExaminerInsightsService,
    private val examinerPerformanceInsightsDao: ExaminerPerformanceInsightsDao,
    private val cycleDetailsRepo: CycleDetailsRepository
) : ExaminerPerformanceInsightsRepository() {

    override suspend fun fetchExaminerPerformanceInsights(): Result<List<ExaminerPerformanceInsightsItem>> {
        try {
            val cycleId = cycleDetailsRepo.getCurrentCycleId() ?: 4
            val response =
                service.getExaminerInsights(
                    cycleId = cycleId,
                    token = Constants.BEARER_ + AppPreferences.getUserAuth(),
                    lang = "hi"
                )
            if (response.isSuccessful) {
                val apiInsights = response.body()
                if (!apiInsights.isNullOrEmpty()) {
                    val dbInsights = getExaminerPerformanceInsights().first()
                    return if (apiHasLatestData(dbInsights, apiInsights)) {
                        examinerPerformanceInsightsDao.insert(apiInsights)
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
        dbInsights: List<ExaminerPerformanceInsightsItem>,
        apiInsights: List<ExaminerPerformanceInsightsItem>
    ): Boolean {

        val maxDbUpdateAt = dbInsights.maxOfOrNull { it.updated_at } ?: Long.MIN_VALUE
        val hasLatestData = apiInsights.any { it.updated_at > maxDbUpdateAt }

        return hasLatestData
    }

    override fun getExaminerPerformanceInsights(): Flow<List<ExaminerPerformanceInsightsItem>> {
        return examinerPerformanceInsightsDao.getExaminerPerformanceInsights()
    }

}