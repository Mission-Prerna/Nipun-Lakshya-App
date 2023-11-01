package com.data.repository.impl

import com.data.db.dao.CycleDetailsDao
import com.data.db.models.entity.CycleDetails
import com.data.repository.CycleDetailsRepository
import javax.inject.Inject

class CycleDetailsRepositoryImpl @Inject constructor(
    private val cycleDetailsDao: CycleDetailsDao
) : CycleDetailsRepository {

    override fun getExaminerCycleDetails() = cycleDetailsDao.getCycleDetails()

    override suspend fun insertExaminerCycleDetails(cycleDetails: CycleDetails) = cycleDetailsDao.insert(cycleDetails)
    override suspend fun getCurrentCycleDetails(): CycleDetails? = cycleDetailsDao.getCurrentCycleDetails()
    override suspend fun getCurrentCycleId() = cycleDetailsDao.getCurrentCycleId()


}