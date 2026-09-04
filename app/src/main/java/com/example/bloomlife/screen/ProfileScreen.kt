package com.example.bloomlife.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloomlife.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main composable for the Profile screen.
 * Maintains independent sections for User Profile and a BMI Calculator tool.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Standalone Calculator State
    val calcHeight by viewModel.calcHeight.collectAsState()
    val calcWeight by viewModel.calcWeight.collectAsState()
    val calcResultBmi by viewModel.calcResultBmi.collectAsState()
    val calcResultCategory by viewModel.calcResultCategory.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF673AB7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Profile", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E))
                    Text(text = "ID: ${uiState.userId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                }
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE8EAF6))
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF3F51B5))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 1: USER PROFILE ---
        Text(text = "My Profile Details", modifier = Modifier.align(Alignment.Start), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInputField(label = "Username", value = uiState.username, onValueChange = viewModel::updateUsername)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFECEFF1))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { showDatePicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Birthdate", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF37474F))
                    Text(
                        text = uiState.birthDate?.let { dateFormatter.format(Date(it)) } ?: "Select Date",
                        modifier = Modifier.weight(3f).padding(start = 16.dp),
                        fontSize = 16.sp,
                        color = if (uiState.birthDate == null) Color.Gray else Color.Black
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFECEFF1))
                ProfileInputField(label = "Age (Auto)", value = uiState.age, onValueChange = {}, readOnly = true)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFECEFF1))

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = "Gender", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF37474F))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = uiState.gender == "Male", onClick = { viewModel.updateGender("Male") })
                        Text("Male", modifier = Modifier.clickable { viewModel.updateGender("Male") })
                        Spacer(modifier = Modifier.width(24.dp))
                        RadioButton(selected = uiState.gender == "Female", onClick = { viewModel.updateGender("Female") })
                        Text("Female", modifier = Modifier.clickable { viewModel.updateGender("Female") })
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFECEFF1))
                ProfileInputField(label = "Height (cm)", value = uiState.height, onValueChange = viewModel::updateProfileHeight)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFECEFF1))
                ProfileInputField(label = "Weight (kg)", value = uiState.weight, onValueChange = viewModel::updateProfileWeight)

                Spacer(modifier = Modifier.height(16.dp))
                // Profile BMI display (This is stored data)
                Surface(
                    color = Color(0xFFE8EAF6),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Stored Profile BMI: ", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                        Text(text = if (uiState.bmi.isEmpty()) "Pending Save" else "${uiState.bmi} (${uiState.category})", color = Color(0xFF3F51B5))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save My Profile")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 2: STANDALONE BMI CALCULATOR (Independent Tool) ---
        Text(text = "Standalone BMI Calculator", modifier = Modifier.align(Alignment.Start), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInputField(label = "Height (cm)", value = calcHeight, onValueChange = viewModel::updateCalcHeight)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFECEFF1))
                ProfileInputField(label = "Weight (kg)", value = calcWeight, onValueChange = viewModel::updateCalcWeight)

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TEMPORARY RESULT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (calcResultBmi.isEmpty()) "--" else calcResultBmi,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF004D40)
                        )
                        Text(
                            text = if (calcResultCategory.isEmpty()) "Waiting for input..." else calcResultCategory,
                            fontSize = 16.sp,
                            color = Color(0xFF00695C)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.birthDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateBirthDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF37474F))
        TextField(
            value = value,
            onValueChange = { onValueChange(it.filter { char -> char != '\n' }) },
            modifier = Modifier.weight(3f),
            readOnly = readOnly,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF3F51B5),
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}
