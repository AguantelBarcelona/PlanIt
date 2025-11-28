package com.tuempresa.PlanIt.domain.repository

import com.tuempresa.PlanIt.domain.models.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(userId: Int): Flow<List<Task>>
    fun getTaskById(taskId: Int): Flow<Task?>
    fun searchTasks(userId: Int, query: String): Flow<List<Task>>
    fun getTotalTaskCount(userId: Int): Flow<Int>
    fun getCompletedTaskCount(userId: Int): Flow<Int>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteAllTasks(userId: Int)
    suspend fun toggleTaskCompletion(task: Task)
}