package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.entity.CycleDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDetailsDao {

    @Query("SELECT * FROM cycle_details")
    fun getCycleDetails(): Flow<CycleDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycleDetails: CycleDetails)

    @Query("Select * from cycle_details order by end_date desc limit 1")
    suspend fun getCurrentCycleDetails(): CycleDetails?

    @Query("Select id from cycle_details order by end_date desc limit 1")
    suspend fun getCurrentCycleId() : Int

    @Query("Select * from cycle_details where id = :cycleId")
    suspend fun getCycleDetails(cycleId : Int) : CycleDetails
}