package com.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.data.MockData
import com.data.db.Convertors
import com.data.db.NLDatabase
import com.data.db.models.entity.Student
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentsDaoTest {

    private lateinit var nlDatabase: NLDatabase
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
        studentsDao = nlDatabase.getStudentsDao()
    }

    @Test
    fun getStudents_without_insertion() = runBlocking {
        val result = studentsDao.getStudents(grade = 10).first()
        Assert.assertEquals(0, result.size)
    }
    @Test
    fun getStudents_gives_result() = runBlocking {
        insertMockStudents()

        val result = studentsDao.getStudents(grade = 10).first()
        Assert.assertEquals(2, result.size)
    }

    @Test
    fun getStudentNameById_gives_result() = runBlocking {
        insertMockStudents()

        val result = studentsDao.getStudentNameById(id = "1")
        Assert.assertEquals("Ujjwal", result)
    }

    @Test
    fun getStudentNameByInvalidId_gives_null() = runBlocking {
        insertMockStudents()

        val result = studentsDao.getStudentNameById(id = "-2")
        Assert.assertEquals(null, result)
    }

    @Test
    fun updateStudent_NoConflict_gives_result() = runBlocking {
        val student = Student(
            id = "1",
            name = "Ujjwal",
            grade = 10,
            rollNo = 123,
            isPlaceHolderStudent = false,
            schoolUdise = 12345
        )
        val student2 = Student(
            id = "2",
            name = "Rahul",
            grade = 10,
            rollNo = 124,
            isPlaceHolderStudent = false,
            schoolUdise = 12345
        )
        studentsDao.insert(listOf(student, student2))

        val student3 = Student(
            id = "3",
            name = "Ajay",
            grade = 10,
            rollNo = 125,
            isPlaceHolderStudent = false,
            schoolUdise = 12345
        )
        studentsDao.update(student3)
        val result = studentsDao.getStudents(grade = 10).first()
        Assert.assertEquals(3, result.size)
    }

    @Test
    fun updateStudent_Conflict_gives_result() = runBlocking {
        val student = Student(
            id = "1",
            name = "Ujjwal",
            grade = 10,
            rollNo = 123,
            isPlaceHolderStudent = false,
            schoolUdise = 12345
        )
        val student2 = Student(
            id = "2",
            name = "Rahul",
            grade = 10,
            rollNo = 124,
            isPlaceHolderStudent = false,
            schoolUdise = 12345
        )
        studentsDao.insert(listOf(student, student2))

        val student3 = Student(
            id = "2",
            name = "Rahul",
            grade = 10,
            rollNo = 124,
            isPlaceHolderStudent = false,
            schoolUdise = 12345
        )
        studentsDao.update(student3)
        val result = studentsDao.getStudents(grade = 10).first()
        Assert.assertEquals(2, result.size)
    }

    @Test
    fun getStudentGradeById_gives_result() = runBlocking {
        insertMockStudents()

        val result = studentsDao.getStudentGradeById(id = "2")
        Assert.assertEquals(11, result)
        Assert.assertNotEquals(10, result)
        Assert.assertNotEquals(12, result)
    }

    @Test(expected = NullPointerException::class)
    fun getStudentGradeByInvalidId_gives_npe() = runBlocking {
        insertMockStudents()

        val result = studentsDao.getStudentGradeById(id = "-12")
        Assert.assertEquals(11, result)
    }

    @Test
    fun getGradesList_gives_result() = runBlocking {
        insertMockStudents()

        val result = studentsDao.getGradesList().first()
        Assert.assertEquals(4, result.size)
        Assert.assertNotEquals(5, result.size)
    }

    @Test
    fun getStudentsCountByGrade_Expected_Result() = runBlocking {
        insertMockStudents()
        val result = studentsDao.getStudentsCountByGrade(grade = 10)
        val result2 = studentsDao.getStudentsCountByGrade(grade = 12)
        Assert.assertEquals(2, result)
        Assert.assertEquals(1, result2)
    }

    @Test
    fun getStudentsCountByInvalidGrade_gives_empty_list() = runBlocking {
        insertMockStudents()
        val result = studentsDao.getStudentsCountByGrade(grade = -10)
        Assert.assertEquals(0, result)
    }

    @Test
    fun insert_EmptyList_Expected_Empty() = runBlocking {
        studentsDao.insert(emptyList())
        val result = studentsDao.getStudents(grade = 10).first()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun getStudents_NonExistingGrade_Expected_Empty() = runBlocking {
        val result = studentsDao.getStudents(grade = 99).first()
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun getStudentNameById_NonExistingID_Expected_Result() = runBlocking {
        val result = studentsDao.getStudentNameById(id = "non_existing_id")
        Assert.assertTrue(result.isNullOrEmpty())
    }
    
    private fun insertMockStudents() = runBlocking{
        studentsDao.insert(MockData.getMockStudents2())
    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }
}