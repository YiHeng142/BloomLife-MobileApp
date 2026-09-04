package com.example.bloomlife.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloomlife.viewmodel.ProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onAccountInfoClick: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- ACCOUNT SECTION ---
            SettingsHeader("ACCOUNT")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAccountInfoClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.username.take(1).uppercase().ifEmpty { "?" },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = uiState.username.ifEmpty { "Set your username" }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Text(text = uiState.email.ifEmpty { "Manage your account info" }, fontSize = 13.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- PREFERENCES SECTION ---
            SettingsHeader("PREFERENCES")

            SettingsToggleRow(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                initialValue = uiState.let { false },
                onToggle = { viewModel.toggleDarkMode(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                initialValue = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SUPPORT SECTION ---
            SettingsHeader("SUPPORT")

            SettingsItemRow(
                icon = Icons.Default.Help,
                title = "Help & Support",
                onClick = { showHelpDialog = true }
            )
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "About BloomLife",
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsActionRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                color = Color(0xFFE57373),
                onClick = onLogout
            )

            SettingsActionRow(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                color = Color(0xFFE57373),
                onClick = { showDeleteConfirm = true }
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "BloomLife v1.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }

    // --- DIALOGS ---

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support") },
            text = {
                Column {
                    Text("Need help? Contact our support team:")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@bloomlife.com")
                                }
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("support@bloomlife.com", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:+60312345678")
                                }
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("+60 3-1234 5678", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("Close") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About BloomLife") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Introduction", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Purpose:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("The purpose of BloomLife is to promote good health and well-being by:", fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    AboutBulletPoint("Providing personalized workout plans based on fitness levels and goals.")
                    AboutBulletPoint("Promoting sufficient water intake through hydration monitoring and reminders.")
                    AboutBulletPoint("Recording total calories taken and providing food suggestions and advice.")
                    AboutBulletPoint("Motivating users through achievements and rewards to build long-term healthy habits.")
                }
            },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("Close") } }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account") },
            text = { Text("This will permanently delete your account and all associated data. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AboutBulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text("• ", fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F51B5), fontSize = 18.sp)
        Text(text, fontSize = 15.sp)
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsItemRow(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, modifier = Modifier.weight(1f), color = Color.Black)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun SettingsToggleRow(icon: ImageVector, title: String, initialValue: Boolean, onToggle: (Boolean) -> Unit = {}) {
    var checked by remember { mutableStateOf(initialValue) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, modifier = Modifier.weight(1f), color = Color.Black)
            Switch(checked = checked, onCheckedChange = {
                checked = it
                onToggle(it)
            })
        }
    }
}

@Composable
fun SettingsActionRow(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = color, fontWeight = FontWeight.Bold)
        }
    }
}