package com.example.bloomlife.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bloomlife.screen.exercise.ExerciseGuideScreen
import com.example.bloomlife.screen.exercise.WorkoutPlansScreen
import com.example.bloomlife.screen.exercise.WorkoutTimerScreen

@Composable
fun ExerciseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "workoutPlans") {

        composable("workoutPlans") {
            WorkoutPlansScreen(
                onPlanClick = { planId ->
                    navController.navigate("exerciseGuide/$planId")
                }
            )
        }

        composable(
            route = "exerciseGuide/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.IntType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getInt("planId") ?: -1
            ExerciseGuideScreen(
                planId = planId,
                onExerciseClick = { exerciseId ->
                    navController.navigate("workoutTimer/$exerciseId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "workoutTimer/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getInt("exerciseId") ?: -1
            WorkoutTimerScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}