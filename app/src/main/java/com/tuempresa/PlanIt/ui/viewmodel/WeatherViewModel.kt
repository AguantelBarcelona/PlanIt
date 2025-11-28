package com.tuempresa.PlanIt.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.tuempresa.PlanIt.domain.use_case.GetWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherData(
    val location: String,
    val temperature: Double,
    val description: String,
)

data class WeatherState(
    val isLoading: Boolean = false,
    val weatherData: WeatherData? = null,
    val error: String? = null
)

class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val application: Application
) : ViewModel() {

    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState = _weatherState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    fun fetchWeatherForCurrentLocation() {
        _weatherState.value = WeatherState(isLoading = true)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModelScope.launch {
                    getWeatherUseCase(location.latitude, location.longitude).onSuccess {
                        val weatherData = WeatherData(
                            location = it.name,
                            temperature = it.main.temp,
                            description = it.weather.firstOrNull()?.description ?: ""
                        )
                        _weatherState.value = WeatherState(weatherData = weatherData)
                    }.onFailure {
                        _weatherState.value = WeatherState(error = it.message)
                    }
                }
            } else {
                _weatherState.value = WeatherState(error = "No se pudo obtener la ubicación.")
            }
        }.addOnFailureListener {
            _weatherState.value = WeatherState(error = "Error al obtener la ubicación: ${it.message}")
        }
    }
}
