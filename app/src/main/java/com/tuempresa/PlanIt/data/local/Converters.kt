package com.tuempresa.PlanIt.data.local

import androidx.room.TypeConverter
import com.tuempresa.PlanIt.data.local.entities.TaskPriority

class Converters {
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority {
        return TaskPriority.valueOf(priority)
    }
}