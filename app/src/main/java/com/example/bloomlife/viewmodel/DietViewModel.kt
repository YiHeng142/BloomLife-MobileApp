package com.example.bloomlife.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bloomlife.model.FoodItem
import com.example.bloomlife.model.Meal
import com.example.bloomlife.model.RewardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DietViewModel : ViewModel() {

    private fun todayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        return formatter.format(Calendar.getInstance().time)
    }

    // ---- Meals ----
    private val _meals = MutableStateFlow(
        listOf(
            Meal(name = "Breakfast", foods = emptyList()),
            Meal(name = "Lunch", foods = emptyList()),
            Meal(name = "Dinner", foods = emptyList())
        )
    )
    val meals: StateFlow<List<Meal>> = _meals.asStateFlow()

    private val calorieGoal = 2000

    // ---- Reward state ----
    private val _reward = MutableStateFlow(RewardState())
    val reward: StateFlow<RewardState> = _reward.asStateFlow()

    val totalCalories: Int
        get() = _meals.value.sumOf { meal -> meal.foods.sumOf { it.calories } }

    private fun checkGoalCompletion() {
        val reachedGoal = totalCalories >= calorieGoal
        val current = _reward.value

        if (reachedGoal && !current.goalAchieved) {
            _reward.update {
                it.copy(
                    goalAchieved = true,
                    showCongrats = true,
                    canFeedPlant = it.lastRewardDate != todayDateString()
                )
            }
        } else if (!reachedGoal && current.goalAchieved) {
            _reward.update { it.copy(goalAchieved = false, canFeedPlant = false) }
        }
    }

    fun dismissCongrats() {
        _reward.update { it.copy(showCongrats = false) }
    }

    fun feedPlant() {
        val current = _reward.value
        if (!current.canFeedPlant) return

        val today = todayDateString()
        if (current.lastRewardDate == today) {
            _reward.update { it.copy(canFeedPlant = false) }
            return
        }

        // Already fully grown
        if (current.plantStage >= 3 && current.plantGrowthPercent >= 100) {
            _reward.update { it.copy(lastRewardDate = today, canFeedPlant = false) }
            return
        }

        val newProgress = current.plantGrowthPercent + 10

        _reward.update {
            if (newProgress >= 100) {
                if (it.plantStage < 3) {
                    it.copy(
                        plantStage = it.plantStage + 1,
                        plantGrowthPercent = 0,
                        lastRewardDate = today,
                        canFeedPlant = false
                    )
                } else {
                    it.copy(
                        plantGrowthPercent = 100,
                        lastRewardDate = today,
                        canFeedPlant = false
                    )
                }
            } else {
                it.copy(
                    plantGrowthPercent = newProgress,
                    lastRewardDate = today,
                    canFeedPlant = false
                )
            }
        }
    }

    // ---- Meal actions ----
    fun addFood(mealName: String, food: FoodItem) {
        _meals.update { current ->
            current.map { meal ->
                if (meal.name == mealName) meal.copy(foods = meal.foods + food) else meal
            }
        }
        checkGoalCompletion()
    }

    fun removeFood(mealName: String, foodId: Int) {
        _meals.update { current ->
            current.map { meal ->
                if (meal.name == mealName) {
                    meal.copy(foods = meal.foods.filter { it.id != foodId })
                } else meal
            }
        }
        checkGoalCompletion()
    }

    fun reset() {
        _meals.value = listOf(
            Meal(name = "Breakfast", foods = emptyList()),
            Meal(name = "Lunch", foods = emptyList()),
            Meal(name = "Dinner", foods = emptyList())
        )
        _reward.update { it.copy(goalAchieved = false, canFeedPlant = false) }
    }
}