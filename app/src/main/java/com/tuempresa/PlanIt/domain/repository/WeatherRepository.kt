package com.tuempresa.PlanIt.domain.repository

import com.tuempresa.PlanIt.data.network.WeatherResponse

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherResponse>
}