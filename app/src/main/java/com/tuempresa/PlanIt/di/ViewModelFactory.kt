package com.tuempresa.PlanIt.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuempresa.PlanIt.data.preferences.UserPreferencesRepository
import com.tuempresa.PlanIt.domain.repository.AuthRepository
import com.tuempresa.PlanIt.domain.repository.TaskRepository
import com.tuempresa.PlanIt.domain.use_case.GetWeatherUseCase
import com.tuempresa.PlanIt.ui.viewmodel.AuthViewModel
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel
import com.tuempresa.PlanIt.ui.viewmodel.WeatherViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, taskRepository, userPreferencesRepository) as T
            }
            modelClass.isAssignableFrom(TaskViewModel::class.java) -> {
                TaskViewModel(taskRepository, getWeatherUseCase) as T
            }
            modelClass.isAssignableFrom(WeatherViewModel::class.java) -> {
                WeatherViewModel(getWeatherUseCase, application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}