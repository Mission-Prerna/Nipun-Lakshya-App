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

class CycleDetailsDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var cycleDetailsDao: CycleDetailsDao
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
        cycleDetailsDao = nlDatabase.getMentorDetailsDao()
    }

    @Test
    fun getCycleDetails_without_insertion_gives_null() = runBlocking {
        val result = cycleDetailsDao.getCycleDetails().first()
        Assert.assertEquals(null, result)
    }
    @Test
    fun getCycleDetails_gives_result() = runBlocking {
        insertData()

        val result = cycleDetailsDao.getCycleDetails().first()
        val result4 = cycleDetailsDao.getCycleDetails(3)
        Assert.assertEquals(3, result.id)
        Assert.assertEquals(MockData.getDummyCycleDetails(), result4)
    }

    @Test
    fun getCurrentCycleDetails_gives_result() = runBlocking {
        insertData()

        val result2 = cycleDetailsDao.getCurrentCycleDetails()?.name
        val result3 = cycleDetailsDao.getCurrentCycleId()
        Assert.assertEquals("Cycle 4", result2)
        Assert.assertEquals(4, result3)
    }

    private fun insertData() = runBlocking {
        cycleDetailsDao.insert(MockData.getDummyCycleDetails())
        cycleDetailsDao.insert(MockData.getDummyCycleDetailsNew())
    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }

}