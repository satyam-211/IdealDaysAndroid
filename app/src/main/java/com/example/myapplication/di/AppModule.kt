package com.example.myapplication.di

import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.repo.LocalTaskRepository
import com.example.myapplication.data.repo.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao) : TaskRepository {
        return LocalTaskRepository(taskDao)
    }
}