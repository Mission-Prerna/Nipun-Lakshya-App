package com.data.repository

import com.data.db.models.AppAction

abstract class AppActionsRepository {

    abstract suspend fun insertAction(appActions: List<AppAction>)

    abstract fun getAllActions(): List<AppAction>

    abstract suspend fun markActionCompleted(actionId: Long)

    abstract fun getLatestTime(): Long?
}