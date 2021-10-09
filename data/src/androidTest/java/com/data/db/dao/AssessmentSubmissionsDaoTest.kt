package com.data.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.data.MockData
import com.data.db.Convertors
import com.data.db.NLDatabase
import com.data.db.models.entity.Student
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AssessmentSubmissionsDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var assessmentSubmissionsDao: AssessmentSubmissionsDao
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
        assessmentSubmissionsDao = nlDatabase.getAssessmentSubmissionDao()
        studentsDao = nlDatabase.getStudentsDao()
    }

    @Test
    fun getSubmissions_without_insertion() = runBlocking {
        Assert.assertEquals(0, assessmentSubmissionsDao.getSubmissionsCount())
    }
    @Test(expected = NullPointerException::class)
    fun getSubmissionsCount_Expected_NPE() = runBlocking {
        assessmentSubmissionsDao.insert(listOf(MockData.getDummyNullAssessmentSubmission()))
        Assert.assertEquals(1, assessmentSubmissionsDao.getSubmissionsCount())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_Record_Expect_Foreign_Key_Constraint_Failed() = runBlocking {
        assessmentSubmissionsDao.insert(listOf(MockData.getDummyAssessmentSubmission()))
        Assert.assertEquals(1, assessmentSubmissionsDao.getSubmissionsCount())
    }

    @Test
    fun getSubmissions_gives_result() = runBlocking {
        val student = Student(
            id = "123",
            name = "Ujjwal",
            grade = 1,
            rollNo = 12,
            isPlaceHolderStudent = false
        )
        studentsDao.insert(listOf(student))
        assessmentSubmissionsDao.insert(listOf(MockData.getDummyAssessmentSubmission()))
        Assert.assertEquals(1, assessmentSubmissionsDao.getSubmissionsCount())
        Assert.assertEquals(201, assessmentSubmissionsDao.getSubmissions()[0].studentSubmissions?.actorId)
    }



    @After
    fun tearDown() {
        nlDatabase.close()
    }

}