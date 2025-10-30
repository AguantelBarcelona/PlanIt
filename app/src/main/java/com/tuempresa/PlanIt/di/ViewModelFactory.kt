package com.tuempresa.PlanIt.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuempresa.PlanIt.data.preferences.UserPreferencesRepository
import com.tuempresa.PlanIt.data.repository.AuthRepository
import com.tuempresa.PlanIt.data.repository.TaskRepository
import com.tuempresa.PlanIt.ui.viewmodel.AuthViewModel
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, taskRepository, userPreferencesRepository) as T
            }
            modelClass.isAssignableFrom(TaskViewModel::class.java) -> {
                TaskViewModel(taskRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}