package com.tuempresa.PlanIt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tuempresa.PlanIt.data.local.converters.SubtaskConverter
import com.tuempresa.PlanIt.domain.models.Subtask

@Entity(tableName = "tasks")
@TypeConverters(SubtaskConverter::class)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
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

enum class TaskPriority(val displayName: String) {
    LOW("Baja"),
    NORMAL("Normal"),
    HIGH("Alta"),
    URGENT("Urgente")
}
