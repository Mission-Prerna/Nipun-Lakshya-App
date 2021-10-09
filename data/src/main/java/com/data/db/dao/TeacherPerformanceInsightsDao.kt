package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.TeacherPerformanceInsightsItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherPerformanceInsightsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insightsListEntity: List<TeacherPerformanceInsightsItem>)

    @Query("SELECT * FROM teacher_performance_insights_list ORDER BY month desc")
    fun getTeacherPerformanceInsights(): Flow<List<TeacherPerformanceInsightsItem>>
}

