package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entities.TaskEntity
import kotlinx.datetime.LocalDate
import java.util.UUID

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: UUID): TaskEntity?

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: UUID)

    @Query("""
        SELECT * FROM tasks 
        WHERE DATE(scheduledDate) = DATE(:date)
    """)
    suspend fun getTasksForDay(date: LocalDate): List<TaskEntity>

    @Query("""
    SELECT * 
    FROM tasks
    WHERE (
            -- Condition for Binary Tasks
            (taskType = 0 AND isCompleted = 0 AND scheduledDate BETWEEN DATE(:startDate) AND  DATE(:endDate))
            OR
            -- Condition for Partial Tasks (fetch all partial tasks within date range)
            (taskType = 1 AND scheduledDate BETWEEN DATE(:startDate) AND DATE(:endDate)) 
        )
    
    ORDER BY scheduledDate DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getIncompleteTasks(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
        offset: Int
    ): List<TaskEntity>

    @Query("""
        UPDATE tasks 
        SET isCompleted = CASE WHEN isCompleted = 1 THEN 0 ELSE 1 END
        WHERE id = :taskId
    """)
    suspend fun toggleBinaryTask(taskId: UUID)

    @Query("""
        UPDATE tasks 
        SET scheduledDate = :currentDate
        WHERE id = :taskId
    """)
    suspend fun updateTaskDate(taskId: UUID, currentDate: LocalDate)


    @Query("""
        UPDATE tasks 
        SET completionHistoryJson = :completionHistory
        WHERE id = :taskId
    """)
    suspend fun updatePartialTaskCompletion(
        taskId: UUID,
        completionHistory: String,
    )
    @Query("""
        SELECT * FROM tasks
        WHERE DATE(scheduledDate) BETWEEN DATE(:startDate) AND DATE(:endDate)
        
        ORDER BY scheduledDate DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getTasksForDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
        offset: Int = 0
    ): List<TaskEntity>

}