package com.example.bloomlife.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bloomlife.model.FoodItem

@Composable
fun RecommendationScreen(
    totalCalories: Int,
    totalProtein: Double,
    totalCarbohydrates: Double,
    totalFat: Double,
    foods: List<FoodItem>,
    onBack: () -> Unit
) {

    val calorieTarget = 2000
    val proteinTarget = 100.0
    val carbohydrateTarget = 250.0
    val fatTarget = 65.0

    val recommendations = mutableListOf<FoodItem>()

    if (totalProtein < proteinTarget * 0.7) {

        recommendations.addAll(
            foods.filter {
                it.protein >= 10
            }
        )
    }

    if (totalCarbohydrates < carbohydrateTarget * 0.7) {

        recommendations.addAll(
            foods.filter {
                it.carbohydrates >= 20
            }
        )
    }

    if (totalFat < fatTarget * 0.7) {

        recommendations.addAll(
            foods.filter {
                it.fat >= 5
            }
        )
    }

    val uniqueRecommendations = recommendations
        .distinctBy { it.id }
        .take(8)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Row {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        item {

            Text(
                text = "Based on today's intake",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Nutrition Summary",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )

                    Text(
                        text = "Calories: $totalCalories / $calorieTarget kcal"
                    )

                    Text(
                        text = "Protein: ${"%.1f".format(totalProtein)} / " +
                                "$proteinTarget g"
                    )

                    Text(
                        text = "Carbohydrates: " +
                                "${"%.1f".format(totalCarbohydrates)} / " +
                                "$carbohydrateTarget g"
                    )

                    Text(
                        text = "Fat: ${"%.1f".format(totalFat)} / " +
                                "$fatTarget g"
                    )
                }
            }
        }

        item {

            Text(
                text = getRecommendationMessage(
                    totalCalories,
                    totalProtein,
                    totalCarbohydrates,
                    totalFat
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (uniqueRecommendations.isEmpty()) {

            item {

                Text(
                    text = "Your intake is currently close to the example targets. " +
                            "Continue eating a varied and balanced diet."
                )
            }

        } else {

            items(uniqueRecommendations) { food ->

                RecommendationCard(food)
            }
        }
    }
}

fun getRecommendationMessage(
    calories: Int,
    protein: Double,
    carbohydrates: Double,
    fat: Double
): String {

    return when {

        protein < 70 ->
            "Your protein intake is relatively low. Consider adding protein-rich foods."

        carbohydrates < 175 ->
            "Your carbohydrate intake is relatively low. Consider adding whole grains or fruit."

        fat < 45 ->
            "Your healthy fat intake is relatively low. Consider foods such as avocado, nuts or salmon."

        calories > 2200 ->
            "Your calorie intake is relatively high today. Consider choosing lighter meals for the rest of the day."

        else ->
            "Your nutrition intake looks reasonably balanced today."
    }
}

@Composable
fun RecommendationCard(
    food: FoodItem
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${food.calories} kcal"
            )

            Text(
                text = "Protein: ${food.protein}g"
            )

            Text(
                text = "Carbohydrates: ${food.carbohydrates}g"
            )

            Text(
                text = "Fat: ${food.fat}g"
            )
        }
    }
}