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

class SchoolsDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var schoolsDao: SchoolsDao
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
        schoolsDao = nlDatabase.getSchoolDao()
    }

    @Test
    fun getSchools_without_insertion_gives_empty_list() = runBlocking {
        val result = schoolsDao.getSchools()
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun getSchools_gives_result() = runBlocking {
        schoolsDao.insert(listOf(MockData.getDummySchoolData()))

        val result = schoolsDao.getSchools()
        Assert.assertEquals(1, result.size)
    }

    @Test
    fun delete_schools_gives_empty_list() = runBlocking {
        schoolsDao.delete()

        val result = schoolsDao.getSchools()
        Assert.assertNotEquals(1, result.size)
        Assert.assertEquals(0, result.size)
    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }

}