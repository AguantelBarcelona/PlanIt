package com.tuempresa.PlanIt.data.repository

import com.tuempresa.PlanIt.data.network.WeatherApiClient
import com.tuempresa.PlanIt.data.network.WeatherResponse
import com.tuempresa.PlanIt.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherApiClient: WeatherApiClient
) : WeatherRepository {
    override suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return try {
            val response = weatherApiClient.getWeather(latitude, longitude)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
