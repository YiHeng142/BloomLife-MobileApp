package com.example.bloomlife.screen.exercise

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bloomlife.data.ExerciseData
import com.example.bloomlife.model.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseGuideScreen(
    planId: Int,
    onExerciseClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val plan = ExerciseData.getPlanById(planId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan?.name ?: "Exercise Guide") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (plan == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Plan not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(plan.exercises, key = { it.id }) { exercise ->
                    ExerciseGuideCard(
                        exercise = exercise,
                        onStartClick = { onExerciseClick(exercise.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseGuideCard(
    exercise: Exercise,
    onStartClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = exercise.imageRes),
                contentDescription = exercise.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${exercise.sets} sets × ${exercise.reps} reps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = exercise.instructions,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Timer")
            }
        }
    }
}