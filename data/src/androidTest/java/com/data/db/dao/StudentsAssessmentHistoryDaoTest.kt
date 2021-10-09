package com.data.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.data.MockData
import com.data.db.Convertors
import com.data.db.NLDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentsAssessmentHistoryDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao
    private lateinit var studentsDao: StudentsDao
    private val convertors = Convertors()

    @Before
    fun setUp() {
        nlDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NLDatabase::class.java
        )
            .allowMainThreadQueries()
            .addTypeConverter(convertors)
            .build()
        studentsAssessmentHistoryDao = nlDatabase.getAssessmentHistoryDao()
        studentsDao = nlDatabase.getStudentsDao()

    }
    @Test
    fun getStudents_without_insertion() = runBlocking {
        val result = studentsAssessmentHistoryDao.getStudentsByGradeMonthYear(
            udise = 876,
            grade = 10,
            month = 12,
            year = 2023
        ).first()
        Assert.assertEquals(0, result.size)
    }
    @Test
    fun getStudentsByGradeMonthYear_Join_Tables_Gives_Result() = runBlocking{
        studentsDao.insert(MockData.getMockStudents())
        studentsAssessmentHistoryDao.insert(MockData.getMockStudentsAssessmentHistory())
        val result = studentsAssessmentHistoryDao.getStudentsByGradeMonthYear(
            udise = 876,
            grade = 10,
            month = 12,
            year = 2023
        ).first()
        Assert.assertEquals("Ujjwal", result[0].name)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun getStudentsByGradeMonthYear_Join_Tables_Invalid_Data_Gives_Exception() = runBlocking{
        studentsDao.insert(MockData.getMockStudents())
        studentsAssessmentHistoryDao.insert(MockData.getMockStudentsAssessmentHistoryInvalidData()) // foreign key constraint failure
        val result = studentsAssessmentHistoryDao.getStudentsByGradeMonthYear(
            udise = 876,
            grade = 10,
            month = 12,
            year = 2023
        ).first()
        Assert.assertEquals("Ujjwal", result[0].name)
    }

    @Test
    fun getStudentsByGradeCycle_Join_Tables_Gives_Result() = runBlocking{
        studentsDao.insert(MockData.getMockStudents())
        studentsAssessmentHistoryDao.insert(MockData.getMockStudentsAssessmentHistory())
        val result = studentsAssessmentHistoryDao.getStudentsByGradeCycle(
            udise = 123,
            grade = 12,
            cycleId = 1
        ).first()
        Assert.assertEquals(1, result.size)
    }

    @Test
    fun getStudentsByGradeCycle_Wrong_Cycle_Join_Tables_gives_empty_list() = runBlocking{
        studentsDao.insert(MockData.getMockStudents())
        studentsAssessmentHistoryDao.insert(MockData.getMockStudentsAssessmentHistory())
        val result = studentsAssessmentHistoryDao.getStudentsByGradeCycle(
            udise = 123,
            grade = 12,
            cycleId = 2
        ).first()
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun getStudentsForCorrectUdise_Join_Tables_Gives_Result() = runBlocking{
        studentsDao.insert(MockData.getMockStudents())
        studentsAssessmentHistoryDao.insert(MockData.getMockStudentsAssessmentHistory())
        val result = studentsAssessmentHistoryDao.getStudentsForUdise(schoolUdise = 123).first()
        Assert.assertEquals(5, result.size)
    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }
}