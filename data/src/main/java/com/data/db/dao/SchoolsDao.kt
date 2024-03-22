package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.entity.School

@Dao
interface SchoolsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(schools: List<School>): List<Long>

    @Query("DELETE from schools")
    fun delete()

    @Query("SELECT * from schools")
    fun getSchools() : List<School>

    @Query("SELECT udise from schools where is_visited=1")
    fun getUdiseForVisitedSchools(): List<Long>

    @Query("UPDATE schools SET is_visited = 1 WHERE udise = :udise")
    suspend fun markSchoolAsVisited(udise: Long)

}
