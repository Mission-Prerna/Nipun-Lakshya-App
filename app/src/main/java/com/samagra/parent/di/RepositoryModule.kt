package com.samagra.parent.di

import com.data.db.dao.ExaminerPerformanceInsightsDao
import com.samagra.parent.network.TeacherInsightsService
import com.samagra.parent.repository.TeacherPerformanceInsightsRepository
import com.samagra.parent.repository.impl.TeacherPerformanceInsightsRepositoryImpl
import com.samagra.parent.ui.DataSyncRepository
import com.data.db.dao.TeacherPerformanceInsightsDao
import com.data.repository.CycleDetailsRepository
import com.samagra.parent.network.ExaminerInsightsService
import com.samagra.parent.repository.ExaminerPerformanceInsightsRepository
import com.samagra.parent.repository.impl.ExaminerPerformanceInsightsRepositoryImpl
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
    fun provideTeacherInsightsRepository(
        service: TeacherInsightsService,
        dao: TeacherPerformanceInsightsDao
    ): TeacherPerformanceInsightsRepository {
        return TeacherPerformanceInsightsRepositoryImpl(
            service,
            dao
        )
    }
    @Singleton
    @Provides
    fun provideExaminerInsightsRepository(
        service: ExaminerInsightsService,
        dao: ExaminerPerformanceInsightsDao,
        cycleDetailsRepo: CycleDetailsRepository
    ): ExaminerPerformanceInsightsRepository {
        return ExaminerPerformanceInsightsRepositoryImpl(
            service,
            dao,
            cycleDetailsRepo
        )
    }

    @Singleton
    @Provides
    fun provideDataSyncRepository(): DataSyncRepository {
        return DataSyncRepository()
    }

}
