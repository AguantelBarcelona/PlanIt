package com.tuempresa.PlanIt.domain.use_case

import com.tuempresa.PlanIt.data.network.WeatherResponse
import com.tuempresa.PlanIt.domain.repository.WeatherRepository

class GetWeatherUseCase(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return repository.getWeather(latitude, longitude)
    }
}
