package com.example.bloomlife.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloomlife.screen.water.WaterEntryRow
import com.example.bloomlife.viewmodel.WaterViewModel
import com.example.bloomlife.model.WaterEntry


@Composable
fun WaterScreen(vm: WaterViewModel = viewModel()) {
    val totalMl by vm.totalMl.collectAsStateWithLifecycle()
    val dailyGoalMl by vm.dailyGoalMl.collectAsStateWithLifecycle()
    val logs by vm.logs.collectAsStateWithLifecycle()
    val customAmount by vm.customAmount.collectAsStateWithLifecycle()
    val showCongrats by vm.showCongrats.collectAsStateWithLifecycle()
    val canFeedTree by vm.canFeedTree.collectAsStateWithLifecycle()
    val goalInput by vm.goalInput.collectAsStateWithLifecycle()


    // Dialog visibility state — same nullable/boolean pattern as Practical 6's AlertDialog
    var showLogDialog by remember { mutableStateOf(false) }
    var entryBeingEdited by remember { mutableStateOf<WaterEntry?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalErrorText by remember { mutableStateOf<String?>(null) }

    val totalLiters = totalMl / 1000f
    val goalLiters = dailyGoalMl / 1000f
    val progress = (totalMl.toFloat() / dailyGoalMl.toFloat()).coerceIn(0f, 1f)



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ---- Header ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Water", style = MaterialTheme.typography.headlineMedium)
            }
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---- Progress card (READ) — tap to edit daily goal ----
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    goalErrorText = null
                    vm.onGoalInputChange(dailyGoalMl.toString())
                    showGoalDialog = true
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "%.1f L".format(totalLiters),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "of %.1f L daily goal".format(goalLiters),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap to change goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- Reward: Feed Tree button (appears after goal is reached) ----
        if (canFeedTree) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.feedTree() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF00796B)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🌱 Feed Tree")
            }
        }

        // ---- Quick actions (CREATE) ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { vm.addWater(250) },
                modifier = Modifier.weight(1f)
            ) {
                Text("+ 250ml")
            }
            OutlinedButton(
                onClick = { showLogDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Log")
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Today's log", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // ---- Log history (READ list, tap = UPDATE, delete icon = DELETE) ----
        if (logs.isEmpty()) {
            Text(
                text = "No entries yet. Tap + 250ml or Log to start.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items = logs, key = { it.id }) { entry ->
                    WaterEntryRow(
                        entry = entry,
                        onEditClick = {
                            entryBeingEdited = entry
                            editAmountText = entry.amountMl.toString()
                        },
                        onDeleteClick = { vm.removeEntry(entry) }
                    )
                }
            }
        }
    } // <-- end of Column

    // ---- CREATE dialog: log a custom amount ----
    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("Log water intake") },
            text = {
                OutlinedTextField(
                    value = customAmount,
                    onValueChange = vm::onCustomAmountChange,
                    label = { Text("Amount (ml)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addFromCustomInput()
                    showLogDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showLogDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ---- UPDATE dialog: edit an existing entry ----
    entryBeingEdited?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryBeingEdited = null },
            title = { Text("Edit entry") },
            text = {
                OutlinedTextField(
                    value = editAmountText,
                    onValueChange = { editAmountText = it },
                    label = { Text("Amount (ml)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newAmount = editAmountText.toIntOrNull()
                    if (newAmount != null) {
                        vm.updateEntry(entry, newAmount)
                    }
                    entryBeingEdited = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { entryBeingEdited = null }) { Text("Cancel") }
            }
        )
    }

    // ---- Edit daily goal dialog (minimum 1.0 L enforced) ----
    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set daily water goal") },
            text = {
                Column {
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = {
                            vm.onGoalInputChange(it)
                            goalErrorText = null
                        },
                        label = { Text("Goal (ml)") },
                        singleLine = true,
                        isError = goalErrorText != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (goalErrorText != null) {
                        Text(
                            text = goalErrorText!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val error = vm.validateGoalInput()
                    if (error != null) {
                        goalErrorText = error
                    } else {
                        vm.updateDailyGoal()
                        showGoalDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ---- Congratulation popup: shown once when daily goal is reached ----
    if (showCongrats) {
        AlertDialog(
            onDismissRequest = { vm.dismissCongrats() },
            title = { Text("🎉 Goal Reached!") },
            text = {
                Text("Congratulations! You've hit your daily water goal. Your tree is ready to be fed 🌳💧")
            },
            confirmButton = {
                TextButton(onClick = { vm.dismissCongrats() }) {
                    Text("Nice!")
                }
            }
        )
    }
} // <-- end of WaterScreen function