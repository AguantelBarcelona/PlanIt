package com.tuempresa.PlanIt.domain.models

import com.tuempresa.PlanIt.data.local.entities.TaskPriority

data class Task(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val subtasks: List<Subtask> = emptyList(),
    val photoUri: String? = null,
    val audioUri: String? = null,
    val attachmentUri: String? = null,
    val locationLat: Double? = null,
    val locationLong: Double? = null,
    val locationName: String? = null
)
