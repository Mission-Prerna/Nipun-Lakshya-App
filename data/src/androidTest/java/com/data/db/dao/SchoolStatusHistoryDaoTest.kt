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

class SchoolStatusHistoryDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var schoolsStatusHistoryDao: SchoolsStatusHistoryDao
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
        schoolsStatusHistoryDao = nlDatabase.getSchoolStatusHistory()
        schoolsDao = nlDatabase.getSchoolDao()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_school_status_history_gives_exception(){
        val school1 = MockData.generateDummySchool() // udise 1234...
        val school2 = MockData.generateAnotherDummySchool() // udise 9876...
        schoolsDao.insert(listOf(school1, school2))
        schoolsStatusHistoryDao.insert(listOf(MockData.getSchoolStatusHistory(),MockData.getSchoolStatusHistory2(),MockData.getSchoolStatusHistory3())) // throws foreign key constraint exception since not handled via try catch
    }

    @Test
    fun getSchoolStatuses_without_insertion_gives_empty_list() = runBlocking {
        val result3 = schoolsStatusHistoryDao.getSchoolStatuses()
        Assert.assertEquals(0, result3.size)
    }
    @Test
    fun getSchoolStatuses_for_valid_cycle_id_gives_result() = runBlocking{
        insertDataWithSafety()
        val result2 = schoolsStatusHistoryDao.getSchoolStatuses(cycleId = 3)
        Assert.assertEquals(1, result2.size)
    }

    @Test
    fun getSchoolStatuses_for_invalid_cycle_id_gives_empty_list() = runBlocking{
        insertDataWithSafety()
        val result = schoolsStatusHistoryDao.getSchoolStatuses(cycleId = -4)
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun getAllSchoolStatuses_gives_result() = runBlocking {
        insertDataWithSafety()
        val result3 = schoolsStatusHistoryDao.getSchoolStatuses()
        Assert.assertEquals(2, result3.size)
    }

    @Test
    fun getSchoolStatusByValidCycleAndUdise_gives_result() = runBlocking {
        insertDataWithSafety()
        val result5 = schoolsStatusHistoryDao.getSchoolStatusByCycleAndUdise(
            cycleId = 3,
            udise = 1234567890L
        )
        Assert.assertEquals("nipun", result5[0].status)
    }

    @Test
    fun getSchoolStatusByInvalidCycleAndUdise_gives_empty_list() = runBlocking {
        insertDataWithSafety()
        val result5 = schoolsStatusHistoryDao.getSchoolStatusByCycleAndUdise(
            cycleId = 3,
            udise = 123
        )
        Assert.assertEquals(0, result5.size)
    }

    @Test(expected = NullPointerException::class)
    fun getSchoolStatusByUdise_without_data_insertion() = runBlocking {
        val result6 = schoolsStatusHistoryDao.getSchoolStatuses(udise = 9876543210L)
        Assert.assertEquals("Another Dummy School", result6.schoolname)
        Assert.assertNotEquals("nipun", result6.status)
    }

    @Test
    fun getSchoolStatusByUdise_gives_result() = runBlocking {
        insertDataWithSafety()
        val result6 = schoolsStatusHistoryDao.getSchoolStatuses(udise = 9876543210L)
        Assert.assertEquals("Another Dummy School", result6.schoolname)
        Assert.assertNotEquals("nipun", result6.status)
    }
    @Test
    fun deleteSchoolStatusHistory_gives_empty_list() = runBlocking {
        schoolsStatusHistoryDao.delete()
        Assert.assertEquals(0, schoolsStatusHistoryDao.getSchoolStatuses().size)
    }



    private fun insertDataWithSafety(){
        val school1 = MockData.generateDummySchool()
        val school2 = MockData.generateAnotherDummySchool()
        schoolsDao.insert(listOf(school1, school2))
        schoolsStatusHistoryDao.insertWithSafety(listOf(MockData.getSchoolStatusHistory(),MockData.getSchoolStatusHistory2(),MockData.getSchoolStatusHistory3())) // inserts records with try catch safety, records without exception will be inserted
    }
    @After
    fun tearDown() {
        nlDatabase.close()
    }
}