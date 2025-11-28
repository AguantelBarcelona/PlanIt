package com.tuempresa.PlanIt.data.repository

import com.tuempresa.PlanIt.data.local.dao.TaskDao
import com.tuempresa.PlanIt.data.mapper.toDomain
import com.tuempresa.PlanIt.data.mapper.toEntity
import com.tuempresa.PlanIt.domain.models.Task
import com.tuempresa.PlanIt.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(userId: Int): Flow<List<Task>> {
        return taskDao.getAllTasks(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTaskById(taskId: Int): Flow<Task?> {
        return taskDao.getTaskById(taskId).map { it?.toDomain() }
    }

    override fun searchTasks(userId: Int, query: String): Flow<List<Task>> {
        return taskDao.searchTasks(userId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalTaskCount(userId: Int): Flow<Int> = taskDao.getTotalTaskCount(userId)

    override fun getCompletedTaskCount(userId: Int): Flow<Int> = taskDao.getCompletedTaskCount(userId)

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun deleteAllTasks(userId: Int) {
        taskDao.deleteAllTasks(userId)
    }

    override suspend fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updatedTask.toEntity())
    }
}