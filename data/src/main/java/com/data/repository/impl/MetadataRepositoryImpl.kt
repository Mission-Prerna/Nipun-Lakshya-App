package com.data.repository.impl

import com.data.db.dao.MetadataDao
import com.data.db.models.entity.Actor
import com.data.db.models.entity.AssessmentType
import com.data.db.models.entity.Competency
import com.data.db.models.entity.Designation
import com.data.db.models.entity.ReferenceIds
import com.data.db.models.entity.Subjects
import com.data.network.MetadataService
import com.data.repository.MetadataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val service: MetadataService,
    private val metadataDao: MetadataDao
) : MetadataRepository {
    override fun getActors(): Flow<List<Actor>> {
        return metadataDao.getActors()
    }

    override suspend fun areCompetenciesAvailable(): Boolean {
        return metadataDao.getCompetencyCount() > 0
    }

    override fun getAssessmentTypes(): Flow<List<AssessmentType>> {
        return metadataDao.getAssessmentTypes()
    }

    override fun getSubjects(): Flow<List<Subjects>> {
        return metadataDao.getSubjects()
    }

    override fun getDesignations(): Flow<List<Designation>> {
        return metadataDao.getDesignations()
    }

    override fun getCompetencies(): Flow<List<Competency>> {
        return metadataDao.getCompetency()
    }

    override fun getReferenceIds(): Flow<List<ReferenceIds>> {
        return metadataDao.getReferenceIds()
    }

    override fun getRefIdsFromCompetencyIds(competencyList: List<Int>): List<ReferenceIds> {
        return metadataDao.getRefIdsFromCompetencyIds(competencyList)
    }

    override fun getRefIdsFromCompetencyIdsWithType(
        competencyIds: List<Int>,
        type: Int
    ) = metadataDao.getRefIdsFromCompetencyIdsAndType(competencyIds, type)

    override fun getCompetencies(grade: Int, searchTerm: String): List<Competency> {
        return metadataDao.getCompetencyIdsList(grade, searchTerm)
    }

    /*    override suspend fun fetchMetadata(apiKey: String): Result<Unit> {
            try {
                val response = service.fetchMetaData(apiKey)
                if (response.isSuccessful) {
                    val metadataFields = response.body()
                    if (metadataFields?.actors.isNullOrEmpty().not()) {
                        //insert actors in db
                        metadataDao.insertActors(metadataFields?.actors)
                    }
                    if (metadataFields?.assessmentTypes.isNullOrEmpty().not()) {
                        //insert assessment types in db
                        metadataDao.insertAssessmentTypes(metadataFields?.assessmentTypes)
                    }
                    if (metadataFields?.subjects.isNullOrEmpty().not()) {
                        //insert subjects in db
                        metadataDao.insertSubjects(metadataFields?.subjects)
                    }
                    if (metadataFields?.designations.isNullOrEmpty().not()) {
                        //insert designations in db
                        metadataDao.insertDesignations(metadataFields?.designations)
                    }
                    if (metadataFields?.competencyMapping.isNullOrEmpty().not()) {
                        //insert competencies in db
                        metadataDao.insertCompetency(metadataFields?.competencyMapping)
                    }
                    if (metadataFields?.workflowRefIds.isNullOrEmpty().not()) {
                        //insert workflow ref ids in db
                        metadataDao.insertReferenceIds(metadataFields?.workflowRefIds)
                    }
                    return Result.Success(Unit)
                }
                return Result.Error(Exception(response.errorBody()?.string() ?: response.message()))
            } catch (e: Exception) {
                return Result.Error(e)
            }
        }*/
}