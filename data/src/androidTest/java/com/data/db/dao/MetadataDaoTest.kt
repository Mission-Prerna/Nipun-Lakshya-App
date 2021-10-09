package com.data.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.data.MockData
import com.data.db.Convertors
import com.data.db.NLDatabase
import com.data.db.models.entity.Actor
import com.data.db.models.entity.Competency
import com.data.db.models.entity.Subjects
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MetadataDaoTest {

    private lateinit var nlDatabase: NLDatabase
    private lateinit var metadataDao: MetadataDao
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
    }

    @Test
    fun getActors_without_insertion_gives_empty_list() = runBlocking {
        val result = metadataDao.getActors().first()
        Assert.assertEquals(0, result.size)
    }
    @Test
    fun getActors_gives_result() = runBlocking {
        metadataDao.insertActors(MockData.getDummyActors())
        val result = metadataDao.getActors().first()

        Assert.assertEquals(2, result.size)
    }

    @Test
    fun update_Actor_gives_result() = runBlocking {
        metadataDao.insertActors(MockData.getDummyActors())
        val newActor = Actor(0,"NewDummy")
        metadataDao.updateActors(newActor)
        val result = metadataDao.getActors().first()

        Assert.assertEquals(2, result.size)
        Assert.assertEquals("NewDummy", result[1].name)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_invalid_competency_gives_exception() = runBlocking {
        val competency = Competency(
            id = 0,
            subjectId = 123,
            grade = 12,
            learningOutcome = "dummy",
            flowState = 1,
            month = "Jan"
        )
        metadataDao.insertCompetency(listOf(competency))
        val result = metadataDao.getCompetency().first()

        Assert.assertEquals(1, result.size)

    }

    @Test
    fun insert_valid_competency_gives_result() = runBlocking {
        val subject = Subjects(123,"Hindi")
        metadataDao.insertSubjects(listOf(subject))
        val competency = Competency(
            id = 0,
            subjectId = 123,
            grade = 12,
            learningOutcome = "dummy",
            flowState = 1,
            month = "Jan"
        )
        metadataDao.insertCompetency(listOf(competency))
        val result = metadataDao.getCompetency().first()

        Assert.assertEquals(1, result.size)

    }

    @After
    fun tearDown() {
        nlDatabase.close()
    }
}