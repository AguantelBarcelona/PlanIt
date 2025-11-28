package com.tuempresa.PlanIt.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.tuempresa.PlanIt.data.network.WeatherResponse
import com.tuempresa.PlanIt.navigation.Routes
import com.tuempresa.PlanIt.ui.components.TaskItem
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel.TaskUiState
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel.WeatherUiState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    userId: Int
) {
    val uiState by viewModel.uiState.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isSpeedDialOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION)

    LaunchedEffect(locationPermissionState) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            locationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.fetchWeather(location.latitude, location.longitude)
                }
            }
        }
    }

    LaunchedEffect(userId) {
        viewModel.loadTasks(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                viewModel.searchTasks(userId, it)
                            },
                            placeholder = { Text("Buscar tareas...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    } else {
                        Text("Mis Tareas")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearching = !isSearching
                        if (!isSearching) {
                            searchQuery = ""
                            viewModel.loadTasks(userId)
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Cerrar búsqueda" else "Buscar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Tareas") },
                    label = { Text("Tareas") },
                    selected = true,
                    onClick = { /* Ya estamos aquí */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendario") },
                    label = { Text("Calendario") },
                    selected = false,
                    onClick = { navController.navigate(Routes.Calendar.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate(Routes.Profile.route) }
                )
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
            ) {
                if (isSpeedDialOpen) {
                    FloatingActionButton(
                        onClick = { 
                            navController.navigate(Routes.TaskEdit.route)
                            isSpeedDialOpen = false 
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir tarea")
                    }
                    FloatingActionButton(
                        onClick = { 
                            showDeleteAllDialog = true
                            isSpeedDialOpen = false
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Eliminar todo")
                    }
                }
                FloatingActionButton(
                    onClick = { isSpeedDialOpen = !isSpeedDialOpen },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isSpeedDialOpen) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Abrir menú de acciones"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WeatherInfoCard(weatherState = weatherState)

            when (val state = uiState) {
                is TaskUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                is TaskUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay tareas",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Toca + para crear una nueva",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                is TaskUiState.Success -> {
                    val (completed, pending) = state.tasks.partition { it.isCompleted }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (pending.isNotEmpty()) {
                            item {
                                Text("Tareas Pendientes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                            }
                            items(pending, key = { it.id }) { task ->
                                Box(modifier = Modifier.animateItemPlacement()) {
                                    TaskItem(
                                        task = task,
                                        onTaskClick = { 
                                            val isPastDueDate = task.dueDate?.let { it < System.currentTimeMillis() - 86400000 } ?: false
                                            if (!task.isCompleted && !isPastDueDate) {
                                                navController.navigate("${Routes.TaskEdit.route}?taskId=${task.id}")
                                            }
                                        },
                                        onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                                        onDeleteTask = { viewModel.deleteTask(it) }
                                    )
                                }
                            }
                        }

                        if (completed.isNotEmpty()) {
                            item {
                                Text("Tareas Completadas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                            }
                            items(completed, key = { it.id }) { task ->
                                Box(modifier = Modifier.animateItemPlacement()) {
                                    TaskItem(
                                        task = task,
                                        onTaskClick = { 
                                            val isPastDueDate = task.dueDate?.let { it < System.currentTimeMillis() - 86400000 } ?: false
                                            if (!task.isCompleted && !isPastDueDate) {
                                                navController.navigate("${Routes.TaskEdit.route}?taskId=${task.id}")
                                            }
                                        },
                                        onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                                        onDeleteTask = { viewModel.deleteTask(it) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                is TaskUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error al cargar tareas",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadTasks(userId) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
    
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Eliminar todas las tareas") },
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllTasks(userId)
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun WeatherInfoCard(weatherState: WeatherUiState) {
    when (weatherState) {
        is WeatherUiState.Success -> {
            WeatherSuccessContent(weatherState.weather)
        }
        is WeatherUiState.Error -> {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Error al cargar el tiempo: ${weatherState.message}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is WeatherUiState.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cargando el tiempo...")
            }
        }
        is WeatherUiState.Idle -> {}
    }
}

@Composable
private fun WeatherSuccessContent(weather: WeatherResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = weather.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${weather.main.temp.toInt()}°C",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${weather.weather.firstOrNull()?.icon}@2x.png",
                contentDescription = "Icono del tiempo",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}