package com.data.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.data.MockData
import com.data.db.Convertors
import com.data.db.NLDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SchoolSubmissionsDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var schoolSubmissionsDao: SchoolSubmissionsDao
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
        schoolSubmissionsDao = nlDatabase.getSchoolSubmissionDao()
        schoolsDao = nlDatabase.getSchoolDao()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun missing_schools_data_gives_foreign_key_constraint_failed() = runBlocking {
        schoolSubmissionsDao.insert(MockData.getDummySchoolSubmissionData())

        val result = schoolSubmissionsDao.getSubmissions()
        Assert.assertEquals(1, result.size)
    }

    @Test
    fun getSubmissions_without_data_insertion_gives_empty_list() = runBlocking {
        val result = schoolSubmissionsDao.getSubmissions()
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun getSubmissions_gives_valid_result() = runBlocking {
        insertData()
        val result = schoolSubmissionsDao.getSubmissions()
        Assert.assertEquals(1, result.size)
    }

    @Test
    fun getSubmissionsCount_gives_valid_result() = runBlocking {
        insertData()
        val result2 = schoolSubmissionsDao.getSubmissionsCount()
        Assert.assertEquals(1, result2)
    }

    private fun insertData(){
       schoolsDao.insert(listOf(MockData.getDummySchoolData2()))
       schoolSubmissionsDao.insert(MockData.getDummySchoolSubmissionData())
    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }
}