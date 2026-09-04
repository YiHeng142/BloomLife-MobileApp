package com.example.bloomlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BloomLifeTheme() {
                BloomLife()
            }
        }
    }
}


//Get Current Date
fun todayDateString(): String {
    val formatter = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    )
    formatter.timeZone = TimeZone.getTimeZone(
        "Asia/Kuala_Lumpur"
    )
    return formatter.format(
        Calendar.getInstance().time
    )
}
@Composable
fun BloomLife() {
    //Meals
    var meals by remember {mutableStateOf(listOf(
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

    //Screen
    var currentScreen by remember {mutableStateOf("dashboard")}

    //Selected Meal
    var selectedMeal by remember {mutableStateOf("")}

    //Reward Variables
    var goalAchieved by remember {mutableStateOf(false)}
    var showCongrats by remember {mutableStateOf(false)}
    var canFeedPlant by remember {mutableStateOf(false)}
    var plantStage by remember {mutableStateOf(1)}
    var plantGrowthPercent by remember {mutableStateOf(0)}
    var lastRewardDate by remember {mutableStateOf("")}

    //Calculate Total Calories
    val totalCalories = meals.sumOf { meal ->
        meal.foods.sumOf { food ->
            food.calories
        }
    }

    val calorieGoal = 2000

    //Check Goal
    LaunchedEffect(totalCalories) {
        val reachedGoal = totalCalories >= calorieGoal
        if (
            reachedGoal &&
            !goalAchieved
        ) {
            goalAchieved = true
            showCongrats = true
            canFeedPlant = lastRewardDate != todayDateString()

        } else if (
            !reachedGoal &&
            goalAchieved
        ) {
            goalAchieved = false
            canFeedPlant = false
        }
    }

    fun feedPlant() {

        //User cannot feed the plant
        //if reward isn't available
        if (!canFeedPlant) {
            return
        }

        val today = todayDateString()
        //Prevent multiple rewards
        //on the same day

        if (lastRewardDate == today) {
            canFeedPlant = false
            return
        }

        //If plant is completely grown
        if(
            plantStage >= 3 &&
            plantGrowthPercent >= 100
        ) {
            lastRewardDate = today
            canFeedPlant = false
            return
        }

        //Grow plant by 10%
        val newProgress =
            plantGrowthPercent + 10

        if (newProgress >= 100) {
            if (plantStage < 3) {
                //Move to next stage
                plantStage += 1
                //Reset progress
                plantGrowthPercent = 0
            } else {
                // Final stage
                plantGrowthPercent = 100
            }
        } else {
            plantGrowthPercent = newProgress
        }

        //Record reward date
        lastRewardDate = today

        //Hide reward button
        canFeedPlant = false
    }

    //DashBoard
    if (currentScreen == "dashboard") {
        DashboardScreen(
            meals = meals,
            onAddFood = { mealName -> selectedMeal = mealName
                currentScreen = "addFood"
            },
            onRemoveFood = {
                    mealName,
                    foodId -> meals = meals.map { meal ->
                    if (
                        meal.name ==
                        mealName
                    ) {
                        meal.copy(
                            foods =
                                meal.foods.filter {
                                    it.id != foodId
                                }
                        )
                    } else {
                        meal
                    }
                }
            },

            onReset = { meals = listOf(
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
                goalAchieved = false
                canFeedPlant = false
            },

            onRecommendations = {currentScreen ="recommendations"},
            canFeedPlant = canFeedPlant,
            plantStage = plantStage,
            plantGrowthPercent = plantGrowthPercent,
            onFeedPlant = {
                feedPlant()
            }
        )
    }

    //Add Food Screen
    else if (
        currentScreen == "addFood"
    ) {
        AddFoodScreen(
            mealName = selectedMeal,
            foods = FoodData.foods,
            onFoodSelected = { food -> addFoodToMeal(
                    mealName = selectedMeal,
                    food = food,
                    meals = meals,
                    onMealsChanged = {meals = it}
                )
                currentScreen = "dashboard"
            },

            onBack = {
                currentScreen =
                    "dashboard"
            }
        )
    }

    //Recommendation Screen
    else if (
        currentScreen ==
        "recommendations"
    ) {
        val totalProtein = meals.sumOf { meal ->
                meal.foods.sumOf { food ->
                    food.protein
                }
            }

        val totalCarbohydrates = meals.sumOf { meal ->
                meal.foods.sumOf { food ->
                    food.carbohydrates
                }
            }

        val totalFat = meals.sumOf { meal ->
                meal.foods.sumOf { food ->
                    food.fat
                }
            }

        RecommendationScreen(
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbohydrates = totalCarbohydrates,
            totalFat = totalFat,
            foods = FoodData.foods,
            onBack = {currentScreen = "dashboard"}
        )
    }

    //Congratulation popup: shown when daily goal is reached
    if (showCongrats) {
        AlertDialog(
            onDismissRequest = {
                showCongrats = false
            },
            title = {
                Text("🎉 Goal Reached!")
            },
            text = {
                Text(
                    "Congratulations! You've hit your daily nutrition goal. " +
                            "Your plant is ready to be fed 🌳🌱"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCongrats = false
                    }
                ) {
                    Text("Nice!")
                }
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

    val updatedMeals =
        meals.map { meal ->
            if (
                meal.name ==
                mealName
            ) {
                meal.copy(foods = meal.foods + food)
            } else {
                meal
            }
        }
    onMealsChanged(
        updatedMeals
    )
}

