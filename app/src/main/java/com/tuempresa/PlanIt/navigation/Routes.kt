package com.tuempresa.PlanIt.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object TaskList : Routes("taskList")
    object TaskDetail : Routes("taskDetail")
    object TaskEdit : Routes("taskEdit")
    object Profile : Routes("profile")
    object Calendar : Routes("calendar")
}