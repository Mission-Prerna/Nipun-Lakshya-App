package com.data.repository

import com.data.db.models.entity.Actor
import com.data.db.models.entity.AssessmentType
import com.data.db.models.entity.Competency
import com.data.db.models.entity.Designation
import com.data.db.models.entity.ReferenceIds
import com.data.db.models.entity.Subjects
import kotlinx.coroutines.flow.Flow

interface MetadataRepository {

    fun getActors(): Flow<List<Actor>>
    fun getAssessmentTypes(): Flow<List<AssessmentType>>
    fun getSubjects(): Flow<List<Subjects>>
    fun getDesignations(): Flow<List<Designation>>
    fun getCompetencies(): Flow<List<Competency>>
    fun getReferenceIds(): Flow<List<ReferenceIds>>
    fun getCompetencies(grade: Int, searchTerm: String): List<Competency>
    fun getRefIdsFromCompetencyIds(competencyList: List<Int>): List<ReferenceIds>

    fun getRefIdsFromCompetencyIdsWithType(
        competencyIds: List<Int>,
        type: Int
    ): List<ReferenceIds>

    suspend fun areCompetenciesAvailable(): Boolean

//    suspend fun fetchMetadata(apiKey: String) : Result<Unit>

}