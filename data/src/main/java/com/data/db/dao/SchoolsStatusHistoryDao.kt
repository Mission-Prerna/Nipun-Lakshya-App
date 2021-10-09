package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.data.db.models.entity.SchoolStatusHistory
import com.data.db.models.helper.SchoolDetailsWithReportHistory
import com.data.db.models.helper.SchoolWithReportHistory
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface SchoolsStatusHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(schoolStatusHistory: List<SchoolStatusHistory>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(schoolStatusHistory: SchoolStatusHistory) : Long

    fun insertWithSafety(schoolStatusHistory: List<SchoolStatusHistory>) {
        schoolStatusHistory.forEach {
            try {
                insert(it)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
    @Query("DELETE from school_status_history")
    fun delete()

    @Query("SELECT * from school_status_history")
    fun getSchoolStatuses(): List<SchoolStatusHistory>

    @Query("SELECT * from school_status_history where cycle_id = :cycleId")
    fun getSchoolStatuses(cycleId: Int): List<SchoolStatusHistory>

    @Query("SELECT * from school_status_history where cycle_id = :cycleId and udise = :udise")
    fun getSchoolStatusByCycleAndUdise(cycleId: Int, udise: Long): List<SchoolStatusHistory>

    @Query(
        "SELECT school.school_name, school.udise, history.updated_at, history.status" +
                " from school_status_history as history " +
                "LEFT JOIN schools as school ON history.udise = school.udise " +
                "where school.udise = :udise limit 1"
    )
    suspend fun getSchoolStatuses(udise: Long): SchoolWithReportHistory

    @Query(
        "SELECT school.*, history.updated_at, history.status " +
                "from schools as school " +
                "LEFT JOIN school_status_history as history " +
                "ON history.udise = school.udise"
    )
    fun getSchoolsWithStatus(): Flow<List<SchoolDetailsWithReportHistory>>


}
