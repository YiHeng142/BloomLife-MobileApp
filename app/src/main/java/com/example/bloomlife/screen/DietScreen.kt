package com.example.bloomlife.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloomlife.data.FoodData
import com.example.bloomlife.viewmodel.DietViewModel

@Composable
fun DietScreen(vm: DietViewModel = viewModel()) {

    val meals by vm.meals.collectAsStateWithLifecycle()
    val reward by vm.reward.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("dashboard") }
    var selectedMeal by remember { mutableStateOf("") }

    if (currentScreen == "dashboard") {
        DashboardScreen(
            meals = meals,
            onAddFood = { mealName ->
                selectedMeal = mealName
                currentScreen = "addFood"
            },
            onRemoveFood = { mealName, foodId -> vm.removeFood(mealName, foodId) },
            onReset = { vm.reset() },
            onRecommendations = { currentScreen = "recommendations" },
            canFeedPlant = reward.canFeedPlant,
            plantStage = reward.plantStage,
            plantGrowthPercent = reward.plantGrowthPercent,
            onFeedPlant = { vm.feedPlant() }
        )

    } else if (currentScreen == "addFood") {
        AddFoodScreen(
            mealName = selectedMeal,
            foods = FoodData.foods,
            onFoodSelected = { food ->
                vm.addFood(selectedMeal, food)
                currentScreen = "dashboard"
            },
            onBack = { currentScreen = "dashboard" }
        )

    } else if (currentScreen == "recommendations") {
        val totalCalories = meals.sumOf { meal -> meal.foods.sumOf { it.calories } }
        val totalProtein = meals.sumOf { meal -> meal.foods.sumOf { it.protein } }
        val totalCarbohydrates = meals.sumOf { meal -> meal.foods.sumOf { it.carbohydrates } }
        val totalFat = meals.sumOf { meal -> meal.foods.sumOf { it.fat } }

        RecommendationScreen(
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbohydrates = totalCarbohydrates,
            totalFat = totalFat,
            foods = FoodData.foods,
            onBack = { currentScreen = "dashboard" }
        )
    }

    // ---- Congratulation popup: shown when daily calorie goal is reached ----
    if (reward.showCongrats) {
        AlertDialog(
            onDismissRequest = { vm.dismissCongrats() },
            title = { Text("🎉 Goal Reached!") },
            text = {
                Text("Congratulations! You've hit your daily nutrition goal. Your plant is ready to be fed 🌳🌱")
            },
            confirmButton = {
                TextButton(onClick = { vm.dismissCongrats() }) { Text("Nice!") }
            }
        )
    }
}