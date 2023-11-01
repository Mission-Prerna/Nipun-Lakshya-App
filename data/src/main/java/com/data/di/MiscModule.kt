package com.data.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * All dependencies which cannot be classified into the categories of
 *  Storage, Repository are provided by this module.
 */
@Module
@InstallIn(SingletonComponent::class)
object MiscModule {

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}
