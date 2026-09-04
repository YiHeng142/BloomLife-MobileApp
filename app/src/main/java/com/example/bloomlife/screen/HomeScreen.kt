package com.example.bloomlife.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.WaterDrop
import com.example.bloomlife.viewmodel.DietViewModel
import com.example.bloomlife.viewmodel.WaterViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import com.example.bloomlife.viewmodel.ProfileViewModel


@Composable
fun HomeScreen(
    waterVm: WaterViewModel,
    dietVm: DietViewModel,
    profileVm: ProfileViewModel,
    onNavigateToWater: () -> Unit,
    onNavigateToExercise: () -> Unit,
    onNavigateToDiet: () -> Unit
) {
    val totalMl by waterVm.totalMl.collectAsStateWithLifecycle()
    val dailyGoalMl by waterVm.dailyGoalMl.collectAsStateWithLifecycle()
    val canFeedTree by waterVm.canFeedTree.collectAsStateWithLifecycle()
    val treeGrowthPercent by waterVm.treeGrowthPercent.collectAsStateWithLifecycle()
    val treeStage by waterVm.treeStage.collectAsStateWithLifecycle()
    val meals by dietVm.meals.collectAsStateWithLifecycle()
    val profile by profileVm.uiState.collectAsStateWithLifecycle()

    val totalLiters = totalMl / 1000f
    val goalLiters = dailyGoalMl / 1000f
    val waterPercent = ((totalMl.toFloat() / dailyGoalMl.toFloat()) * 100)
        .coerceIn(0f, 100f).toInt()
    val mlShort = (dailyGoalMl - totalMl).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Header ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00796B))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Home",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Welcome, ${profile.username}",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // ---- Water summary card ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$waterPercent%",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        IconButton(onClick = onNavigateToWater) {
                            Icon(Icons.Filled.Add, contentDescription = "Go to Water module")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = "Water",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { waterPercent / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Water: %.1f L / %.1f L".format(totalLiters, goalLiters),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (mlShort > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Reminder: You're ${mlShort}ml short of your daily water goal!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Exercise placeholder card (teammate's module) ----
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exercise: View workout plans", style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = onNavigateToExercise) {
                        Icon(Icons.Filled.Add, contentDescription = "Go to Exercise module")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

// ---- Meal Record card ----
            val allFoods = meals.flatMap { it.foods }
            val totalCalories = allFoods.sumOf { it.calories }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Meal Record:", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = onNavigateToDiet) {
                            Icon(Icons.Filled.Add, contentDescription = "Go to Diet module")
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    if (allFoods.isEmpty()) {
                        Text(
                            "No meals logged yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        allFoods.forEach { food ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium)
                                Text("${food.calories} kcal", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Total: $totalCalories kcal",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- Plant growth section ----
            Text("Plant Growth", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (treeStage) {
                            1 -> "🌱 Stage 1: Seedling"
                            2 -> "🌿 Stage 2: Sprout"
                            else -> "🌳 Stage 3: Full Tree"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = "Water progress",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { treeGrowthPercent / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                        )
                    }
                    Text(
                        text = "$treeGrowthPercent% grown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Complete your daily water goal to earn +10% growth (once per day).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { waterVm.feedTree() },
                        enabled = canFeedTree,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (canFeedTree) "Feed Tree 🌱" else "Reward claimed for today")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}