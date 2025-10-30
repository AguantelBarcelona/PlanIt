package com.tuempresa.PlanIt.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuempresa.PlanIt.data.local.entities.TaskPriority
import com.tuempresa.PlanIt.data.repository.TaskRepository
import com.tuempresa.PlanIt.domain.models.Subtask
import com.tuempresa.PlanIt.domain.models.Task
import com.tuempresa.PlanIt.util.validators.TaskValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel principal para gestión de tareas.
 * Maneja estado con StateFlow (para UI) y SharedFlow (para eventos únicos).
 * Centraliza toda la lógica de negocio y validaciones.
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {
    
    // === ESTADOS (StateFlow) ===
    
    // Estado de la lista de tareas
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    // Estado del formulario (crear/editar)
    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()
    
    // Estado de carga para operaciones
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // === EVENTOS ÚNICOS (SharedFlow) ===
    
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    // === FUNCIONES PÚBLICAS ===
    
    /**
     * Carga todas las tareas desde el repositorio
     */
    fun loadTasks(userId: Int) {
        viewModelScope.launch {
            repository.getAllTasks(userId)
                .catch { exception ->
                    _uiState.value = TaskUiState.Error(
                        exception.message ?: "Error al cargar tareas"
                    )
                }
                .collect { tasks ->
                    val updatedTasks = tasks.map { task ->
                        if (!task.isCompleted && task.dueDate != null && task.dueDate!! < Calendar.getInstance().timeInMillis) {
                            task.copy(isCompleted = true)
                        } else {
                            task
                        }
                    }
                    _uiState.value = if (updatedTasks.isEmpty()) {
                        TaskUiState.Empty
                    } else {
                        TaskUiState.Success(updatedTasks)
                    }
                }
        }
    }
    
    /**
     * Actualiza el título en el formulario
     */
    fun onTitleChange(title: String) {
        _formState.value = _formState.value.copy(
            title = title,
            titleError = null // Limpia error al escribir
        )
    }
    
    /**
     * Actualiza la descripción en el formulario
     */
    fun onDescriptionChange(description: String) {
        _formState.value = _formState.value.copy(
            description = description,
            descriptionError = null
        )
    }
    
    /**
     * Actualiza la fecha de vencimiento
     */
    fun onDueDateChange(dueDate: Long?) {
        _formState.value = _formState.value.copy(
            dueDate = dueDate,
            dueDateError = null
        )
    }
    
    /**
     * Actualiza la prioridad
     */
    fun onPriorityChange(priority: TaskPriority) {
        _formState.value = _formState.value.copy(priority = priority)
    }
    
    /**
     * Guarda la URI de la foto capturada
     */
    fun onPhotoCapture(photoUri: String) {
        _formState.value = _formState.value.copy(photoUri = photoUri)
        viewModelScope.launch {
            _events.emit(UiEvent.ShowSnackbar("Foto adjuntada correctamente"))
        }
    }
    
    /**
     * Guarda la URI del audio grabado
     */
    fun onAudioRecorded(audioUri: String) {
        _formState.value = _formState.value.copy(audioUri = audioUri)
        viewModelScope.launch {
            _events.emit(UiEvent.ShowSnackbar("Audio adjuntado correctamente"))
        }
    }

    /**
     * Añade una nueva subtarea en blanco
     */
    fun addSubtask() {
        val currentSubtasks = _formState.value.subtasks.toMutableList()
        currentSubtasks.add(Subtask(""))
        _formState.value = _formState.value.copy(subtasks = currentSubtasks)
    }

    /**
     * Actualiza el texto de una subtarea
     */
    fun onSubtaskChange(index: Int, newTitle: String) {
        val currentSubtasks = _formState.value.subtasks.toMutableList()
        if (index >= 0 && index < currentSubtasks.size) {
            currentSubtasks[index] = currentSubtasks[index].copy(title = newTitle)
            _formState.value = _formState.value.copy(subtasks = currentSubtasks)
        }
    }

    /**
     * Cambia el estado de completado de una subtarea
     */
    fun onSubtaskCheckedChange(index: Int, isChecked: Boolean) {
        val currentSubtasks = _formState.value.subtasks.toMutableList()
        if (index >= 0 && index < currentSubtasks.size) {
            currentSubtasks[index] = currentSubtasks[index].copy(isCompleted = isChecked)
            _formState.value = _formState.value.copy(subtasks = currentSubtasks)
        }
    }

    /**
     * Elimina una subtarea
     */
    fun removeSubtask(index: Int) {
        val currentSubtasks = _formState.value.subtasks.toMutableList()
        if (index >= 0 && index < currentSubtasks.size) {
            currentSubtasks.removeAt(index)
            _formState.value = _formState.value.copy(subtasks = currentSubtasks)
        }
    }
    
    /**
     * Valida y guarda la tarea
     */
    fun saveTask(userId: Int) {
        val currentForm = _formState.value
        
        // Ejecutar validaciones
        val validationResults = TaskValidator.validateTask(
            title = currentForm.title,
            description = currentForm.description,
            dueDate = currentForm.dueDate
        )
        
        // Actualizar errores en el formulario
        _formState.value = currentForm.copy(
            titleError = validationResults["title"]?.errorMessage,
            descriptionError = validationResults["description"]?.errorMessage,
            dueDateError = validationResults["dueDate"]?.errorMessage
        )
        
        // Si hay errores, no continuar
        if (!TaskValidator.isFormValid(validationResults)) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowSnackbar("Por favor, corrige los errores"))
            }
            return
        }
        
        // Guardar tarea
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val task = Task(
                    id = currentForm.taskId ?: 0,
                    userId = userId,
                    title = currentForm.title,
                    description = currentForm.description,
                    dueDate = currentForm.dueDate,
                    isCompleted = currentForm.isCompleted,
                    priority = currentForm.priority,
                    subtasks = currentForm.subtasks,
                    photoUri = currentForm.photoUri,
                    audioUri = currentForm.audioUri,
                    locationLat = currentForm.locationLat,
                    locationLong = currentForm.locationLong
                )
                
                if (currentForm.taskId == null) {
                    repository.insertTask(task)
                    _events.emit(UiEvent.ShowSnackbar("Tarea creada exitosamente"))
                } else {
                    repository.updateTask(task)
                    _events.emit(UiEvent.ShowSnackbar("Tarea actualizada exitosamente"))
                }
                
                // Limpiar formulario y navegar atrás
                clearForm()
                _events.emit(UiEvent.NavigateBack)
                
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowSnackbar("Error al guardar: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga una tarea para editar
     */
    fun loadTaskForEdit(taskId: Int) {
        viewModelScope.launch {
            repository.getTaskById(taskId).firstOrNull()?.let { task ->
                _formState.value = FormState(
                    taskId = task.id,
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    isCompleted = task.isCompleted,
                    priority = task.priority,
                    subtasks = task.subtasks,
                    photoUri = task.photoUri,
                    audioUri = task.audioUri,
                    locationLat = task.locationLat,
                    locationLong = task.locationLong
                )
            }
        }
    }
    
    /**
     * Marca/desmarca una tarea como completada
     */
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            _events.emit(
                UiEvent.ShowSnackbar(
                    if (task.isCompleted) "Tarea reactivada" else "Tarea completada"
                )
            )
        }
    }
    
    /**
     * Elimina una tarea
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _events.emit(UiEvent.ShowSnackbar("Tarea eliminada"))
        }
    }
    
    /**
     * Elimina todas las tareas (requiere autenticación biométrica)
     */
    fun deleteAllTasks(userId: Int) {
        viewModelScope.launch {
            repository.deleteAllTasks(userId)
            _events.emit(UiEvent.ShowSnackbar("Todas las tareas eliminadas"))
        }
    }
    
    /**
     * Busca tareas por query
     */
    fun searchTasks(userId: Int, query: String) {
        viewModelScope.launch {
            repository.searchTasks(userId, query)
                .collect { tasks ->
                    _uiState.value = if (tasks.isEmpty()) {
                        TaskUiState.Empty
                    } else {
                        TaskUiState.Success(tasks)
                    }
                }
        }
    }
    
    /**
     * Limpia el formulario
     */
    fun clearForm() {
        _formState.value = FormState()
    }
    
    // === DATA CLASSES ===
    
    /**
     * Estado de la UI para la lista de tareas
     */
    sealed class TaskUiState {
        object Loading : TaskUiState()
        object Empty : TaskUiState()
        data class Success(val tasks: List<Task>) : TaskUiState()
        data class Error(val message: String) : TaskUiState()
    }
    
    /**
     * Estado del formulario con campos y errores
     */
    data class FormState(
        val taskId: Int? = null,
        val userId: Int? = null,
        val title: String = "",
        val description: String = "",
        val dueDate: Long? = null,
        val isCompleted: Boolean = false,
        val priority: TaskPriority = TaskPriority.NORMAL,
        val subtasks: List<Subtask> = emptyList(),
        val photoUri: String? = null,
        val audioUri: String? = null,
        val locationLat: Double? = null,
        val locationLong: Double? = null,
        
        // Errores de validación
        val titleError: String? = null,
        val descriptionError: String? = null,
        val dueDateError: String? = null
    )
    
    /**
     * Eventos únicos para la UI
     */
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
        data class NavigateToDetail(val taskId: Int) : UiEvent()
    }
}