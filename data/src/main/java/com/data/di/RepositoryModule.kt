package com.data.di

import android.content.SharedPreferences
import com.data.db.dao.AssessmentSchoolHistoryDao
import com.data.db.dao.AssessmentStateDao
import com.data.db.dao.AssessmentSubmissionsDao
import com.data.db.dao.CycleDetailsDao
import com.data.db.dao.ExaminerPerformanceInsightsDao
import com.data.db.dao.MetadataDao
import com.data.db.dao.SchoolSubmissionsDao
import com.data.db.dao.SchoolsDao
import com.data.db.dao.SchoolsStatusHistoryDao
import com.data.db.dao.StudentsAssessmentHistoryDao
import com.data.db.dao.StudentsDao
import com.data.db.dao.TeacherPerformanceInsightsDao
import com.data.network.AssessmentService
import com.data.network.MetadataService
import com.data.network.SchoolService
import com.data.network.StudentsService
import com.data.repository.AssessmentsRepository
import com.data.repository.CycleDetailsRepository
import com.data.repository.MetadataRepository
import com.data.repository.SchoolsRepository
import com.data.repository.StudentsRepository
import com.data.repository.impl.AssessmentsRepositoryImpl
import com.data.repository.impl.CycleDetailsRepositoryImpl
import com.data.repository.impl.MetadataRepositoryImpl
import com.data.repository.impl.SchoolRepositoryImpl
import com.data.repository.impl.StudentsRepositoryImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideStudentsRepository(
        service: StudentsService,
        studentsDao: StudentsDao,
        studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao,
        submissionsDao: AssessmentSubmissionsDao
    ): StudentsRepository {
        return StudentsRepositoryImpl(
            service,
            studentsDao,
            studentsAssessmentHistoryDao,
            submissionsDao
        )
    }

    @Singleton
    @Provides
    fun provideMetadataRepository(
        service: MetadataService,
        metadataDao: MetadataDao
    ): MetadataRepository {
        return MetadataRepositoryImpl(service, metadataDao)
    }

    @Singleton
    @Provides
    fun provideCycleDetailsRepository(
        cycleDetailsDao: CycleDetailsDao
    ): CycleDetailsRepository {
        return CycleDetailsRepositoryImpl(cycleDetailsDao)
    }

    @Singleton
    @Provides
    fun provideAssessmentsRepository(
        assessmentService: AssessmentService,
        assessmentStateDao: AssessmentStateDao,
        assessmentSubmissionDao: AssessmentSubmissionsDao,
        studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao,
        teacherPerformanceInsightsDao: TeacherPerformanceInsightsDao,
        examinerPerformanceInsightsDao: ExaminerPerformanceInsightsDao,
        historyDao: AssessmentSchoolHistoryDao,
        studentsDao: StudentsDao,
        gson: Gson,
        preferences: SharedPreferences
    ): AssessmentsRepository {
        return AssessmentsRepositoryImpl(
            assessmentService,
            assessmentStateDao,
            assessmentSubmissionDao,
            studentsAssessmentHistoryDao,
            teacherPerformanceInsightsDao,
            examinerPerformanceInsightsDao,
            historyDao,
            studentsDao,
            gson,
            preferences
        )
    }

    @Singleton
    @Provides
    fun provideSchoolsRepository(
        schoolStatusHistoryDao: SchoolsStatusHistoryDao,
        schoolsDao: SchoolsDao,
        assessmentService: AssessmentService,
        schoolService: SchoolService,
        studentsAssessmentHistoryDao: StudentsAssessmentHistoryDao,
        examinerPerformanceInsightsDao: ExaminerPerformanceInsightsDao,
        schoolSubmissionsDao: SchoolSubmissionsDao,
        cycleDetailsDao: CycleDetailsDao
    ): SchoolsRepository {
        return SchoolRepositoryImpl(
            schoolStatusHistoryDao = schoolStatusHistoryDao,
            schoolsDao = schoolsDao,
            assessmentService = assessmentService,
            schoolService = schoolService,
            studentsAssessmentHistoryDao = studentsAssessmentHistoryDao,
            examinerPerformanceInsightsDao = examinerPerformanceInsightsDao,
            schoolSubmissionsDao = schoolSubmissionsDao,
            cycleDetailsDao = cycleDetailsDao
        )
    }

}
