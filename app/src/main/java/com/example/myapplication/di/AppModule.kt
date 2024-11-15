package com.example.myapplication.di

import android.app.AlarmManager
import android.content.Context
import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.repo.LocalTaskRepository
import com.example.myapplication.data.repo.TaskRepository
import com.example.myapplication.utils.AlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return LocalTaskRepository(taskDao)
    }


    @Provides
    @Singleton
    fun provideAlarmManager(
        @ApplicationContext context: Context
    ): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context,
        alarmManager: AlarmManager
    ): AlarmScheduler {
        return AlarmScheduler(context = context, alarmManager = alarmManager)
    }
}