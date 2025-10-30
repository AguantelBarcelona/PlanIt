package com.tuempresa.PlanIt.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuempresa.PlanIt.data.local.entities.User
import com.tuempresa.PlanIt.data.preferences.UserPreferencesRepository
import com.tuempresa.PlanIt.data.repository.AuthRepository
import com.tuempresa.PlanIt.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean? = null, // null = loading, true = logged in, false = logged out
    val isDarkTheme: Boolean = false
)

data class UserState(
    val id: Int,
    val username: String,
    val displayName: String?,
    val profilePictureUri: String?,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository, // Injected TaskRepository
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private val _userState = MutableStateFlow<UserState?>(null)
    val userState = _userState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        checkUserSession()
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            userPreferencesRepository.isDarkTheme.collect { isDark ->
                _authState.value = _authState.value.copy(isDarkTheme = isDark)
            }
        }
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            val username = userPreferencesRepository.loggedInUser.first()
            if (username != null) {
                loadUserProfile()
            }
            _authState.value = _authState.value.copy(isLoggedIn = username != null)
        }
    }

    fun onLoginSuccess() {
        _authState.value = _authState.value.copy(isLoggedIn = true)
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val username = userPreferencesRepository.loggedInUser.first()
            if (username != null) {
                authRepository.findUserByUsername(username)?.let { user ->
                    val totalTasks = taskRepository.getTotalTaskCount(user.id).first()
                    val completedTasks = taskRepository.getCompletedTaskCount(user.id).first()
                    _userState.value = UserState(
                        id = user.id, 
                        username = user.username, 
                        displayName = user.displayName, 
                        profilePictureUri = user.profilePictureUri,
                        totalTasks = totalTasks,
                        completedTasks = completedTasks
                    )
                }
            }
        }
    }

    fun login(username: String, password: String) { // La comprobación de la contraseña está simplificada por ahora
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _events.emit(UiEvent.ShowSnackbar("Usuario y contraseña no pueden estar vacíos"))
                return@launch
            }
            val user = authRepository.findUserByUsername(username)
            if (user != null) { // En una app real, aquí verificarías el hash de la contraseña
                userPreferencesRepository.saveUserSession(username)
                _events.emit(UiEvent.ShowSnackbar("Inicio de sesión exitoso"))
                _events.emit(UiEvent.LoginSuccess)
                loadUserProfile()
            } else {
                _events.emit(UiEvent.ShowSnackbar("Usuario o contraseña incorrectos"))
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _events.emit(UiEvent.ShowSnackbar("Los campos no pueden estar vacíos"))
                return@launch
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _events.emit(UiEvent.ShowSnackbar("El formato del correo electrónico no es válido"))
                return@launch
            }
            val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$".toRegex()
            if (!password.matches(passwordRegex)) {
                _events.emit(UiEvent.ShowSnackbar("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número"))
                return@launch
            }
            if (password != confirmPassword) {
                _events.emit(UiEvent.ShowSnackbar("Las contraseñas no coinciden"))
                return@launch
            }
            if (authRepository.findUserByUsername(email) != null) {
                _events.emit(UiEvent.ShowSnackbar("El correo electrónico ya está registrado"))
                return@launch
            }
            // En una app real, aquí harías un hash de la contraseña antes de guardarla
            authRepository.createUser(User(username = email, displayName = null))
            _events.emit(UiEvent.ShowSnackbar("Usuario registrado con éxito"))
            _events.emit(UiEvent.NavigateToLogin)
        }
    }

    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            val username = userPreferencesRepository.loggedInUser.first()
            if (username != null) {
                authRepository.findUserByUsername(username)?.let { user ->
                    val updatedUser = user.copy(displayName = displayName)
                    authRepository.updateUser(updatedUser)
                    _userState.value = _userState.value?.copy(displayName = updatedUser.displayName)
                    _events.emit(UiEvent.ShowSnackbar("Nombre guardado con éxito"))
                }
            }
        }
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            val username = userPreferencesRepository.loggedInUser.first()
            if (username != null) {
                authRepository.findUserByUsername(username)?.let { user ->
                    val updatedUser = user.copy(profilePictureUri = uri)
                    authRepository.updateUser(updatedUser)
                    _userState.value = _userState.value?.copy(profilePictureUri = updatedUser.profilePictureUri)
                }
            }
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(isDark)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearUserSession()
            _authState.value = _authState.value.copy(isLoggedIn = false)
        }
    }

    fun resetApp() {
        viewModelScope.launch {
            userState.value?.id?.let { userId ->
                taskRepository.deleteAllTasks(userId)
                authRepository.deleteUser(userId)
            }
            userPreferencesRepository.clearUserSession()
            _authState.value = AuthState(isLoggedIn = false)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateToLogin : UiEvent()
        object LoginSuccess : UiEvent()
    }
}