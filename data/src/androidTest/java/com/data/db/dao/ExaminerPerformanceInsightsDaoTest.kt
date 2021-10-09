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

class ExaminerPerformanceInsightsDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var examinerPerformanceInsightsDao: ExaminerPerformanceInsightsDao
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
        examinerPerformanceInsightsDao = nlDatabase.getExaminerPerformanceInsightsDao()
    }

    @Test
    fun getInsights_without_insertion_gives_empty_list() = runBlocking {
        val result = examinerPerformanceInsightsDao.getExaminerPerformanceInsights().first()
        Assert.assertEquals(0, result.size)
    }
    @Test
    fun getInsights_gives_result() = runBlocking {
        examinerPerformanceInsightsDao.insert(MockData.getMockInsightsExaminer())

        val result = examinerPerformanceInsightsDao.getExaminerPerformanceInsights().first()
        Assert.assertEquals(2, result.size)
    }

    @Test
    fun insert_Conflict_Insights_get_result() = runBlocking {
        examinerPerformanceInsightsDao.insert(MockData.getMockConflictInsightsExaminer())

        val result = examinerPerformanceInsightsDao.getExaminerPerformanceInsights().first()
        Assert.assertNotEquals(3, result.size)
        Assert.assertEquals(2, result.size)
    }



    @After
    fun tearDown() {
        nlDatabase.close()
    }

}