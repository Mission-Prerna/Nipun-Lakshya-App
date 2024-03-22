package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.entity.AssessmentSubmission
import com.data.db.models.entity.SchoolSubmission

@Dao
interface SchoolSubmissionsDao {
    @Insert
    fun insert(submission: SchoolSubmission)

    @Query("DELETE from school_submissions where id = :id")
    fun delete(id: Long)

    @Query("SELECT * from school_submissions")
    fun getSubmissions() : List<SchoolSubmission>

    @Query("SELECT count(*) from school_submissions")
    fun getSubmissionsCount() : Long

}
