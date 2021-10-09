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

class TeacherPerformanceInsightsDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var teacherPerformanceInsightsDao: TeacherPerformanceInsightsDao
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
        teacherPerformanceInsightsDao = nlDatabase.getTeacherPerformanceInsightsDao()
    }

    @Test
    fun getInsights_without_insertion_gives_empty_list() = runBlocking {
        val result = teacherPerformanceInsightsDao.getTeacherPerformanceInsights().first()
        Assert.assertEquals(0, result.size)
    }
    @Test
    fun getInsights_gives_result() = runBlocking {
        teacherPerformanceInsightsDao.insert(MockData.getMockInsightsTeacher())

        val result = teacherPerformanceInsightsDao.getTeacherPerformanceInsights().first()
        Assert.assertEquals(2, result.size)
    }

    @Test
    fun insert_Conflict_Insights_gives_result() = runBlocking {
        teacherPerformanceInsightsDao.insert(MockData.getMockConflictInsightsTeacher())

        val result = teacherPerformanceInsightsDao.getTeacherPerformanceInsights().first()
        Assert.assertNotEquals(3, result.size)
        Assert.assertEquals(2, result.size)
    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }

}