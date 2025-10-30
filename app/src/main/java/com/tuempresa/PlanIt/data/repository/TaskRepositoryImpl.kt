package com.tuempresa.PlanIt.data.repository

import com.tuempresa.PlanIt.data.local.dao.TaskDao
import com.tuempresa.PlanIt.data.local.entities.TaskEntity
import com.tuempresa.PlanIt.domain.models.Task
import com.tuempresa.PlanIt.domain.models.Subtask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {

    override fun getAllTasks(userId: Int): Flow<List<Task>> {
        return taskDao.getAllTasks(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTaskById(id: Int): Flow<Task?> {
        return taskDao.getTaskById(id).map { it?.toDomain() }
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updatedTask.toEntity())
    }

    override suspend fun deleteAllTasks(userId: Int) {
        taskDao.deleteAllTasks(userId)
    }

    override fun searchTasks(userId: Int, query: String): Flow<List<Task>> {
        return taskDao.searchTasks(userId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalTaskCount(userId: Int): Flow<Int> {
        return taskDao.getTotalTaskCount(userId)
    }

    override fun getCompletedTaskCount(userId: Int): Flow<Int> {
        return taskDao.getCompletedTaskCount(userId)
    }

    private fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            userId = userId,
            title = title,
            description = description,
            createdAt = createdAt,
            dueDate = dueDate,
            isCompleted = isCompleted,
            priority = priority,
            subtasks = subtasks ?: emptyList(),
            photoUri = photoUri,
            audioUri = audioUri,
            attachmentUri = attachmentUri,
            locationLat = locationLat,
            locationLong = locationLong,
            locationName = locationName
        )
    }

    private fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            userId = userId,
            title = title,
            description = description,
            createdAt = createdAt,
            dueDate = dueDate,
            isCompleted = isCompleted,
            priority = priority,
            subtasks = subtasks,
            photoUri = photoUri,
            audioUri = audioUri,
            attachmentUri = attachmentUri,
            locationLat = locationLat,
            locationLong = locationLong,
            locationName = locationName
        )
    }
}