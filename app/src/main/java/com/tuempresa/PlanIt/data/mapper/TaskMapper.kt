package com.tuempresa.PlanIt.data.mapper

import com.tuempresa.PlanIt.data.local.entities.TaskEntity
import com.tuempresa.PlanIt.domain.models.Task

fun TaskEntity.toDomain(): Task {
    return Task(
        id = this.id,
        userId = this.userId,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        isCompleted = this.isCompleted,
        priority = this.priority,
        subtasks = this.subtasks,
        photoUri = this.photoUri,
        audioUri = this.audioUri,
        locationLat = this.locationLat,
        locationLong = this.locationLong
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = this.id,
        userId = this.userId,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        isCompleted = this.isCompleted,
        priority = this.priority,
        subtasks = this.subtasks,
        photoUri = this.photoUri,
        audioUri = this.audioUri,
        locationLat = this.locationLat,
        locationLong = this.locationLong
    )
}