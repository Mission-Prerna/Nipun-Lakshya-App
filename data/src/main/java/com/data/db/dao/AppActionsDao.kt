package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.data.db.models.AppAction

@Dao
interface AppActionsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(appActions: List<AppAction>?)

    @Query("SELECT * FROM app_actions")
    fun getAllActions(): List<AppAction>

    @Query("UPDATE app_actions SET is_completed = 1 WHERE id = :actionId")
    suspend fun markActionCompleted(actionId: Long)

    @Query("SELECT MAX(requested_at) FROM app_actions")
    fun getLatestTime(): Long?
}