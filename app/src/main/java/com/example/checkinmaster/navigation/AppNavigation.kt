package com.example.checkinmaster.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.checkinmaster.ui.screens.detail.TaskDetailScreen
import com.example.checkinmaster.ui.screens.detail.TaskDetailViewModel
import com.example.checkinmaster.ui.screens.home.HomeScreen
import com.example.checkinmaster.ui.screens.home.HomeViewModel

object Destinations {
    const val HOME = "home"
    const val DETAIL = "detail/{taskId}" // taskId = 0 means new
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {
        composable(Destinations.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = vm,
                onAddTask = { navController.navigate("detail/0") },
                onTaskClick = { navController.navigate("detail/$it") }
            )
        }
        composable(
            route = "detail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val vm: TaskDetailViewModel = hiltViewModel(backStackEntry)
            TaskDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
