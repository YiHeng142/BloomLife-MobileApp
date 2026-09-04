package com.example.bloomlife.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloomlife.data.ProfileRepository
import com.example.bloomlife.data.SupabaseClientProvider
import com.example.bloomlife.screen.AccountInfoScreen
import com.example.bloomlife.screen.DietScreen
import com.example.bloomlife.screen.HomeScreen
import com.example.bloomlife.screen.PlaceholderScreen
import com.example.bloomlife.screen.ProfileScreen
import com.example.bloomlife.screen.SettingsScreen
import com.example.bloomlife.screen.WaterScreen
import com.example.bloomlife.viewmodel.WaterViewModel
import com.example.bloomlife.viewmodel.DietViewModel
import com.example.bloomlife.viewmodel.ProfileViewModel
import com.example.bloomlife.viewmodel.ProfileViewModelFactory
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("exercise", "Exercise", Icons.Filled.FitnessCenter),
    BottomNavItem("water", "Water", Icons.Filled.WaterDrop),
    BottomNavItem("home", "Home", Icons.Filled.Home),
    BottomNavItem("diet", "Diet", Icons.Filled.Restaurant),
    BottomNavItem("profile", "Me", Icons.Filled.Person)
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Wait until Supabase has restored the session before creating ViewModels
    var sessionReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // awaitInitialization suspends until Supabase finishes loading the session from storage
        SupabaseClientProvider.client.auth.awaitInitialization()
        sessionReady = true
    }

    if (!sessionReady) {
        // Simple loading state while session restores
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
// Shared ViewModel: created once here, passed down to both Home and Water
    // so both screens read/write the SAME water data (state hoisting, like Practical 6)
    val waterVm: WaterViewModel = viewModel()
    val dietVm: DietViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.route == item.route,
                        onClick = {
                            if (currentDestination?.route != item.route) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    waterVm = waterVm,
                    dietVm = dietVm,
                    onNavigateToWater = { navController.navigate("water") },
                    onNavigateToExercise = { navController.navigate("exercise") },
                    onNavigateToDiet = { navController.navigate("diet") }
                )
            }
            composable("water") {
                WaterScreen(vm = waterVm)
            }
            composable("exercise") {
                com.example.bloomlife.nav.ExerciseNavGraph()
            }
            composable("diet") {
                DietScreen(vm = dietVm)
            }
            composable("profile") {
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: "guest_user"
                val context = androidx.compose.ui.platform.LocalContext.current
                val repository = remember { ProfileRepository(context) }
                val profileVm: ProfileViewModel = viewModel(
                    key = userId,
                    factory = ProfileViewModelFactory(repository, userId)
                )
                ProfileScreen(
                    viewModel = profileVm,
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: "guest_user"
                val context = androidx.compose.ui.platform.LocalContext.current
                val repository = remember { ProfileRepository(context) }
                val profileVm: ProfileViewModel = viewModel(
                    key = userId,
                    factory = ProfileViewModelFactory(repository, userId)
                )
                SettingsScreen(
                    viewModel = profileVm,
                    onBackClick = { navController.popBackStack() },
                    onAccountInfoClick = { navController.navigate("account_info") },
                    onLogout = {
                        scope.launch {
                            SupabaseClientProvider.client.auth.signOut()
                        }
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                        // then navigate back to "login" with popUpTo clearing the back stack
                    },
                    onDeleteAccount = {
                        // TODO: needs a Supabase Edge Function or admin API call — flag to teammate
                    }
                )
            }
            composable("account_info") {
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: "guest_user"
                val context = androidx.compose.ui.platform.LocalContext.current
                val repository = remember { ProfileRepository(context) }
                val profileVm: ProfileViewModel = viewModel(
                    key = userId,
                    factory = ProfileViewModelFactory(repository, userId)
                )
                AccountInfoScreen(
                    viewModel = profileVm,
                    onBackClick = { navController.popBackStack()
                    }

                )
            }
        }
    }
}