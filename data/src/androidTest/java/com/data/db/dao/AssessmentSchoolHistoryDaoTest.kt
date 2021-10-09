package com.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.data.MockData
import com.data.db.Convertors
import com.data.db.NLDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AssessmentSchoolHistoryDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var assessmentSchoolHistoryDao: AssessmentSchoolHistoryDao
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
        assessmentSchoolHistoryDao = nlDatabase.getAssessmentSchoolHistoryDao()
    }

    @Test
    fun getHistories_without_insertion() = runBlocking {
        val result = assessmentSchoolHistoryDao.getHistories(grades = emptyList())
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun getHistories_with_empty_grade_list() = runBlocking {
        insertMockHistories()
        val result = assessmentSchoolHistoryDao.getHistories(grades = emptyList())
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun getHistories_for_invalid_input() = runBlocking {
        insertMockHistories()

        val result = assessmentSchoolHistoryDao.getHistories(grades = listOf(-12))
        Assert.assertEquals(0, result.size)
    }
    @Test
    fun getHistories_gives_valid_result() = runBlocking {
        insertMockHistories()

        val result = assessmentSchoolHistoryDao.getHistories(grades = listOf(1))
        Assert.assertEquals(1, result.size)
    }

    @Test
    fun getHistories_async_gives_valid_result() = runBlocking {
        insertMockHistories()

        val result = assessmentSchoolHistoryDao.getHistoriesAsync(grades = listOf(1)).first()
        Assert.assertEquals(1, result.size)
    }

    @Test
    fun getHistories_async_for_empty_list() = runBlocking {
        insertMockHistories()

        val result = assessmentSchoolHistoryDao.getHistoriesAsync(grades = emptyList()).first()
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun get_History_by_assessment_info_for_invalid_input() = runBlocking {
        insertMockHistories()

        val result = assessmentSchoolHistoryDao.getHistoryByAssessmentInfo(
            grade = 1,
            month = 0,
            year = 2023
        )

        Assert.assertEquals(null, result)
    }

    @Test
    fun get_History_by_assessment_info_gives_result() = runBlocking {
        insertMockHistories()

        val result = assessmentSchoolHistoryDao.getHistoryByAssessmentInfo(
            grade = 1,
            month = 12,
            year = 2023
        )

        Assert.assertEquals(MockData.getMockAssessmentSchoolHistory().successful, result?.successful)
    }

    private fun insertMockHistories(){
        assessmentSchoolHistoryDao.insert(history = MockData.getMockAssessmentSchoolHistory())
    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }

}