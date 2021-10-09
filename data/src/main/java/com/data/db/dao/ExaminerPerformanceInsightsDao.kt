package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.ExaminerPerformanceInsightsItem
import com.data.db.models.TeacherPerformanceInsightsItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ExaminerPerformanceInsightsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insightsListEntity: List<ExaminerPerformanceInsightsItem>)

    @Query("SELECT * FROM examiner_performance_insights_list")
    fun getExaminerPerformanceInsights(): Flow<List<ExaminerPerformanceInsightsItem>>
}

