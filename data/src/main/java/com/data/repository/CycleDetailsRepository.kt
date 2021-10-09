package com.data.repository

import com.data.db.models.entity.CycleDetails
import kotlinx.coroutines.flow.Flow

interface CycleDetailsRepository {

    fun getExaminerCycleDetails(): Flow<CycleDetails>

    suspend fun insertExaminerCycleDetails(cycleDetails: CycleDetails)

    suspend fun getCurrentCycleDetails() : CycleDetails?

    suspend fun getCurrentCycleId() : Int

}