package com.tuempresa.PlanIt.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.tuempresa.PlanIt.data.local.entities.TaskPriority
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TaskEditScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    taskId: Int?,
    userId: Int
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Determine if the form should be read-only
    val isPastDueDate = formState.dueDate?.let { it < System.currentTimeMillis() - 86400000 } ?: false
    val isReadOnly = formState.isCompleted || isPastDueDate

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    // Gallery permission state
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val galleryPermissionState = rememberPermissionState(galleryPermission)

    // Get content from gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onPhotoCapture(it.toString()) }
        }
    )

    // Take a picture with the camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // The URI is already saved in the ViewModel, just show confirmation
                viewModel.onPhotoCapture(formState.photoUri ?: "")
            }
        }
    )

    LaunchedEffect(Unit) {
        if (taskId != null && taskId != 0) {
            viewModel.loadTaskForEdit(taskId)
        } else {
            viewModel.clearForm()
        }

        viewModel.events.collectLatest { event ->
            when (event) {
                is TaskViewModel.UiEvent.NavigateBack -> navController.popBackStack()
                is TaskViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    if (showDatePicker && !isReadOnly) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDueDateChange(it)
                    }
                    showDatePicker = false 
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Elige una opción") },
            text = { Text("¿Desde dónde quieres seleccionar la imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        val uri = createImageUri(context)
                        viewModel.onPhotoCapture(uri.toString())
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                    showImageDialog = false
                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (galleryPermissionState.status.isGranted) {
                        galleryLauncher.launch("image/*")
                    } else {
                        galleryPermissionState.launchPermissionRequest()
                    }
                    showImageDialog = false
                }) {
                    Text("Galería")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null || taskId == 0) "Nueva Tarea" else if (isReadOnly) "Ver Tarea" else "Editar Tarea") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (!isReadOnly) {
                        IconButton(onClick = { viewModel.saveTask(userId) }) {
                            Icon(Icons.Default.Done, contentDescription = "Guardar")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item { 
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.titleError != null,
                    readOnly = isReadOnly
                )
                formState.titleError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.descriptionError != null,
                    readOnly = isReadOnly
                )
                formState.descriptionError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                OutlinedTextField(
                    value = formState.dueDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text("Fecha de Vencimiento") },
                    readOnly = true, // Always read-only, click to open picker
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isReadOnly) { showDatePicker = true },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha",
                            modifier = Modifier.clickable(enabled = !isReadOnly) { showDatePicker = true }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Text("Prioridad", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    TaskPriority.values().asList().chunked(2).forEach { rowPriorities ->
                        Row(Modifier.fillMaxWidth()) {
                            rowPriorities.forEach { priority ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = !isReadOnly) { viewModel.onPriorityChange(priority) }
                                ) {
                                    RadioButton(
                                        selected = formState.priority == priority,
                                        onClick = { viewModel.onPriorityChange(priority) },
                                        enabled = !isReadOnly
                                    )
                                    Text(priority.displayName, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
            }
            
            // Sub-tasks section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sub-tareas", style = MaterialTheme.typography.titleMedium)
            }
            itemsIndexed(formState.subtasks) { index, subtask ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = subtask.isCompleted, 
                        onCheckedChange = { isChecked -> viewModel.onSubtaskCheckedChange(index, isChecked) }, 
                        enabled = !isReadOnly
                    )
                    OutlinedTextField(
                        value = subtask.title, 
                        onValueChange = { newTitle -> viewModel.onSubtaskChange(index, newTitle) }, 
                        modifier = Modifier.weight(1f), 
                        readOnly = isReadOnly
                    )
                    IconButton(onClick = { viewModel.removeSubtask(index) }, enabled = !isReadOnly) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Sub-tarea")
                    }
                }
            }
            item {
                Button(onClick = { viewModel.addSubtask() }, enabled = !isReadOnly) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir sub-tarea")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
            }

            // Attachments section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Adjuntos", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { showImageDialog = true }, enabled = !isReadOnly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Cámara o Galería")
                            Text("Imagen", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = { /* TODO: Record Audio */ }, enabled = !isReadOnly) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Mic, contentDescription = "Grabar Audio")
                            Text("Audio", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = { /* TODO: Attach File */ }, enabled = !isReadOnly) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar Archivo")
                            Text("Archivo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

private fun createImageUri(context: android.content.Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    val image = File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",         /* suffix */
        storageDir      /* directory */
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        image
    )
}