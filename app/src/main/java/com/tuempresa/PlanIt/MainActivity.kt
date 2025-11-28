package com.tuempresa.PlanIt

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tuempresa.PlanIt.data.local.TaskDatabase
import com.tuempresa.PlanIt.data.network.WeatherApiClient
import com.tuempresa.PlanIt.data.preferences.UserPreferencesRepository
import com.tuempresa.PlanIt.data.repository.AuthRepositoryImpl
import com.tuempresa.PlanIt.data.repository.TaskRepositoryImpl
import com.tuempresa.PlanIt.data.repository.WeatherRepositoryImpl
import com.tuempresa.PlanIt.di.ViewModelFactory
import com.tuempresa.PlanIt.domain.repository.AuthRepository
import com.tuempresa.PlanIt.domain.repository.TaskRepository
import com.tuempresa.PlanIt.domain.use_case.GetWeatherUseCase
import com.tuempresa.PlanIt.navigation.Routes
import com.tuempresa.PlanIt.ui.screens.*
import com.tuempresa.PlanIt.ui.theme.MyApplicationTheme
import com.tuempresa.PlanIt.ui.viewmodel.AuthViewModel
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel
import com.tuempresa.PlanIt.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    private val database by lazy { TaskDatabase.getDatabase(this) }
    // Use interfaces for repositories
    private val authRepository: AuthRepository by lazy { AuthRepositoryImpl() } 
    private val taskRepository: TaskRepository by lazy { TaskRepositoryImpl(database.taskDao()) }
    private val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    // Weather dependencies
    private val weatherApiClient by lazy { WeatherApiClient() }
    private val weatherRepository by lazy { WeatherRepositoryImpl(weatherApiClient) }
    private val getWeatherUseCase by lazy { GetWeatherUseCase(weatherRepository) }

    private val viewModelFactory by lazy { 
        ViewModelFactory(
            application,
            authRepository, 
            taskRepository, 
            userPreferencesRepository,
            getWeatherUseCase
        ) 
    }

    private val authViewModel: AuthViewModel by viewModels { viewModelFactory }
    private val taskViewModel: TaskViewModel by viewModels { viewModelFactory }
    private val weatherViewModel: WeatherViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val authState by authViewModel.authState.collectAsState()
            MyApplicationTheme(darkTheme = authState.isDarkTheme) {
                RequestAppPermissions()
                AppNavigation(authViewModel, taskViewModel, weatherViewModel, authState.isLoggedIn)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestAppPermissions() {
    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

    val permissionStates = rememberMultiplePermissionsState(permissions = permissions)

    LaunchedEffect(Unit) {
        permissionStates.launchMultiplePermissionRequest()
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel, taskViewModel: TaskViewModel, weatherViewModel: WeatherViewModel, isLoggedIn: Boolean?) {
    val navController = rememberNavController()

    // NOTE: The user ID from Firebase is a String (uid), but the local task database still expects an Int.
    // This is a temporary fix to make the app compile. The next step is to migrate
    // the Task entity to use a String userId.
    val temporaryUserId = 0

    when (isLoggedIn) {
        null -> SplashScreen()
        true -> {
            NavHost(navController = navController, startDestination = Routes.TaskList.route) {
                // Routes for logged in users
                composable(Routes.TaskList.route) {
                    LaunchedEffect(temporaryUserId) {
                        taskViewModel.loadTasks(temporaryUserId)
                    }
                    TaskListScreen(navController = navController, viewModel = taskViewModel, userId = temporaryUserId)
                }
                composable(
                    route = Routes.TaskEdit.route + "?taskId={taskId}",
                    arguments = listOf(navArgument("taskId") {
                        type = NavType.IntType
                        defaultValue = 0
                    })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getInt("taskId")
                    TaskEditScreen(navController = navController, viewModel = taskViewModel, weatherViewModel = weatherViewModel, taskId = taskId, userId = temporaryUserId)
                }
                composable(Routes.Profile.route) {
                    ProfileScreen(navController = navController, viewModel = authViewModel)
                }
                composable(Routes.Calendar.route) {
                    CalendarScreen(
                        navController = navController,
                        viewModel = taskViewModel,
                        userId = temporaryUserId
                    )
                }
            }
        }
        false -> {
            NavHost(navController = navController, startDestination = Routes.Login.route) {
                // Routes for logged out users
                composable(Routes.Login.route) {
                    LoginScreen(navController = navController, viewModel = authViewModel)
                }
                composable(Routes.Register.route) {
                    RegisterScreen(navController = navController, viewModel = authViewModel)
                }
            }
        }
    }
}