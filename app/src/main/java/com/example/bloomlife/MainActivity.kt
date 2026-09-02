package com.example.bloomlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.bloomlife.data.FoodData
import com.example.bloomlife.model.FoodItem
import com.example.bloomlife.model.Meal
import com.example.bloomlife.screen.AddFoodScreen
import com.example.bloomlife.screen.DashboardScreen
import com.example.bloomlife.screen.RecommendationScreen
import com.example.bloomlife.ui.theme.BloomLifeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {

            BloomLifeTheme{

                BloomLife()
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun BloomLife() {

    var meals by remember {

        mutableStateOf(
            listOf(
                Meal(
                    name = "Breakfast",
                    foods = emptyList()
                ),

                Meal(
                    name = "Lunch",
                    foods = emptyList()
                ),

                Meal(
                    name = "Dinner",
                    foods = emptyList()
                )
            )
        )
    }

    var currentScreen by remember {
        mutableStateOf("dashboard")
    }

    var selectedMeal by remember {
        mutableStateOf("")
    }

    if (currentScreen == "dashboard") {
        DashboardScreen(
            meals = meals,
            onAddFood = { mealName ->
                selectedMeal = mealName
                currentScreen = "addFood"
            },

            onRemoveFood = { mealName, foodId ->
                meals = meals.map { meal ->
                    if (meal.name == mealName) {
                        meal.copy(
                            foods = meal.foods.filter {
                                it.id != foodId
                            }
                        )
                    } else {
                        meal
                    }
                }
            },

            onReset = {
                meals = listOf(
                    Meal(
                        name = "Breakfast",
                        foods = emptyList()
                    ),

                    Meal(
                        name = "Lunch",
                        foods = emptyList()
                    ),

                    Meal(
                        name = "Dinner",
                        foods = emptyList()
                    )
                )
            },

            onRecommendations = {
                currentScreen = "recommendations"
            }
        )

    } else if (currentScreen == "addFood") {

        AddFoodScreen(
            mealName = selectedMeal,
            foods = FoodData.foods,
            onFoodSelected = { food ->
                addFoodToMeal(
                    mealName = selectedMeal,
                    food = food,
                    meals = meals,
                    onMealsChanged = {
                        meals = it
                    }
                )

                currentScreen = "dashboard"
            },

            onBack = {
                currentScreen = "dashboard"
            }
        )

    } else if (currentScreen == "recommendations") {
        val totalCalories = meals.sumOf { meal ->
            meal.foods.sumOf { it.calories }
        }

        val totalProtein = meals.sumOf { meal ->
            meal.foods.sumOf { it.protein }
        }

        val totalCarbohydrates = meals.sumOf { meal ->
            meal.foods.sumOf { it.carbohydrates }
        }

        val totalFat = meals.sumOf { meal ->
            meal.foods.sumOf { it.fat }
        }

        RecommendationScreen(
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbohydrates = totalCarbohydrates,
            totalFat = totalFat,
            foods = FoodData.foods,

            onBack = {
                currentScreen = "dashboard"
            }
        )
    }
}

fun addFoodToMeal(
    mealName: String,
    food: FoodItem,
    meals: List<Meal>,
    onMealsChanged: (List<Meal>) -> Unit
) {

    val updatedMeals = meals.map { meal ->

        if (meal.name == mealName) {

            meal.copy(
                foods = meal.foods + food
            )

        } else {
            meal
        }
    }
    onMealsChanged(updatedMeals)
}