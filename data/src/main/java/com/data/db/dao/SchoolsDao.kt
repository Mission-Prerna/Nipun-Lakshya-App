package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.entity.School
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(schools: List<School>): List<Long>

    @Query("DELETE from schools")
    fun delete()

    @Query("SELECT * from schools")
    fun getSchools() : Flow<List<School>>

}
