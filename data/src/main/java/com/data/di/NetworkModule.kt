package com.data.di

import com.data.network.AssessmentService
import com.data.network.MetadataService
import com.data.network.SchoolService
import com.data.network.StudentsService
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.commons.utils.CommonConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideStudentsService(): StudentsService {
        return Network.getClient(
            ClientType.RETROFIT,
            StudentsService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )!!
    }

    @Provides
    @Singleton
    fun provideMetadataService(): MetadataService {
        return Network.getClient(
            ClientType.RETROFIT,
            MetadataService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )!!
    }

    @Provides
    @Singleton
    fun provideAssessmentsService(): AssessmentService {
        return Network.getClient(
            ClientType.RETROFIT,
            AssessmentService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )!!
    }

    @Provides
    @Singleton
    fun provideSchoolService(): SchoolService {
        return Network.getClient(
            ClientType.RETROFIT,
            SchoolService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )!!
    }
}
