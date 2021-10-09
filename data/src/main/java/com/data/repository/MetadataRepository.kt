package com.data.repository

import com.data.db.models.entity.Actor
import com.data.db.models.entity.AssessmentType
import com.data.db.models.entity.Competency
import com.data.db.models.entity.Designation
import com.data.db.models.entity.ReferenceIds
import com.data.db.models.entity.Subjects
import com.samagra.commons.basemvvm.BaseRepository
import kotlinx.coroutines.flow.Flow

abstract class MetadataRepository: BaseRepository() {

    abstract fun getActors(): Flow<List<Actor>>
    abstract fun getAssessmentTypes(): Flow<List<AssessmentType>>
    abstract fun getSubjects(): Flow<List<Subjects>>
    abstract fun getDesignations(): Flow<List<Designation>>
    abstract fun getLearningOutcomeForRefIdsOfCompetency(grade: Int): List<Competency>
    abstract fun getCompetencies(): Flow<List<Competency>>
    abstract fun getReferenceIds(): Flow<List<ReferenceIds>>
    abstract fun getCompetencies(grade: Int, searchTerm: String): List<Competency>
    abstract fun getDistinctGrades(): List<Int>
    abstract fun getRefIdsFromCompetencyIds(competencyList: List<Int>): List<ReferenceIds>

    abstract fun getRefIdsFromCompetencyIdsWithType(
        competencyIds: List<Int>,
        type: Int
    ): List<ReferenceIds>

    abstract suspend fun areCompetenciesAvailable(): Boolean

//    suspend fun fetchMetadata(apiKey: String) : Result<Unit>

}