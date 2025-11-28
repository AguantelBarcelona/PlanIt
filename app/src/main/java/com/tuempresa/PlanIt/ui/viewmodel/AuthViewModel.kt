package com.tuempresa.PlanIt.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.tuempresa.PlanIt.data.preferences.UserPreferencesRepository
import com.tuempresa.PlanIt.domain.repository.AuthRepository
import com.tuempresa.PlanIt.domain.repository.TaskRepository 
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean? = null,
    val isDarkTheme: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false
)

data class UserState(
    val uid: String,
    val username: String,
    val email: String,
    val displayName: String?,
    val profilePictureUrl: String?,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository // Still needed for theme
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
        val firebaseUser = authRepository.currentUser
        _authState.value = _authState.value.copy(isLoggedIn = firebaseUser != null)
        if (firebaseUser != null) {
            loadUserProfile()
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.currentUser?.let { user ->
                _userState.value = UserState(
                    uid = user.uid,
                    username = user.username,
                    email = user.email,
                    displayName = user.displayName,
                    profilePictureUrl = user.profilePictureUrl
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            if (email.isBlank() || password.isBlank()) {
                _events.emit(UiEvent.ShowSnackbar("Email y contraseña no pueden estar vacíos"))
                _authState.value = _authState.value.copy(isLoading = false)
                return@launch
            }
            authRepository.login(email, password)
                .onSuccess {
                    _authState.value = _authState.value.copy(isLoggedIn = true, isLoading = false)
                    _events.emit(UiEvent.LoginSuccess)
                    loadUserProfile()
                }
                .onFailure { exception ->
                    val errorMessage = getFirebaseAuthErrorMessage(exception)
                    _authState.value = _authState.value.copy(isLoading = false, error = errorMessage)
                    _events.emit(UiEvent.ShowSnackbar(errorMessage))
                }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            if (email.isBlank() || password.isBlank() || username.isBlank()) {
                 _events.emit(UiEvent.ShowSnackbar("Todos los campos son obligatorios"))
                _authState.value = _authState.value.copy(isLoading = false)
                return@launch
            }
             authRepository.register(email, password, username)
                .onSuccess {
                    _authState.value = _authState.value.copy(isLoading = false)
                    _events.emit(UiEvent.ShowSnackbar("Usuario registrado con éxito"))
                    _events.emit(UiEvent.NavigateToLogin)
                }
                .onFailure { exception ->
                    val errorMessage = getFirebaseAuthErrorMessage(exception)
                    _authState.value = _authState.value.copy(isLoading = false, error = errorMessage)
                    _events.emit(UiEvent.ShowSnackbar(errorMessage))
                }
        }
    }

    private fun getFirebaseAuthErrorMessage(exception: Throwable): String {
        return when ((exception as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_CREDENTIAL" -> "Las credenciales son incorrectas."
            "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico no es válido."
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta."
            "ERROR_USER_NOT_FOUND" -> "No se encontró ningún usuario con este correo."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo electrónico ya está en uso."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil. Debe tener al menos 6 caracteres."
            else -> "Ha ocurrido un error inesperado. Inténtalo de nuevo."
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = _authState.value.copy(isLoggedIn = false)
            _userState.value = null
        }
    }
    
    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(isDark)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateToLogin : UiEvent()
        object LoginSuccess : UiEvent()
    }
}