package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.entity.AssessmentState
import com.data.db.models.helper.AssessmentStateDetails
import com.data.db.models.helper.FlowStateStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentStateDao {

    @Query("SELECT * FROM assessment_state")
    fun getStates(): MutableList<AssessmentState>

    @Query("SELECT * FROM assessment_state")
    fun observeStates(): Flow<MutableList<AssessmentState>>

    @Query("SELECT a.*, c.learning_outcome, c.subject_id, sub.name AS subject_name, s.name as student_name, c.grade " +
            "FROM assessment_state AS a " +
            "LEFT JOIN competencies AS c ON a.competency_id = c.id " +
            "LEFT JOIN students AS s ON a.student_id = s.id " +
            "LEFT JOIN subjects AS sub ON c.subject_id = sub.id " +
            "where state_status = :stateStatus " +
            "ORDER BY id asc")
    fun observeIncompleteStates(stateStatus: FlowStateStatus = FlowStateStatus.PENDING): Flow<MutableList<AssessmentStateDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(state: MutableList<AssessmentState>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(state: AssessmentState)

    @Query("DELETE FROM assessment_state")
    suspend fun deleteAllAsync()

    @Query("DELETE FROM assessment_state")
    fun deleteAll()

    @Query("SELECT a.*, c.learning_outcome, c.subject_id, sub.name AS subject_name, s.name as student_name, c.grade " +
            "FROM assessment_state AS a " +
            "LEFT JOIN competencies AS c ON a.competency_id = c.id " +
            "LEFT JOIN students AS s ON a.student_id = s.id " +
            "LEFT JOIN subjects AS sub ON c.subject_id = sub.id")
    fun getDetailedStates(): MutableList<AssessmentStateDetails>

    @Query("SELECT * FROM assessment_state where state_status is not :statesNotCovered")
    fun getIncompleteStates(statesNotCovered: FlowStateStatus = FlowStateStatus.COMPLETED): MutableList<AssessmentState>
}
