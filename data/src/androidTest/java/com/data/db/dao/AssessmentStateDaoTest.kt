package com.data.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.data.FlowType
import com.data.MockData
import com.data.db.Convertors
import com.data.db.NLDatabase
import com.data.db.models.entity.AssessmentState
import com.data.db.models.helper.FlowStateStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AssessmentStateDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var metadataDao: MetadataDao
    private lateinit var studentsDao: StudentsDao
    private lateinit var assessmentStateDao: AssessmentStateDao
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
        metadataDao = nlDatabase.getMetadataDao()
        studentsDao = nlDatabase.getStudentsDao()
        assessmentStateDao = nlDatabase.getAssessmentStateDao()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_Assessment_State_without_foreign_key_gives_exception(){
        val state = AssessmentState(
            id = 0,
            studentId = "123",
            refIds = mutableListOf(),
            competencyId = null,
            flowType = FlowType.ODK,
            result = null,
            stateStatus = FlowStateStatus.PENDING
        )
        assessmentStateDao.insert(mutableListOf(state))
    }

    @Test
    fun getState_without_insertion(){
        val result = assessmentStateDao.getStates()
        Assert.assertEquals(0,result.size)
    }
    @Test
    fun get_Assessment_State_gives_valid_result() = runBlocking {
        insertData()
        val result = assessmentStateDao.getStates()
        Assert.assertEquals(2, result.size)
        Assert.assertEquals("456", result[1].studentId)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun get_assessment_state_for_invalid_input() = runBlocking {
        metadataDao.insertSubjects(listOf(MockData.getSubject1(), MockData.getSubject2()))
        metadataDao.insertCompetency(listOf(MockData.getCompetency1(), MockData.getCompetency2()))
        studentsDao.insert(listOf(MockData.getStudent1(), MockData.getStudent2()))
        assessmentStateDao.insert(mutableListOf(MockData.getAssessmentState1(), MockData.getAssessmentState3()))

        val result = assessmentStateDao.getStates()
        Assert.assertEquals(2, result.size)
    }

    @Test
    fun observe_state_gives_valid_result() = runBlocking{
        insertData()
        val result2 = assessmentStateDao.observeStates().first()
        Assert.assertEquals(2,result2.size)
    }

    @Test
    fun observe_incomplete_state_gives_valid_result() = runBlocking{
        insertData()
        val result3 = assessmentStateDao.observeIncompleteStates().first()
        Assert.assertEquals("Maths",result3[0].subjectName)
        Assert.assertEquals(FlowStateStatus.PENDING,result3[0].stateStatus)
    }

    @Test
    fun getStates_after_updation() = runBlocking{
        insertData()
        val updatedAssessmentState = AssessmentState(
            id = 1,
            studentId = "456",
            refIds = mutableListOf(),
            competencyId = 2,
            flowType = FlowType.ODK,
            result = "failed",
            stateStatus = FlowStateStatus.COMPLETED
        )
        assessmentStateDao.update(updatedAssessmentState)
        val result4 = assessmentStateDao.getStates()
        Assert.assertEquals("failed",result4[1].result)
    }

    @Test
    fun getDetailedStates_gives_result() = runBlocking{
        insertData()
        val result5 = assessmentStateDao.getDetailedStates()
        Assert.assertEquals("Rahul",result5[1].studentName)
    }

    @Test
    fun getIncompleteStates_gives_result() = runBlocking{
        insertData()
        val result6 = assessmentStateDao.getIncompleteStates()
        Assert.assertEquals(1,result6.size)
    }

    @Test
    fun deleteStates_gives_result() = runBlocking{
        insertData()
        assessmentStateDao.deleteAllAsync()
        val result7 = assessmentStateDao.getStates()
        Assert.assertEquals(0,result7.size)
    }

    private fun insertData() = runBlocking(){
        metadataDao.insertSubjects(listOf(MockData.getSubject1(), MockData.getSubject2()))
        metadataDao.insertCompetency(listOf(MockData.getCompetency1(), MockData.getCompetency2()))
        studentsDao.insert(listOf(MockData.getStudent1(), MockData.getStudent2()))
        assessmentStateDao.insert(mutableListOf(MockData.getAssessmentState1(), MockData.getAssessmentState2()))
    }
    @After
    fun tearDown() {
        nlDatabase.close()
    }
}