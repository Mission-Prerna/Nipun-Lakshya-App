package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.data.db.Convertors
import com.data.db.models.entity.AssessmentSchoolHistory
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(Convertors::class)
interface AssessmentSchoolHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(histories: List<AssessmentSchoolHistory>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: AssessmentSchoolHistory)

    @Query("SELECT * FROM assessment_school_histories WHERE grade IN (:grades)")
    fun getHistoriesAsync(grades : List<Int>) : Flow<MutableList<AssessmentSchoolHistory>>

    @Query("SELECT * FROM assessment_school_histories WHERE grade IN (:grades)")
    fun getHistories(grades : List<Int>) : List<AssessmentSchoolHistory>

    @Query("SELECT * FROM assessment_school_histories WHERE grade = :grade and month = :month and year = :year")
    fun getHistoryByAssessmentInfo(grade: Int, month: Int, year: Int): AssessmentSchoolHistory?
}
