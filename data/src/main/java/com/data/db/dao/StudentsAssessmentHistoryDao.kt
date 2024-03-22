package com.data.db.dao

import androidx.room.*
import com.data.db.models.entity.StudentAssessmentHistory
import com.data.db.models.helper.SchoolAssessmentCount
import com.data.db.models.helper.StudentWithAssessmentHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentsAssessmentHistoryDao {

    @Transaction
    @Query(
        "SELECT students.id, students.name, students.grade, students.roll_no, students.is_place_holder_student, " +
                "students_assessment_history.status, students_assessment_history.last_assessment_date, students_assessment_history.month " +
                "FROM students " +
                "LEFT JOIN students_assessment_history ON students.id = students_assessment_history.id " + // Use the "id" column as the foreign key
                "WHERE students.grade = :grade " +
                "AND is_place_holder_student = 0 " +
                "AND students.school_udise = :udise " +
                "AND students_assessment_history.month = :month " +
                "AND students_assessment_history.year = :year ORDER BY students.name ASC"
    )
    fun getStudentsByGradeMonthYear(
        udise: Long,
        grade: Int,
        month: Int,
        year: Int
    ): Flow<MutableList<StudentWithAssessmentHistory>>

    @Transaction
    @Query(
        "SELECT students.id, students.name, students.grade, students.roll_no, students.is_place_holder_student, " +
                "students_assessment_history.status, students_assessment_history.last_assessment_date, students_assessment_history.month " +
                "FROM students " +
                "LEFT JOIN students_assessment_history ON students.id = students_assessment_history.id " + // Use the "id" column as the foreign key
                "WHERE students.grade = :grade " +
                "AND is_place_holder_student = 0 " +
                "AND students_assessment_history.cycle_id = :cycleId " +
                "AND students_assessment_history.udise = :udise " +
                "ORDER BY students.name ASC"
    )
    fun getStudentsByGradeCycle(
        udise: Long,
        grade: Int,
        cycleId: Int
    ): Flow<MutableList<StudentWithAssessmentHistory>>

    @Transaction
    @Query(
        "SELECT students.id, students.name, students.grade, students.roll_no, students.is_place_holder_student, " +
                "students_assessment_history.status, students_assessment_history.last_assessment_date, students_assessment_history.month " +
                "FROM students " +
                "LEFT JOIN students_assessment_history ON students.id = students_assessment_history.id " + // Use the "id" column as the foreign key
                "WHERE students.school_udise = :schoolUdise " +
                "AND students_assessment_history.status IS NOT NULL " +
                "AND is_place_holder_student = 0 " +
                "ORDER BY students.grade ASC, students.name ASC"
    )
    fun getStudentsForUdise(schoolUdise: Long): Flow<List<StudentWithAssessmentHistory>>

    @Query("SELECT * from students_assessment_history where month = :month and year = :year")
    suspend fun getAllHistories(month: Int, year: Int): List<StudentAssessmentHistory>

    @Query("SELECT * from students_assessment_history where cycle_id = :cycleId")
    suspend fun getAllHistories(cycleId: Int): List<StudentAssessmentHistory>

    @Query("SELECT * " +
            "FROM students_assessment_history AS SAH " +
            "JOIN students AS S ON SAH.id = S.id " +
            "WHERE SAH.cycle_id = :cycleId " +
            "AND S.grade IN (:grades)")
    suspend fun getAllHistories(cycleId: Int, grades : List<Int>): List<StudentAssessmentHistory>

    @Query(
        "SELECT students.id, students.name, students.grade, students.roll_no, students.is_place_holder_student, " +
                "students_assessment_history.status, students_assessment_history.last_assessment_date, students_assessment_history.month " +
                "FROM students " +
                "LEFT JOIN students_assessment_history ON students.id = students_assessment_history.id " + // Use the "id" column as the foreign key
                "WHERE students.id = :studentId " +
                "AND is_place_holder_student = 0 " +
                "AND students_assessment_history.month = :month " +
                "AND students_assessment_history.year = :year"
    )
    suspend fun getHistoryByAssessmentInfo(
        studentId: String,
        month: Int,
        year: Int
    ): StudentWithAssessmentHistory

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentAssessmentHistoryList: List<StudentAssessmentHistory>)
    //timestamp check with column value in this table

    @Query("DELETE from students_assessment_history where id=:studentId")
    suspend fun deleteForId(studentId: String)

    @Query("DELETE from students_assessment_history where id in (:studentIds)")
    suspend fun deleteForIds(studentIds: List<String>) : Int

    @Query(
        "SELECT COUNT(*) AS total_students, SUM(CASE WHEN status != 'pending' THEN 1 ELSE 0 END) AS total_non_pending " +
                "FROM students_assessment_history " +
                "WHERE cycle_id = :cycleId " +
                "AND udise = :udise"
    )
    fun getSchoolAssessmentCount(udise: Long, cycleId: Int): Flow<SchoolAssessmentCount>

    @Query(
        "SELECT students.id, students.name, students.grade, students.roll_no, students.is_place_holder_student, " +
                "students_assessment_history.status, students_assessment_history.last_assessment_date, students_assessment_history.month " +
                "FROM students " +
                "LEFT JOIN students_assessment_history ON students.id = students_assessment_history.id " + // Use the "id" column as the foreign key
                "WHERE school_udise = :udise " +
                "AND students_assessment_history.cycle_id = :cycleId " +
                "AND status = 'pending' ORDER BY students.grade ASC, students.name ASC"
    )
    suspend fun getPendingStudentsWithStatus(
        udise: Long, cycleId: Int
    ): List<StudentWithAssessmentHistory>

    @Query(
        "SELECT students.id, students.name, students.grade, students.roll_no, students.is_place_holder_student, " +
                "students_assessment_history.status, students_assessment_history.last_assessment_date, students_assessment_history.month " +
                "FROM students " +
                "LEFT JOIN students_assessment_history ON students.id = students_assessment_history.id " + // Use the "id" column as the foreign key
                "WHERE school_udise = :udise " +
                "AND students_assessment_history.cycle_id = :cycleId "
    )
    suspend fun getStudentsWithStatus(
        udise: Long, cycleId: Int
    ): List<StudentWithAssessmentHistory>
}
