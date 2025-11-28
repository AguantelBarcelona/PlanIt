package com.tuempresa.PlanIt.data.repository

import com.tuempresa.PlanIt.domain.models.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(userId: Int): Flow<List<Task>>
    fun getTaskById(id: Int): Flow<Task?>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun toggleTaskCompletion(task: Task)
    suspend fun deleteAllTasks(userId: Int)
    fun searchTasks(userId: Int, query: String): Flow<List<Task>>
    fun getTotalTaskCount(userId: Int): Flow<Int>
    fun getCompletedTaskCount(userId: Int): Flow<Int>
}