package com.tuempresa.PlanIt.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tuempresa.PlanIt.domain.models.Subtask

class SubtaskConverter {
    @TypeConverter
    fun fromSubtaskList(subtasks: List<Subtask>): String {
        return Gson().toJson(subtasks)
    }

    @TypeConverter
    fun toSubtaskList(subtasksJson: String): List<Subtask> {
        val typeToken = object : TypeToken<List<Subtask>>() {}.type
        return Gson().fromJson(subtasksJson, typeToken)
    }
}
