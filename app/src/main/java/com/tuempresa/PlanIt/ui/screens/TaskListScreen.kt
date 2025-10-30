package com.tuempresa.PlanIt.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tuempresa.PlanIt.navigation.Routes
import com.tuempresa.PlanIt.ui.components.TaskItem
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel.TaskUiState

/**
 * Pantalla principal que muestra la lista de tareas.
 * 
 * Cumple requisitos:
 * - Interfaz visual organizada con navegación clara
 * - Animaciones funcionales (items de lista)
 * - Gestión de estado con ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    userId: Int
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isSpeedDialOpen by remember { mutableStateOf(false) }
    
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TaskUiState.Loading -> {
                    // Indicador de progreso
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is TaskUiState.Empty -> {
                    // Estado vacío
                    Column(
                        modifier = Modifier.align(Alignment.Center),
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
                
                is TaskUiState.Success -> {
                    val (completed, pending) = state.tasks.partition { it.isCompleted }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Pending Tasks
                        Text("Tareas Pendientes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(pending, key = { it.id }) { task ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically(),
                                    modifier = Modifier.animateItemPlacement()
                                ) {
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

                        // Completed Tasks
                        if (completed.isNotEmpty()) {
                            Text("Tareas Completadas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(completed, key = { it.id }) { task ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + slideInVertically(),
                                        exit = fadeOut() + slideOutVertically(),
                                        modifier = Modifier.animateItemPlacement()
                                    ) {
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
                }
                
                is TaskUiState.Error -> {
                    // Estado de error
                    Column(
                        modifier = Modifier.align(Alignment.Center),
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
                else -> {}
            }
        }
    }
    
    // Diálogo de confirmación para eliminar todas las tareas
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Eliminar todas las tareas") },
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Aquí se llamaría a BiometricPrompt antes de ejecutar
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