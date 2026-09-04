package com.example.bloomlife.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bloomlife.model.Meal

@Composable
fun DashboardScreen(
    meals: List<Meal>,
    onAddFood: (String) -> Unit,
    onRemoveFood: (String, Int) -> Unit,
    onReset: () -> Unit,
    onRecommendations: () -> Unit,
    canFeedPlant: Boolean,
    plantStage: Int,
    plantGrowthPercent: Int,
    onFeedPlant: () -> Unit
) {
    val totalCalories = meals.sumOf { meal ->
        meal.foods.sumOf { food ->
            food.calories
        }
    }

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

    val calorieTarget = 2000
    val proteinTarget = 100.0
    val carbohydrateTarget = 250.0
    val fatTarget = 65.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //Title
        item {
            Text(
                text = "Nutrition Tracker",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Track your daily food intake",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        //Nutrition Summary
        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Today's Nutrition",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "$totalCalories / $calorieTarget kcal",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = {
                            (totalCalories.toFloat() /
                                    calorieTarget)
                                .coerceIn(0f, 1f)
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    NutritionRow(
                        name = "Protein",
                        current = totalProtein,
                        target = proteinTarget,
                        unit = "g"
                    )

                    NutritionRow(
                        name = "Carbohydrates",
                        current = totalCarbohydrates,
                        target = carbohydrateTarget,
                        unit = "g"
                    )

                    NutritionRow(
                        name = "Fat",
                        current = totalFat,
                        target = fatTarget,
                        unit = "g"
                    )
                }
            }
        }

        //Plant Reward
        item {
            PlantCard(
                plantStage = plantStage,
                plantGrowthPercent = plantGrowthPercent
            )
        }

        //Feed Plant Button
        if (canFeedPlant) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎉 Daily Goal Achieved!",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "You've earned a reward. " +
                                    "Feed your plant to make it grow!"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onFeedPlant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🌱 Feed Plant")
                        }
                    }
                }
            }
        }

        //Meals Title
        item {
            Text(
                text = "Meals",
                style = MaterialTheme.typography.titleLarge
            )
        }

        //Meal Cards
        items(
            count = meals.size
        ) { index -> val meal = meals[index]
            MealCard(
                meal = meal,

                onAddFood = {
                    onAddFood(meal.name)
                },
                onRemoveFood = { foodId -> onRemoveFood(
                        meal.name,
                        foodId
                    )
                }
            )
        }

        //Recommendations
        item {

            Button(
                onClick = onRecommendations,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "💡 Food Recommendations"
                )
            }
        }

        //Reset
        item {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset"
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "Reset Today's Meals"
                )
            }
        }
    }
}

//Plant Card
@Composable
fun PlantCard(
    plantStage: Int,
    plantGrowthPercent: Int
) {
    val plantEmoji = when (plantStage) {
        1 -> "🌱"
        2 -> "🌿"
        else -> "🌳"
    }

    val plantName = when (plantStage) {
        1 -> "Seedling"
        2 -> "Young Plant"
        else -> "Full Tree"
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌱 Your Plant",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plantEmoji,
                style = MaterialTheme.typography.displayMedium
            )

            Text(
                text = plantName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$plantGrowthPercent% grown"
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = {
                    plantGrowthPercent / 100f
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

//Nutrition Row
@Composable
fun NutritionRow(
    name: String,
    current: Double,
    target: Double,
    unit: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name
            )
            Text(
                text = "${"%.1f".format(current)} / " +
                        "${"%.0f".format(target)} $unit"
            )
        }
        LinearProgressIndicator(
            progress = {
                (current / target)
                    .toFloat()
                    .coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

//Meal Card
@Composable
fun MealCard(
    meal: Meal,
    onAddFood: () -> Unit,
    onRemoveFood: (Int) -> Unit
) {
    val calories = meal.foods.sumOf { food ->
        food.calories
    }
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            //Meal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$calories kcal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(
                    onClick = onAddFood
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add food"
                    )
                }
            }

            //Food List
            if (meal.foods.isEmpty()) {
                Text(
                    text = "No food added yet",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                meal.foods.forEach { food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = food.name
                            )
                            Text(
                                text = "${food.calories} kcal • " +
                                        "${food.protein}g protein"
                            )
                        }
                        IconButton(
                            onClick = {onRemoveFood(food.id)}
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete food"
                            )
                        }
                    }
                }
            }
        }
    }
}

