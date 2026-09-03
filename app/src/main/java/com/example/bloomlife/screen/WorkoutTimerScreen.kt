package com.example.bloomlife.screen.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloomlife.data.ExerciseData
import com.example.bloomlife.viewmodel.TimerPhase
import com.example.bloomlife.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTimerScreen(
    exerciseId: Int,
    vm: TimerViewModel = viewModel(),
    onBack: () -> Unit
) {
    val exercise = ExerciseData.getExerciseById(exerciseId)
    val phase by vm.phase.collectAsStateWithLifecycle()
    val secondsLeft by vm.secondsLeft.collectAsStateWithLifecycle()
    val currentRound by vm.currentRound.collectAsStateWithLifecycle()
    val isRunning by vm.isRunning.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Timer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (phase) {
                    TimerPhase.WORK -> "WORK"
                    TimerPhase.REST -> "REST"
                    TimerPhase.FINISHED -> "DONE!"
                    TimerPhase.IDLE -> "READY"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = when (phase) {
                    TimerPhase.WORK -> MaterialTheme.colorScheme.primary
                    TimerPhase.REST -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "$secondsLeft",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(8.dp))

            if (currentRound > 0) {
                Text(
                    text = "Round $currentRound",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(32.dp))

            if (phase == TimerPhase.IDLE) {
                Button(
                    onClick = {
                        val ex = exercise
                        if (ex != null) {
                            vm.startSimpleTimer(seconds = 30)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simple Timer (30s)")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val ex = exercise
                        if (ex != null) {
                            vm.startIntervalTimer(
                                rounds = ex.sets,
                                work = 30,
                                rest = ex.restSeconds
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Interval Timer (${exercise?.sets ?: 0} rounds)")
                }
            }

            if (phase == TimerPhase.WORK || phase == TimerPhase.REST) {
                Button(
                    onClick = vm::pause,
                    enabled = isRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pause")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = vm::reset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset")
                }
            }

            if (phase == TimerPhase.FINISHED) {
                Button(
                    onClick = vm::reset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Do Another Round")
                }
            }
        }
    }
}