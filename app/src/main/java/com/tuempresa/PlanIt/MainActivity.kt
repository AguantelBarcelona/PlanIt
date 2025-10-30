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
import com.tuempresa.PlanIt.data.local.TaskDatabase
import com.tuempresa.PlanIt.data.preferences.UserPreferencesRepository
import com.tuempresa.PlanIt.data.repository.AuthRepositoryImpl
import com.tuempresa.PlanIt.data.repository.TaskRepositoryImpl
import com.tuempresa.PlanIt.di.ViewModelFactory
import com.tuempresa.PlanIt.navigation.Routes
import com.tuempresa.PlanIt.ui.screens.*
import com.tuempresa.PlanIt.ui.theme.MyApplicationTheme
import com.tuempresa.PlanIt.ui.viewmodel.AuthViewModel
import com.tuempresa.PlanIt.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { TaskDatabase.getDatabase(this) }
    private val authRepository by lazy { AuthRepositoryImpl(database.userDao()) }
    private val taskRepository by lazy { TaskRepositoryImpl(database.taskDao()) }
    private val userPreferencesRepository by lazy { UserPreferencesRepository(this) }
    private val viewModelFactory by lazy { ViewModelFactory(authRepository, taskRepository, userPreferencesRepository) }

    private val authViewModel: AuthViewModel by viewModels { viewModelFactory }
    private val taskViewModel: TaskViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val authState by authViewModel.authState.collectAsState()
            MyApplicationTheme(darkTheme = authState.isDarkTheme) {
                RequestNotificationPermission()
                AppNavigation(authViewModel, taskViewModel, authState.isLoggedIn)
            }
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                // Handle permission result if needed
            }
        )

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel, taskViewModel: TaskViewModel, isLoggedIn: Boolean?) {
    val navController = rememberNavController()
    val userState by authViewModel.userState.collectAsState()

    when (isLoggedIn) {
        null -> SplashScreen()
        true -> {
            NavHost(navController = navController, startDestination = Routes.TaskList.route) {
                // Routes for logged in users
                composable(Routes.TaskList.route) {
                    userState?.let { user ->
                        LaunchedEffect(user.id) {
                            taskViewModel.loadTasks(user.id)
                        }
                        TaskListScreen(navController = navController, viewModel = taskViewModel, userId = user.id)
                    }
                }
                composable(
                    route = Routes.TaskEdit.route + "?taskId={taskId}",
                    arguments = listOf(navArgument("taskId") {
                        type = NavType.IntType
                        defaultValue = 0
                    })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getInt("taskId")
                    userState?.let { user ->
                        TaskEditScreen(navController = navController, viewModel = taskViewModel, taskId = taskId, userId = user.id)
                    }
                }
                composable(Routes.Profile.route) {
                    ProfileScreen(navController = navController, viewModel = authViewModel)
                }
                composable(Routes.Calendar.route) {
                    userState?.let { user ->
                        CalendarScreen(
                            navController = navController,
                            viewModel = taskViewModel,
                            userId = user.id
                        )
                    }
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