package com.example.myapplication.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.entities.TaskEntity
import com.example.myapplication.data.local.type_converters.CompletionHistoryConverter
import com.example.myapplication.data.local.type_converters.Converters

@Database(
    entities = [TaskEntity::class],
    version = 5,
)
@TypeConverters(Converters::class,CompletionHistoryConverter::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}