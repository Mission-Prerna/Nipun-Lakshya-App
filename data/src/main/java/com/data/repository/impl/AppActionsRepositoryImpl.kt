package com.data.repository.impl

import com.data.db.dao.AppActionsDao
import com.data.db.models.AppAction
import com.data.repository.AppActionsRepository
import javax.inject.Inject

class AppActionsRepositoryImpl @Inject constructor(
    private val appActionsDao: AppActionsDao
): AppActionsRepository() {

    override suspend fun insertAction(appActions: List<AppAction>?) {
        appActionsDao.insert(appActions)
    }

    override fun getAllActions(): List<AppAction> {
        return appActionsDao.getAllActions()
    }

    override suspend fun markActionCompleted(actionId: Long) {
        appActionsDao.markActionCompleted(actionId)
    }

    override fun getLatestTime(): Long? {
        return appActionsDao.getLatestTime()
    }

}