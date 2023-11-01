package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentsDao {

    @Query("SELECT * FROM students where grade = :grade AND is_place_holder_student is 0 ORDER BY name ASC")
    fun getStudents(grade : Int): Flow<MutableList<Student>>

    @Query("SELECT name FROM students where id = :id")
    suspend fun getStudentNameById(id : String): String

    @Query("SELECT grade FROM students where id = :id")
    suspend fun getStudentGradeById(id : String): Int

    @Query("SELECT DISTINCT grade FROM students ORDER BY grade ASC")
    fun getGradesList(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(launches: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(launch: Student)

    @Query("SELECT count(*) from students where grade = :grade AND is_place_holder_student is 0")
    suspend fun getStudentsCountByGrade(grade : Int): Int
}
