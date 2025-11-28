package com.tuempresa.PlanIt.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tuempresa.PlanIt.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    // Cuando se concedan los permisos, se obtendrá el clima.
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.fetchWeatherForCurrentLocation()
        }
    }

    val weatherState by viewModel.weatherState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (locationPermissions.allPermissionsGranted) {
            when {
                weatherState.isLoading -> {
                    CircularProgressIndicator()
                }
                weatherState.error != null -> {
                    Text(
                        text = "Error: ${weatherState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                weatherState.weatherData != null -> {
                    val weather = weatherState.weatherData!!
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = weather.location, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "${weather.temperature}°C", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = weather.description, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                else -> {
                    // Si se han concedido los permisos, pero se está esperando a que se carguen los datos del tiempo, se muestra un cargador.
                    CircularProgressIndicator()
                }
            }
        } else {
            // No se han concedido los permisos. Muestra un mensaje.
            Text(
                text = "Se necesitan permisos de ubicación para mostrar el clima. Por favor, actívelos en la configuración de la aplicación.",
                textAlign = TextAlign.Center
            )
        }
    }
}
